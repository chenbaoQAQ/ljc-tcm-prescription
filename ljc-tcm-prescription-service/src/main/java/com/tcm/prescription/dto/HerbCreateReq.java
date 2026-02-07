package com.tcm.prescription.dto;

import lombok.Data;

import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import java.math.BigDecimal;

@Data
public class HerbCreateReq {

    @NotBlank(message = "Name cannot be empty")
    @Size(max = 64, message = "Name too long")
    private String nameCn;

    @DecimalMin(value = "0.0", inclusive = false, message = "Dose must be > 0 if provided")
    private BigDecimal defaultDoseG;

    @Size(max = 255, message = "Notes too long")
    private String notes;
}
