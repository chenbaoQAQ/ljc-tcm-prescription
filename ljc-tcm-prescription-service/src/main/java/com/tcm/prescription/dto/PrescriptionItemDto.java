package com.tcm.prescription.dto;

import lombok.Data;

import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;

@Data
public class PrescriptionItemDto {

    @NotNull(message = "Herb ID is required")
    private Long herbId;

    @NotNull(message = "Dose is required")
    @DecimalMin(value = "0.01", message = "Dose must be > 0", inclusive = true)
    private BigDecimal doseG;

    private Integer sortOrder;
}
