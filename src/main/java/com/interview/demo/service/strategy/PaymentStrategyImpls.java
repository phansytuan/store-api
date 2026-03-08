package com.interview.demo.service.strategy;

import com.interview.demo.enums.PaymentMethod;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.UUID;

// ---- CASH ----
@Slf4j
@Component
class CashPaymentStrategy implements PaymentStrategy {

    @Override
    public String processPayment(Long orderId, BigDecimal amount) {
        log.info("[CASH] Processing payment for order #{}: {} VND", orderId, amount);
        // Thực tế: không cần call API, chỉ ghi nhận
        return "CASH-" + orderId;
    }

    @Override
    public PaymentMethod getMethod() { return PaymentMethod.CASH; }
}

// ---- VNPAY ----
@Slf4j
@Component
class VnPayStrategy implements PaymentStrategy {

    @Override
    public String processPayment(Long orderId, BigDecimal amount) {
        log.info("[VNPAY] Calling VNPay API for order #{}: {} VND", orderId, amount);
        // Thực tế: gọi VNPay API, trả về payment URL hoặc transaction ID
        return "VNPAY-TXN-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }

    @Override
    public PaymentMethod getMethod() { return PaymentMethod.VNPAY; }
}

// ---- MOMO ----
@Slf4j
@Component
class MomoStrategy implements PaymentStrategy {

    @Override
    public String processPayment(Long orderId, BigDecimal amount) {
        log.info("[MOMO] Calling MoMo API for order #{}: {} VND", orderId, amount);
        return "MOMO-TXN-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }

    @Override
    public PaymentMethod getMethod() { return PaymentMethod.MOMO; }
}

// ---- BANK TRANSFER ----
@Slf4j
@Component
class BankTransferStrategy implements PaymentStrategy {

    @Override
    public String processPayment(Long orderId, BigDecimal amount) {
        log.info("[BANK] Creating bank transfer instruction for order #{}: {} VND", orderId, amount);
        return "BANK-REF-" + orderId;
    }

    @Override
    public PaymentMethod getMethod() { return PaymentMethod.BANK_TRANSFER; }
}
