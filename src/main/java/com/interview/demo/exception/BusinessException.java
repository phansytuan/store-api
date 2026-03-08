package com.interview.demo.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

// ============================================================
// BASE: Unchecked Exception (RuntimeException)
// ============================================================
@Getter
public class BusinessException extends RuntimeException {

    private final String errorCode;
    private final int httpStatus;

    public BusinessException(String errorCode, String message, int httpStatus) {
        super(message);
        this.errorCode  = errorCode;
        this.httpStatus = httpStatus;
    }
}
