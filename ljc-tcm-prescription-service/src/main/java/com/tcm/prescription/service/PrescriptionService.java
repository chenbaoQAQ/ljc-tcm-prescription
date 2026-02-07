package com.tcm.prescription.service;

import com.tcm.prescription.common.ErrorCode;
import com.tcm.prescription.dto.*;
import com.tcm.prescription.entity.Herb;
import com.tcm.prescription.entity.Prescription;
import com.tcm.prescription.entity.PrescriptionItem;
import com.tcm.prescription.exception.ServiceException;
import com.tcm.prescription.repository.HerbRepository;
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
public class PrescriptionService {

    private final PrescriptionRepository prescriptionRepository;
    private final HerbRepository herbRepository;
    private final PrescriptionItemRepository prescriptionItemRepository;

    @Transactional
    public PrescriptionDetailResp create(PrescriptionCreateReq req) {
        validateItems(req.getItems());

        Prescription prescription = new Prescription();
        prescription.setName(req.getName());
        prescription.setDescription(req.getDescription());
        
        // Save items
        List<PrescriptionItem> items = buildItems(req.getItems(), prescription);
        prescription.setItems(items); // Cascade save

        prescription = prescriptionRepository.save(prescription);
        return toDetailResp(prescription);
    }

    @Transactional
    public PrescriptionDetailResp update(Long id, PrescriptionCreateReq req) {
        Prescription prescription = getById(id);
        
        validateItems(req.getItems());

        prescription.setName(req.getName());
        prescription.setDescription(req.getDescription());
        
        // Clear old items and add new ones (Soft delete is handled by orphanRemoval=true + manual Logic if needed, but here simple replacement)
        // Note: For true soft delete of items, we should mark them deleted. 
        // orphanRemoval=true will HARD delete unless we intercept. 
        // Given the requirement "Update: Soft delete old items", we should manually handle it to ensure history is kept if needed or just deletedAt set.
        // But since we use @Where, retrieving the list only gives active ones.
        // Standard orphanRemoval deletes physically. 
        // Let's implement manual soft delete for old items.
        
        List<PrescriptionItem> oldItems = prescription.getItems();
        for (PrescriptionItem item : oldItems) {
            item.setDeletedAt(LocalDateTime.now());
        }
        // We can't just clear the list if we want to keep them in DB as deleted. 
        // But here we are replacing logic.
        // Actually, easiest valid way:
        // 1. Mark all current items as deleted.
        // 2. Add new items as NEW records.
        // This keeps history.
        
        // We need to NOT remove them from the list if relying on CascadeType.ALL to save the deletion status?
        // No, if we want to soft delete, we update them. 
        // But the relationship collection `items` in entity only shows non-deleted ones due to @Where.
        // So `prescription.getItems()` returns active items.
        // We mark them deleted. 
        // Then we add NEW items.
        
        List<PrescriptionItem> newItems = buildItems(req.getItems(), prescription);
        prescription.getItems().addAll(newItems); 
        
        // The old items are still in the list, just marked deleted. 
        // When we save prescription, it saves all. 
        // But wait, @Where on the OneToMany collection might prevent us from seeing deleted ones, which is fine.
        // But does it prevent saving? No.
        
        return toDetailResp(prescriptionRepository.save(prescription));
    }
    
    // Check duplication and validity
    private void validateItems(List<PrescriptionItemDto> items) {
        Set<Long> herbIds = new HashSet<>();
        for (PrescriptionItemDto item : items) {
            if (!herbIds.add(item.getHerbId())) {
                throw new ServiceException(ErrorCode.PARAM_ERROR.getCode(), "Duplicate herb ID: " + item.getHerbId());
            }
        }
    }

    private List<PrescriptionItem> buildItems(List<PrescriptionItemDto> itemDtos, Prescription prescription) {
        List<PrescriptionItem> items = new ArrayList<>();
        for (PrescriptionItemDto dto : itemDtos) {
            Herb herb = herbRepository.findById(dto.getHerbId())
                    .orElseThrow(() -> new ServiceException(ErrorCode.NOT_FOUND.getCode(), "Herb not found: " + dto.getHerbId()));
            
            if (herb.getStatus() == 0) {
                 throw new ServiceException(ErrorCode.BUSINESS_ERROR.getCode(), "Herb is disabled: " + herb.getNameCn());
            }

            PrescriptionItem item = new PrescriptionItem();
            item.setPrescription(prescription);
            item.setHerb(herb);
            item.setHerbNameSnapshot(herb.getNameCn());
            item.setDoseG(dto.getDoseG());
            item.setSortOrder(dto.getSortOrder() != null ? dto.getSortOrder() : 0);
            items.add(item);
        }
        return items;
    }

