package com.tcm.prescription.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.tcm.prescription.common.ErrorCode;
import com.tcm.prescription.dto.MedicalRecordCreateReq;
import com.tcm.prescription.dto.MedicalRecordListItemResp;
import com.tcm.prescription.dto.MedicalRecordResp;
import com.tcm.prescription.entity.MedicalRecord;
import com.tcm.prescription.entity.Prescription;
import com.tcm.prescription.entity.PrescriptionItem;
import com.tcm.prescription.exception.ServiceException;
import com.tcm.prescription.repository.MedicalRecordRepository;
import com.tcm.prescription.repository.PrescriptionItemRepository;
import com.tcm.prescription.repository.PrescriptionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import javax.persistence.criteria.Predicate;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class MedicalRecordService {

    private final MedicalRecordRepository medicalRecordRepository;
    private final PrescriptionRepository prescriptionRepository;
    private final PrescriptionItemRepository prescriptionItemRepository;
    private final ObjectMapper objectMapper;

    @Transactional
    public MedicalRecordResp createMedicalRecord(MedicalRecordCreateReq req) {
        // Trim and validate patient name
        String patientName = req.getPatientName().trim();
        if (!StringUtils.hasText(patientName)) {
            throw new ServiceException(ErrorCode.PARAM_ERROR.getCode(), "Patient name cannot be blank");
        }

        // Deduplicate prescription IDs
        List<Long> prescriptionIds = req.getPrescriptionIds().stream()
                .distinct()
                .collect(Collectors.toList());

        if (prescriptionIds.isEmpty()) {
            throw new ServiceException(ErrorCode.PARAM_ERROR.getCode(), "Prescription IDs cannot be empty");
        }

        // Fetch prescriptions
        List<Prescription> prescriptions = prescriptionRepository.findAllByIds(prescriptionIds);
        if (prescriptions.size() != prescriptionIds.size()) {
            throw new ServiceException(ErrorCode.NOT_FOUND.getCode(), "Some prescriptions not found or deleted");
        }

        // Create prescription ID to name mapping (maintain order)
        Map<Long, String> idToNameMap = prescriptions.stream()
                .collect(Collectors.toMap(Prescription::getId, Prescription::getName, (a, b) -> a));

        // Build prescription names snapshot (in order of input IDs)
        String prescriptionNamesSnapshot = prescriptionIds.stream()
                .map(idToNameMap::get)
                .collect(Collectors.joining(","));

        // Merge herbs using internal logic
        List<MergedHerbDto> mergedHerbs = mergeHerbsInternal(prescriptionIds);

        // Serialize to JSON
        String prescriptionIdsJson;
        String mergedHerbsJson;
        try {
            prescriptionIdsJson = objectMapper.writeValueAsString(prescriptionIds);
            mergedHerbsJson = objectMapper.writeValueAsString(mergedHerbs);
        } catch (JsonProcessingException e) {
            log.error("Failed to serialize JSON", e);
            throw new ServiceException(ErrorCode.BUSINESS_ERROR.getCode(), "Failed to process data");
        }

        // Create and save medical record
        MedicalRecord record = new MedicalRecord();
        record.setPatientName(patientName);
        record.setVisitDate(req.getVisitDate());
        record.setPrescriptionIdsJson(prescriptionIdsJson);
        record.setPrescriptionNamesSnapshot(prescriptionNamesSnapshot);
        record.setMergedHerbsJson(mergedHerbsJson);
        record.setNotes(req.getNotes());

        record = medicalRecordRepository.save(record);

        log.info("Created medical record {} for patient: {}", record.getId(), patientName);

        return toDetailResp(record);
    }

    public Page<MedicalRecordListItemResp> listByPatientName(String patientName, int page, int size) {
        if (!StringUtils.hasText(patientName)) {
            throw new ServiceException(ErrorCode.PARAM_ERROR.getCode(), "Patient name is required");
        }

        Specification<MedicalRecord> spec = (root, query, cb) -> {
            Predicate predicate = cb.like(cb.lower(root.get("patientName")), "%" + patientName.toLowerCase() + "%");
            return predicate;
        };

        Sort sort = Sort.by(Sort.Direction.DESC, "visitDate").and(Sort.by(Sort.Direction.DESC, "id"));
        Page<MedicalRecord> records = medicalRecordRepository.findAll(spec, PageRequest.of(page - 1, size, sort));

        return records.map(this::toListItemResp);
    }

    public MedicalRecordResp getDetail(Long id) {
        MedicalRecord record = getById(id);
        return toDetailResp(record);
    }

    @Transactional
    public void delete(Long id) {
        MedicalRecord record = getById(id);
        record.setDeletedAt(LocalDateTime.now());
        medicalRecordRepository.save(record);
        log.info("Soft deleted medical record {}", id);
    }

    private MedicalRecord getById(Long id) {
        return medicalRecordRepository.findById(id)
                .orElseThrow(() -> new ServiceException(ErrorCode.NOT_FOUND.getCode(), "Medical record not found"));
    }

    /**
     * Internal method to merge herbs from multiple prescriptions
     * Takes the maximum dose for duplicate herbs
     */
    private List<MergedHerbDto> mergeHerbsInternal(List<Long> prescriptionIds) {
        // Fetch all prescription items
        List<PrescriptionItem> allItems = prescriptionItemRepository.findByPrescriptionIds(prescriptionIds);

        // Group by herb ID and take max dose
        Map<Long, MergedHerbBuilder> map = new HashMap<>();
        for (PrescriptionItem item : allItems) {
            map.computeIfAbsent(item.getHerb().getId(),
                    k -> new MergedHerbBuilder(item.getHerbNameSnapshot()))
                    .addDose(item.getDoseG());
        }

        // Build result and sort by name
        return map.values().stream()
                .map(MergedHerbBuilder::build)
                .sorted(Comparator.comparing(MergedHerbDto::getName))
                .collect(Collectors.toList());
    }

    private MedicalRecordResp toDetailResp(MedicalRecord record) {
        MedicalRecordResp resp = new MedicalRecordResp();
        resp.setId(record.getId());
        resp.setPatientName(record.getPatientName());
        resp.setVisitDate(record.getVisitDate());
        resp.setPrescriptionNames(record.getPrescriptionNamesSnapshot());
        resp.setNotes(record.getNotes());

        // Deserialize prescription IDs
        try {
            List<Long> prescriptionIds = objectMapper.readValue(
                    record.getPrescriptionIdsJson(),
                    new TypeReference<List<Long>>() {
                    });
            resp.setPrescriptionIds(prescriptionIds);
        } catch (JsonProcessingException e) {
            log.error("Failed to deserialize prescription IDs", e);
            resp.setPrescriptionIds(Collections.emptyList());
        }

        // Deserialize and process merged herbs
        List<MergedHerbDto> mergedHerbs = deserializeMergedHerbs(record.getMergedHerbsJson());
        resp.setMergedHerbs(convertToRespItems(mergedHerbs));
        resp.setMergedHerbsText(buildMergedHerbsText(mergedHerbs));

        return resp;
    }

    private MedicalRecordListItemResp toListItemResp(MedicalRecord record) {
        MedicalRecordListItemResp resp = new MedicalRecordListItemResp();
        resp.setId(record.getId());
        resp.setPatientName(record.getPatientName());
        resp.setVisitDate(record.getVisitDate());
        resp.setPrescriptionNames(record.getPrescriptionNamesSnapshot());
        resp.setNotes(record.getNotes());

        // Deserialize and process merged herbs
        List<MergedHerbDto> mergedHerbs = deserializeMergedHerbs(record.getMergedHerbsJson());
        resp.setMergedHerbs(convertToListRespItems(mergedHerbs));
        resp.setMergedHerbsText(buildMergedHerbsText(mergedHerbs));

        return resp;
    }


    private List<MergedHerbDto> deserializeMergedHerbs(String json) {
        try {
            return objectMapper.readValue(json, new TypeReference<List<MergedHerbDto>>() {
            });
        } catch (JsonProcessingException e) {
            log.error("Failed to deserialize merged herbs", e);
            return Collections.emptyList();
        }
    }

    private List<MedicalRecordResp.MergedHerbItem> convertToRespItems(List<MergedHerbDto> herbs) {
        return herbs.stream()
                .map(h -> {
                    MedicalRecordResp.MergedHerbItem item = new MedicalRecordResp.MergedHerbItem();
                    item.setName(h.getName());
                    item.setDoseG(h.getDoseG().stripTrailingZeros().toPlainString());
                    return item;
                })
                .collect(Collectors.toList());
    }

    private List<MedicalRecordListItemResp.MergedHerbItem> convertToListRespItems(List<MergedHerbDto> herbs) {
        return herbs.stream()
                .map(h -> {
                    MedicalRecordListItemResp.MergedHerbItem item = new MedicalRecordListItemResp.MergedHerbItem();
                    item.setName(h.getName());
                    item.setDoseG(h.getDoseG().stripTrailingZeros().toPlainString());
                    return item;
                })
                .collect(Collectors.toList());
    }

    private String buildMergedHerbsText(List<MergedHerbDto> herbs) {
        return herbs.stream()
                .map(h -> h.getName() + " " + h.getDoseG().stripTrailingZeros().toPlainString() + "g")
                .collect(Collectors.joining(", "));
    }


    // Helper DTO for internal merge processing
    private static class MergedHerbDto {
        private String name;
        private BigDecimal doseG;

        public MergedHerbDto() {
        }

        public MergedHerbDto(String name, BigDecimal doseG) {
            this.name = name;
            this.doseG = doseG;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public BigDecimal getDoseG() {
            return doseG;
        }

        public void setDoseG(BigDecimal doseG) {
            this.doseG = doseG;
        }
    }

    // Helper class for merging herbs
    private static class MergedHerbBuilder {
        private final String name;
        private BigDecimal maxDose = BigDecimal.ZERO;

        MergedHerbBuilder(String name) {
            this.name = name;
        }

        void addDose(BigDecimal dose) {
            if (dose.compareTo(maxDose) > 0) {
                maxDose = dose;
            }
        }

        MergedHerbDto build() {
            return new MergedHerbDto(name, maxDose);
        }
    }
}
