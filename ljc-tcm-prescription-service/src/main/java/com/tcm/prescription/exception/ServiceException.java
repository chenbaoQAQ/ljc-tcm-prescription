package com.tcm.prescription.exception;

import com.tcm.prescription.common.ErrorCode;
import lombok.Getter;

@Getter
public class ServiceException extends RuntimeException {

    private final int code;

    public ServiceException(ErrorCode errorCode) {
        super(errorCode.getMessage());
        this.code = errorCode.getCode();
    }

    public ServiceException(int code, String message) {
        super(message);
        this.code = code;
    }
}
