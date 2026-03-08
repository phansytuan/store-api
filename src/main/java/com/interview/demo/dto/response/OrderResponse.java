package com.interview.demo.dto.response;

import com.interview.demo.enums.OrderStatus;
import com.interview.demo.enums.PaymentMethod;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class OrderResponse {
    private Long            id;
    private String          orderCode;
    private OrderStatus     status;
    private PaymentMethod   paymentMethod;
    private BigDecimal      totalAmount;
    private String          shippingAddress;
    private String          note;
    private LocalDateTime   createdAt;
    private List<OrderItemResponse> items;

    @Data
    @Builder
    public static class OrderItemResponse {
        private Long       productId;
        private String     productName;
        private int        quantity;
        private BigDecimal unitPrice;
        private BigDecimal subtotal;
    }
}
