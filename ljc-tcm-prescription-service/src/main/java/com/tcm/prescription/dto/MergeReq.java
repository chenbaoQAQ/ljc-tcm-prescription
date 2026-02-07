package com.tcm.prescription.dto;

import lombok.Data;

import javax.validation.constraints.NotEmpty;
import java.util.List;

@Data
public class MergeReq {
    @NotEmpty(message = "Must select at least one prescription")
    private List<Long> prescriptionIds;
}
