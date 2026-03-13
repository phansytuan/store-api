package com.interview.demo.controller;

import com.interview.demo.dto.request.CreateOrderRequest;
import com.interview.demo.dto.response.ApiResponse;
import com.interview.demo.dto.response.OrderResponse;
import com.interview.demo.enums.OrderStatus;
import com.interview.demo.service.impl.OrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * OrderController
 *
 * PathParam: /orders/{orderId}          → xác định đơn hàng cụ thể
 * QueryParam: ?status=PENDING &page=0   → filter + pagination
 */
@Tag(name = "Orders", description = "Order management (requires authentication)")
@SecurityRequirement(name = "bearerAuth")
@RestController
@RequestMapping("/api/v1/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    /**
     * POST /orders
     * Tạo đơn hàng mới – body chứa items, địa chỉ, payment method
     */
    @Operation(summary = "Create new order")
    @PostMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<OrderResponse>> createOrder(
            @Valid @RequestBody CreateOrderRequest request) {
        OrderResponse order = orderService.createOrder(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok(order, "Order created successfully"));
    }

    /**
     * GET /orders ?status=PENDING &page=0 &size=10
     *
     * QueryParam: status (optional filter), page/size (pagination)
     */
    @Operation(summary = "Get my orders")
    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<Page<OrderResponse>>> getMyOrders(
            @Parameter(description = "Filter by status")
            @RequestParam(required = false) OrderStatus status,

            @RequestParam(defaultValue = "0")  int page,
            @RequestParam(defaultValue = "10") int size) {

        Page<OrderResponse> orders = orderService.getMyOrders(status,
                PageRequest.of(page, size, Sort.by("createdAt").descending()));
        return ResponseEntity.ok(ApiResponse.ok(orders));
    }

    /**
     * GET /orders/{orderId}
     *
     * PathParam: orderId – bắt buộc để xác định đơn hàng cụ thể
     */
    @Operation(summary = "Get order by ID")
    @GetMapping("/{orderId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<OrderResponse>> getById(
            @Parameter(description = "Order ID")
            @PathVariable Long orderId) {
        return ResponseEntity.ok(ApiResponse.ok(orderService.getById(orderId)));
    }

    /**
     * Admin: thống kê đơn hàng theo status
     */
    @Operation(summary = "Order stats by status (Admin only)")
    @GetMapping("/admin/stats")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Map<OrderStatus, Long>>> getStats() {
        return ResponseEntity.ok(ApiResponse.ok(orderService.getOrderStats()));
    }
}
