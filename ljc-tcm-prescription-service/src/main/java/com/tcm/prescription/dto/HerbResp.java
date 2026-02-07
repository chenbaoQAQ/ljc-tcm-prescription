package com.tcm.prescription.dto;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class HerbResp {
    private Long id;
    private String nameCn;
    private String unit;
    private BigDecimal defaultDoseG;
    private String notes;
    private Integer status;
    private LocalDateTime updatedAt;
}
