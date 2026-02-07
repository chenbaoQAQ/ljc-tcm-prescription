package com.tcm.prescription.common;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;
import org.slf4j.MDC;

import java.io.Serializable;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Result<T> implements Serializable {

    private int code;
    private String message;
    private T data;
    private String traceId;

    public static <T> Result<T> success(T data) {
        return build(0, "success", data);
    }

    public static <T> Result<T> failure(int code, String message) {
        return build(code, message, null);
    }
    
    public static <T> Result<T> failure(ErrorCode errorCode) {
        return build(errorCode.getCode(), errorCode.getMessage(), null);
    }

    private static <T> Result<T> build(int code, String message, T data) {
        Result<T> result = new Result<>();
        result.setCode(code);
        result.setMessage(message);
        result.setData(data);
        result.setTraceId(MDC.get("traceId")); // Or generate a random one if not using tracing middleware
        return result;
    }
}
