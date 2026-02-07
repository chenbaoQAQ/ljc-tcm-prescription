package com.tcm.prescription.common;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ErrorCode {

    SUCCESS(0, "Success"),
    PARAM_ERROR(40001, "Parameter Validation Failed"),
    NOT_FOUND(40401, "Resource Not Found"),
    CONFLICT(40901, "Resource Conflict"),
    BUSINESS_ERROR(42201, "Business Rule Violation"),
    INTERNAL_ERROR(50000, "Internal Server Error");

    private final int code;
    private final String message;
}
