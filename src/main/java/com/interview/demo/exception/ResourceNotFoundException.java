package com.interview.demo.exception;

public class ResourceNotFoundException extends BusinessException {
    public ResourceNotFoundException(String resource, Long id) {
        super("RESOURCE_NOT_FOUND", resource + " not found with id: " + id, 404);
    }
    public ResourceNotFoundException(String message) {
        super("RESOURCE_NOT_FOUND", message, 404);
    }
}
