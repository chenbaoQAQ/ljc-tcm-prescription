package com.tcm.prescription.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
public class MergeResp {
    
    private List<MergedItem> items;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MergedItem {
        private Long herbId;
        private String name;
        private BigDecimal doseG;
        private List<Source> sources;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Source {
        private Long prescriptionId;
        private BigDecimal doseG;
    }
}
