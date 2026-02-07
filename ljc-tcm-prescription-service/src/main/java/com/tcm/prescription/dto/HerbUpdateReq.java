package com.tcm.prescription.dto;

import lombok.Data;

import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.Size;
import java.math.BigDecimal;

@Data
public class HerbUpdateReq {
    
    @Size(max = 64, message = "Name too long")
    private String nameCn; // Can be updated, must be unique if changed

    @DecimalMin(value = "0.0", inclusive = false, message = "Dose must be > 0")
    private BigDecimal defaultDoseG;

    @Size(max = 255, message = "Notes too long")
    private String notes;

    private Integer status; // 0 or 1
}
