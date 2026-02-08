package com.tcm.prescription.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Positive;
import java.time.LocalDate;
import java.util.List;

@Data
public class MedicalRecordCreateReq {

    @NotBlank(message = "Patient name cannot be blank")
    private String patientName;

    @NotNull(message = "Visit date cannot be null")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate visitDate;

    @NotEmpty(message = "Prescription IDs cannot be empty")
    private List<@Positive(message = "Prescription ID must be positive") Long> prescriptionIds;

    private String notes; // Optional notes
}
