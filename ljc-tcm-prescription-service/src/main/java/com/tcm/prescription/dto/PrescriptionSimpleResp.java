package com.tcm.prescription.dto;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class PrescriptionSimpleResp {
    private Long id;
    private String name;
    private int itemCount;
    private LocalDateTime updatedAt;
}