    public Page<PrescriptionSimpleResp> list(String keyword, int page, int size) {
        Specification<Prescription> spec = (root, q, cb) -> {
            if (StringUtils.hasText(keyword)) {
                return cb.like(root.get("name"), "%" + keyword + "%");
            }
            return null;
        };
        Page<Prescription> p = prescriptionRepository.findAll(spec, PageRequest.of(page - 1, size, Sort.by(Sort.Direction.DESC, "id")));
        return p.map(e -> {
            PrescriptionSimpleResp resp = new PrescriptionSimpleResp();
            resp.setId(e.getId());
            resp.setName(e.getName());
            resp.setItemCount(e.getItems().size());
            resp.setUpdatedAt(e.getUpdatedAt());
            return resp;
        });
    }

    public PrescriptionDetailResp getDetail(Long id) {
        return toDetailResp(getById(id));
    }

    private Prescription getById(Long id) {
        return prescriptionRepository.findById(id)
                .orElseThrow(() -> new ServiceException(ErrorCode.NOT_FOUND.getCode(), "Prescription not found"));
    }

    @Transactional
    public void delete(Long id) {
        Prescription p = getById(id);
        LocalDateTime now = LocalDateTime.now();
        p.setDeletedAt(now);
        for (PrescriptionItem item : p.getItems()) {
            item.setDeletedAt(now);
        }
        prescriptionRepository.save(p);
    }

    public MergeResp merge(MergeReq req) {
        List<Long> ids = req.getPrescriptionIds();
        log.info("Merging prescriptions: {}", ids);
        
        List<Prescription> prescriptions = prescriptionRepository.findAllByIds(ids);
        if (prescriptions.size() != ids.size()) {
            throw new ServiceException(ErrorCode.NOT_FOUND.getCode(), "Some prescriptions not found or deleted");
        }

        // Fetch all items
        // Since we have prescriptions, we can get items from them directly (lazy loaded) or use batch fetch.
        // Batch fetch is better for performance.
        List<PrescriptionItem> allItems = prescriptionItemRepository.findByPrescriptionIds(ids);

        // Group by Herb
        Map<Long, MergedItemBuilder> map = new HashMap<>();
        
        for (PrescriptionItem item : allItems) {
            map.computeIfAbsent(item.getHerb().getId(), k -> new MergedItemBuilder(item.getHerb().getId(), item.getHerbNameSnapshot()))
               .addSource(item.getPrescription().getId(), item.getDoseG());
        }

        List<MergeResp.MergedItem> resultItems = map.values().stream()
                .map(MergedItemBuilder::build)
                .sorted(Comparator.comparing(MergeResp.MergedItem::getName)) // Natural order sort
                .collect(Collectors.toList());

        MergeResp resp = new MergeResp();
        resp.setItems(resultItems);
        
        log.info("Merged {} prescriptions into {} herbs", ids.size(), resultItems.size());
        
        return resp;
    }
    
    private PrescriptionDetailResp toDetailResp(Prescription p) {
        PrescriptionDetailResp resp = new PrescriptionDetailResp();
        resp.setId(p.getId());
        resp.setName(p.getName());
        resp.setDescription(p.getDescription());
        resp.setUpdatedAt(p.getUpdatedAt());
        resp.setItems(p.getItems().stream().map(item -> {
            PrescriptionDetailResp.ItemResp i = new PrescriptionDetailResp.ItemResp();
            i.setHerbId(item.getHerb().getId());
            i.setHerbNameSnapshot(item.getHerbNameSnapshot());
            i.setDoseG(item.getDoseG());
            i.setSortOrder(item.getSortOrder());
            return i;
        }).collect(Collectors.toList()));
        return resp;
    }
    
    // Helper class for merging
    private static class MergedItemBuilder {
        Long herbId;
        String name;
        BigDecimal maxDose = BigDecimal.ZERO;
        List<MergeResp.Source> sources = new ArrayList<>();

        MergedItemBuilder(Long herbId, String name) {
            this.herbId = herbId;
            this.name = name;
        }

        void addSource(Long pid, BigDecimal dose) {
            if (dose.compareTo(maxDose) > 0) {
                maxDose = dose;
            }
            MergeResp.Source s = MergeResp.Source.builder().prescriptionId(pid).doseG(dose).build();
            sources.add(s);
        }

        MergeResp.MergedItem build() {
            return MergeResp.MergedItem.builder()
                    .herbId(herbId)
                    .name(name)
                    .doseG(maxDose)
                    .sources(sources)
                    .build();
        }
    }
}
