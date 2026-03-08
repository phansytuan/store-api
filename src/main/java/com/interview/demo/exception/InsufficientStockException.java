package com.interview.demo.exception;

public class InsufficientStockException extends BusinessException {
    public InsufficientStockException(String productName, int requested, int available) {
        super("INSUFFICIENT_STOCK",
              String.format("Product '%s': requested %d but only %d available", productName, requested, available),
              422);
    }
}
