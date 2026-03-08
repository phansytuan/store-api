package com.interview.demo.service.impl;

import com.interview.demo.dto.request.CreateOrderRequest;
import com.interview.demo.dto.response.OrderResponse;
import com.interview.demo.entity.Order;
import com.interview.demo.entity.OrderItem;
import com.interview.demo.entity.Product;
import com.interview.demo.entity.User;
import com.interview.demo.enums.OrderStatus;
import com.interview.demo.event.OrderCreatedEvent;
import com.interview.demo.exception.InsufficientStockException;
import com.interview.demo.exception.ResourceNotFoundException;
import com.interview.demo.repository.OrderRepository;
import com.interview.demo.repository.ProductRepository;
import com.interview.demo.repository.UserRepository;
import com.interview.demo.service.strategy.PaymentContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * OrderService – tổng hợp tất cả kiến thức:
 * - IoC / DI (Constructor injection)
 * - Strategy Pattern (PaymentContext)
 * - Observer Pattern (ApplicationEventPublisher)
 * - Stream API
 * - Exception handling (Unchecked)
 * - Bean: Singleton, @Transactional
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class OrderService {

    // Constructor Injection – IoC Demo
    private final OrderRepository          orderRepository;
    private final UserRepository           userRepository;
    private final ProductRepository        productRepository;
    private final PaymentContext           paymentContext;       // Strategy Pattern
    private final ApplicationEventPublisher eventPublisher;      // Observer Pattern

    /**
     * Tạo đơn hàng – luồng chính
     */
    @Transactional
    public OrderResponse createOrder(CreateOrderRequest request) {
        // 1. Lấy user đang đăng nhập từ SecurityContext
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        // 2. Validate và tạo order items (Stream + Exception)
        List<OrderItem> items = request.getItems().stream()
                .map(this::validateAndBuildItem)           // Intermediate
                .collect(Collectors.toList());             // Terminal

        // 3. Tính tổng tiền (Stream - reduce)
        BigDecimal total = items.stream()
                .map(item -> item.getUnitPrice()
                        .multiply(BigDecimal.valueOf(item.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add); // Terminal

        // 4. Tạo Order (Builder Pattern)
        Order order = Order.builder()
                .orderCode(generateOrderCode())
                .user(user)
                .paymentMethod(request.getPaymentMethod())
                .shippingAddress(request.getShippingAddress())
                .note(request.getNote())
                .totalAmount(total)
                .status(OrderStatus.PENDING)
                .build();

        // Set bidirectional relationship
        items.forEach(item -> item.setOrder(order));
        order.getItems().addAll(items);

        Order saved = orderRepository.save(order);

        // 5. Strategy Pattern – xử lý thanh toán
        String transactionId = paymentContext.processPayment(
                request.getPaymentMethod(), saved.getId(), total);
        log.info("Payment processed: transactionId={}", transactionId);

        // 6. Observer Pattern – publish event (async)
        eventPublisher.publishEvent(new OrderCreatedEvent(this, saved, user));

        return toResponse(saved);
    }

    /**
     * Lấy orders của user đang đăng nhập
     * Demo: PathParam (userId implicit) + QueryParam (status, pageable)
     */
    @Transactional(readOnly = true)
    public Page<OrderResponse> getMyOrders(OrderStatus status, Pageable pageable) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        Page<Order> orders = (status != null)
                ? orderRepository.findByUserIdAndStatus(user.getId(), status, pageable)
                : orderRepository.findByUserId(user.getId(), pageable);

        return orders.map(this::toResponse);
    }

    /**
     * Lấy order theo ID (PathParam demo)
     */
    @Transactional(readOnly = true)
    public OrderResponse getById(Long orderId) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        return orderRepository.findByIdAndUserId(orderId, user.getId())
                .map(this::toResponse)
                .orElseThrow(() -> new ResourceNotFoundException("Order", orderId));
    }

    /**
     * Thống kê (Admin) – Stream + Map demo
     */
    @Transactional(readOnly = true)
    public Map<OrderStatus, Long> getOrderStats() {
        return orderRepository.findAll().stream()
                .collect(Collectors.groupingBy(Order::getStatus, Collectors.counting()));
    }

    // ---- Private helpers ----

    private OrderItem validateAndBuildItem(CreateOrderRequest.OrderItemRequest req) {
        Product product = productRepository.findById(req.getProductId())
                .orElseThrow(() -> new ResourceNotFoundException("Product", req.getProductId()));

        // Unchecked Exception: business rule
        if (product.getStock() < req.getQuantity()) {
            throw new InsufficientStockException(product.getName(), req.getQuantity(), product.getStock());
        }

        return OrderItem.builder()
                .product(product)
                .quantity(req.getQuantity())
                .unitPrice(product.getPrice())
                .build();
    }

    private String generateOrderCode() {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        return "ORD-" + timestamp;
    }

    private OrderResponse toResponse(Order order) {
        List<OrderResponse.OrderItemResponse> itemResponses = order.getItems().stream()
                .map(item -> OrderResponse.OrderItemResponse.builder()
                        .productId(item.getProduct().getId())
                        .productName(item.getProduct().getName())
                        .quantity(item.getQuantity())
                        .unitPrice(item.getUnitPrice())
                        .subtotal(item.getUnitPrice().multiply(BigDecimal.valueOf(item.getQuantity())))
                        .build())
                .collect(Collectors.toList());

        return OrderResponse.builder()
                .id(order.getId())
                .orderCode(order.getOrderCode())
                .status(order.getStatus())
                .paymentMethod(order.getPaymentMethod())
                .totalAmount(order.getTotalAmount())
                .shippingAddress(order.getShippingAddress())
                .note(order.getNote())
                .createdAt(order.getCreatedAt())
                .items(itemResponses)
                .build();
    }
}
