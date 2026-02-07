package com.tcm.prescription.dto;

import lombok.Data;

@Data
public class HerbQuery {
    private String keyword;
    private Integer status;
    
    // Pagination (defaults or handle via Pageable in Controller)
    private int page = 1;
    private int size = 20;
}
