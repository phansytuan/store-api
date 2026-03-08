package com.interview.demo.service.strategy;

import com.interview.demo.enums.PaymentMethod;
import com.interview.demo.exception.BusinessException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * STRATEGY PATTERN – Context
 *
 * Spring inject ALL PaymentStrategy implementations vào đây.
 * Map theo PaymentMethod để lookup O(1).
 *
 * Ưu điểm: Thêm phương thức thanh toán mới chỉ cần thêm @Component class mới,
 * không cần sửa code ở đây → Open/Closed Principle.
 */
@Slf4j
@Service
public class PaymentContext {

    private final Map<PaymentMethod, PaymentStrategy> strategyMap;

    // Spring tự inject List<PaymentStrategy> với tất cả implementation
    public PaymentContext(List<PaymentStrategy> strategies) {
        this.strategyMap = strategies.stream()
                .collect(Collectors.toMap(PaymentStrategy::getMethod, s -> s));
        log.info("Loaded payment strategies: {}", strategyMap.keySet());
    }

    public String processPayment(PaymentMethod method, Long orderId, BigDecimal amount) {
        PaymentStrategy strategy = strategyMap.get(method);
        if (strategy == null) {
            throw new BusinessException("PAYMENT_METHOD_NOT_SUPPORTED",
                    "Payment method not supported: " + method, 400);
        }
        return strategy.processPayment(orderId, amount);
    }
}
