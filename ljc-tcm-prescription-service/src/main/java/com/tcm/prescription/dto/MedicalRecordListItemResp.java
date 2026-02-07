package com.tcm.prescription.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDate;
import java.util.List;

@Data
public class MedicalRecordListItemResp {

    private Long id;
    private String patientName;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate visitDate;

    private String prescriptionNames;
    private String mergedHerbsText;
    
    // Optional: for flexible frontend display
    private List<MergedHerbItem> mergedHerbs;

    @Data
    public static class MergedHerbItem {
        private String name;
        private String doseG;
    }
}
