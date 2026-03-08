package com.interview.demo.service;

import com.interview.demo.dto.request.CreateOrderRequest;
import com.interview.demo.dto.response.OrderResponse;
import com.interview.demo.entity.Order;
import com.interview.demo.entity.Product;
import com.interview.demo.entity.User;
import com.interview.demo.enums.PaymentMethod;
import com.interview.demo.exception.InsufficientStockException;
import com.interview.demo.exception.ResourceNotFoundException;
import com.interview.demo.repository.OrderRepository;
import com.interview.demo.repository.ProductRepository;
import com.interview.demo.repository.UserRepository;
import com.interview.demo.service.impl.OrderService;
import com.interview.demo.service.strategy.PaymentContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * OrderService Unit Test
 *
 * Demo @Mock: tất cả dependency đều là mock (không hit DB, không gửi event thật)
 * @InjectMocks: Spring inject các mock vào OrderService
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("OrderService Tests")
class OrderServiceTest {

    // ---- @Mock: object giả hoàn toàn ----
    @Mock private OrderRepository          orderRepository;
    @Mock private UserRepository           userRepository;
    @Mock private ProductRepository        productRepository;
    @Mock private PaymentContext           paymentContext;
    @Mock private ApplicationEventPublisher eventPublisher;

    // ---- @Mock cho SecurityContext ----
    @Mock private SecurityContext  securityContext;
    @Mock private Authentication   authentication;

    // ---- @InjectMocks: Spring inject các mock vào đây ----
    @InjectMocks
    private OrderService orderService;

    private User    testUser;
    private Product testProduct;

    @BeforeEach
    void setUp() {
        // Setup SecurityContext mock
        when(authentication.getName()).thenReturn("john@demo.com");
        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);

        // Test data
        testUser = User.builder()
                .id(1L).email("john@demo.com").fullName("John Doe").build();

        testProduct = Product.builder()
                .id(1L).name("iPhone 15 Pro")
                .price(new BigDecimal("29990000")).stock(10)
                .build();
    }

    @Test
    @DisplayName("createOrder - success case")
    void createOrder_whenValidRequest_shouldReturnOrderResponse() {
        // Arrange
        when(userRepository.findByEmail("john@demo.com")).thenReturn(Optional.of(testUser));
        when(productRepository.findById(1L)).thenReturn(Optional.of(testProduct));
        when(orderRepository.save(any(Order.class))).thenAnswer(inv -> {
            Order o = inv.getArgument(0);
            // Simulate DB: set ID
            return Order.builder()
                    .id(1L).orderCode(o.getOrderCode()).user(o.getUser())
                    .items(o.getItems()).totalAmount(o.getTotalAmount())
                    .status(o.getStatus()).paymentMethod(o.getPaymentMethod())
                    .shippingAddress(o.getShippingAddress()).build();
        });
        when(paymentContext.processPayment(any(), any(), any())).thenReturn("TXN-123");

        // Act
        CreateOrderRequest request = buildOrderRequest(1, 2);
        OrderResponse response = orderService.createOrder(request);

        // Assert
        assertNotNull(response);
        assertEquals(new BigDecimal("59980000"), response.getTotalAmount()); // 29990000 * 2

        // Verify interactions
        verify(orderRepository, times(1)).save(any(Order.class));
        verify(paymentContext, times(1)).processPayment(eq(PaymentMethod.VNPAY), any(), any());
        verify(eventPublisher, times(1)).publishEvent(any()); // Observer fired
    }

    @Test
    @DisplayName("createOrder - user not found → ResourceNotFoundException")
    void createOrder_whenUserNotFound_shouldThrowException() {
        // Arrange
        when(userRepository.findByEmail(any())).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class,
                () -> orderService.createOrder(buildOrderRequest(1, 1)));

        // Verify: không gọi save khi user không tồn tại
        verify(orderRepository, never()).save(any());
    }

    @Test
    @DisplayName("createOrder - insufficient stock → InsufficientStockException")
    void createOrder_whenInsufficientStock_shouldThrowException() {
        // Arrange
        Product lowStockProduct = Product.builder()
                .id(1L).name("Limited Product")
                .price(new BigDecimal("1000000")).stock(1) // chỉ còn 1
                .build();

        when(userRepository.findByEmail(any())).thenReturn(Optional.of(testUser));
        when(productRepository.findById(1L)).thenReturn(Optional.of(lowStockProduct));

        // Act: yêu cầu mua 5, chỉ có 1
        assertThrows(InsufficientStockException.class,
                () -> orderService.createOrder(buildOrderRequest(1, 5)));

        verify(orderRepository, never()).save(any());
    }

    @Test
    @DisplayName("createOrder - product not found → ResourceNotFoundException")
    void createOrder_whenProductNotFound_shouldThrowException() {
        when(userRepository.findByEmail(any())).thenReturn(Optional.of(testUser));
        when(productRepository.findById(99L)).thenReturn(Optional.empty());

        CreateOrderRequest request = buildOrderRequest(99, 1);

        assertThrows(ResourceNotFoundException.class,
                () -> orderService.createOrder(request));
    }

    // ---- Helper ----
    private CreateOrderRequest buildOrderRequest(long productId, int quantity) {
        CreateOrderRequest.OrderItemRequest item = new CreateOrderRequest.OrderItemRequest();
        item.setProductId(productId);
        item.setQuantity(quantity);

        CreateOrderRequest request = new CreateOrderRequest();
        request.setItems(List.of(item));
        request.setShippingAddress("123 Test Street");
        request.setPaymentMethod(PaymentMethod.VNPAY);
        return request;
    }
}
