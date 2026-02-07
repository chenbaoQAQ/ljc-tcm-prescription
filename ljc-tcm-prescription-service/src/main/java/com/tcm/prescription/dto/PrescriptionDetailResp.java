package com.tcm.prescription.dto;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class PrescriptionDetailResp {
    private Long id;
    private String name;
    private String description;
    private LocalDateTime updatedAt;
    private List<ItemResp> items;

    @Data
    public static class ItemResp {
        private Long herbId;
        private String herbNameSnapshot;
        private BigDecimal doseG;
        private Integer sortOrder;
    }
}
