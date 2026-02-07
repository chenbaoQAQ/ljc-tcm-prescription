package com.tcm.prescription.dto;

import lombok.Data;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Size;
import java.util.List;

@Data
public class PrescriptionCreateReq {

    @NotBlank(message = "Prescription name is required")
    @Size(max = 128, message = "Name too long")
    private String name;

    @Size(max = 255, message = "Description too long")
    private String description;

    @NotEmpty(message = "Prescription must contain at least one item")
    @Valid
    private List<PrescriptionItemDto> items;
}
