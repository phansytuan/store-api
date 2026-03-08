package com.interview.demo.service.strategy;

import com.interview.demo.enums.PaymentMethod;

/**
 * STRATEGY PATTERN – Payment Processing
 *
 * Mỗi phương thức thanh toán là một strategy riêng biệt.
 * Khi thêm phương thức mới, chỉ cần thêm class mới (Open/Closed Principle).
 */
public interface PaymentStrategy {

    /**
     * Xử lý thanh toán
     * @param orderId   ID đơn hàng
     * @param amount    Số tiền
     * @return transactionId
     */
    String processPayment(Long orderId, java.math.BigDecimal amount);

    PaymentMethod getMethod();
}
