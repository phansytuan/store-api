package com.interview.demo.exception;

public class DuplicateEmailException extends BusinessException {
    public DuplicateEmailException(String email) {
        super("EMAIL_DUPLICATE", "Email already exists: " + email, 409);
    }
}
