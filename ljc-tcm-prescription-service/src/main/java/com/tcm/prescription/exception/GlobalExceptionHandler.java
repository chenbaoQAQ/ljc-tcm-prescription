package com.tcm.prescription.exception;

import com.tcm.prescription.common.ErrorCode;
import com.tcm.prescription.common.Result;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.validation.BindException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.UUID;
import javax.servlet.http.HttpServletRequest;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ServiceException.class)
    public Result<Void> handleServiceException(ServiceException e) {
        log.warn("Service Exception: code={}, msg={}", e.getCode(), e.getMessage());
        return Result.failure(e.getCode(), e.getMessage());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public Result<Void> handleValidationException(MethodArgumentNotValidException e) {
        String msg = e.getBindingResult().getAllErrors().get(0).getDefaultMessage();
        log.warn("Validation Exception: {}", msg);
        return Result.failure(ErrorCode.PARAM_ERROR.getCode(), msg);
    }
    
    @ExceptionHandler(BindException.class)
    public Result<Void> handleBindException(BindException e) {
        String msg = e.getAllErrors().get(0).getDefaultMessage();
        log.warn("Bind Exception: {}", msg);
        return Result.failure(ErrorCode.PARAM_ERROR.getCode(), msg);
    }

    @ExceptionHandler(Exception.class)
    public Result<Void> handleException(Exception e) {
        log.error("Internal Server Error", e);
        return Result.failure(ErrorCode.INTERNAL_ERROR.getCode(), "Internal Server Error");
    }
}
