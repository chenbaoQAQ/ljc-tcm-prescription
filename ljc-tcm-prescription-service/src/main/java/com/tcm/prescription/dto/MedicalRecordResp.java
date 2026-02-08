package com.tcm.prescription.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDate;
import java.util.List;

@Data
public class MedicalRecordResp {

    private Long id;
    private String patientName;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate visitDate;

    private List<Long> prescriptionIds;
    private String prescriptionNames;
    
    private List<MergedHerbItem> mergedHerbs;
    private String mergedHerbsText;
    private String notes;

    @Data
    public static class MergedHerbItem {
        private String name;
        private String doseG;
    }
}
