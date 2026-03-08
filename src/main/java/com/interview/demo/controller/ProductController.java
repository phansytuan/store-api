package com.interview.demo.controller;

import com.interview.demo.dto.response.ApiResponse;
import com.interview.demo.dto.response.ProductResponse;
import com.interview.demo.service.impl.ProductService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * ProductController
 *
 * Demo PathParam vs QueryParam:
 * - PathParam: /products/{id}      → xác định resource cụ thể, bắt buộc
 * - QueryParam: ?keyword=&category= → filter, sort, pagination
 */
@Tag(name = "Products", description = "Product management APIs")
@RestController
@RequestMapping("/api/v1/products")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;

    /**
     * GET /products? keyword=phone& category=electronics& page=0& size=10& sort=price,asc
     *
     * QueryParam: tất cả là filter/pagination – không bắt buộc
     */
    @Operation(summary = "Search products with filters (public)")
    @GetMapping
    public ResponseEntity<ApiResponse<Page<ProductResponse>>> getProducts(
            @Parameter(description = "Search keyword")
            @RequestParam(required = false) String keyword,

            @Parameter(description = "Filter by category")
            @RequestParam(required = false) String category,

            @Parameter(description = "Page number (0-based)")
            @RequestParam(defaultValue = "0") int page,

            @Parameter(description = "Page size")
            @RequestParam(defaultValue = "10") int size,

            @Parameter(description = "Sort field")
            @RequestParam(defaultValue = "id") String sort,

            @Parameter(description = "Sort direction: asc or desc")
            @RequestParam(defaultValue = "asc") String direction) {

        Sort.Direction dir = direction.equalsIgnoreCase("desc") ? Sort.Direction.DESC : Sort.Direction.ASC;
        Page<ProductResponse> products = productService.getProducts(keyword, category,
                PageRequest.of(page, size, Sort.by(dir, sort)));
        return ResponseEntity.ok(ApiResponse.ok(products));
    }

    /**
     * GET /products/{id}
     *
     * PathParam: id bắt buộc để xác định product cụ thể
     * Nếu không có id → endpoint không có ý nghĩa
     */
    @Operation(summary = "Get product by ID (public)")
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<ProductResponse>> getById(
            @Parameter(description = "Product ID")
            @PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.ok(productService.getById(id)));
    }

    @Operation(summary = "Get all categories (public)")
    @GetMapping("/categories")
    public ResponseEntity<ApiResponse<List<String>>> getCategories() {
        return ResponseEntity.ok(ApiResponse.ok(productService.getAllCategories()));
    }

    @Operation(summary = "Get products count by category (public)")
    @GetMapping("/stats/by-category")
    public ResponseEntity<ApiResponse<Map<String, Long>>> getStatsByCategory() {
        return ResponseEntity.ok(ApiResponse.ok(productService.countByCategory()));
    }

    @Operation(summary = "Get cheapest N products in stock (public)")
    @GetMapping("/cheapest")
    public ResponseEntity<ApiResponse<List<ProductResponse>>> getCheapest(
            @RequestParam(defaultValue = "5") int limit) {
        return ResponseEntity.ok(ApiResponse.ok(productService.getCheapestInStock(limit)));
    }

    @Operation(summary = "Collections & Stream demo (public)")
    @GetMapping("/demo/collections")
    public ResponseEntity<ApiResponse<Map<String, Object>>> collectionsDemo() {
        return ResponseEntity.ok(ApiResponse.ok(productService.getCollectionDemo()));
    }

    @Operation(summary = "Total inventory value (Admin only)")
    @GetMapping("/admin/inventory-value")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<BigDecimal>> getInventoryValue() {
        return ResponseEntity.ok(ApiResponse.ok(productService.getTotalInventoryValue()));
    }
}
