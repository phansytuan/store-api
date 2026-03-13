package com.interview.demo.service.impl;

import com.interview.demo.dto.response.ProductResponse;
import com.interview.demo.entity.Product;
import com.interview.demo.exception.ResourceNotFoundException;
import com.interview.demo.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

/**
 * ProductService
 *
 * Demo: Collections, Stream API (Intermediate & Terminal operations)
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;

    // ---- CRUD ----

    public Page<ProductResponse> getProducts(String keyword, String category, Pageable pageable) {
        Page<Product> products = productRepository.search(keyword, category, pageable);
        // Stream: Intermediate(map) + Terminal(collect) via Page.map
        return products.map(this::toResponse);
    }

    public ProductResponse getById(Long id) {
        return productRepository.findById(id)
                .map(this::toResponse)
                .orElseThrow(() -> new ResourceNotFoundException("Product", id));
    }

    // ---- Stream API Demo ----

    /**
     * Lấy tên category không trùng lặp, sắp xếp theo alphabet.
     * Demo: Intermediate(map, filter, distinct, sorted) + Terminal(collect)
     */
    public List<String> getAllCategories() {
        return productRepository.findAll().stream()
                // Intermediate operations (lazy – chưa thực thi)
                .map(Product::getCategory)           // transform Product → String
                .filter(Objects::nonNull)            // loại null
                .distinct()                          // loại trùng
                .sorted()                            // sắp xếp A-Z
                // Terminal operation (eager – kích hoạt pipeline)
                .collect(Collectors.toList());
    }

    /**
     * Thống kê sản phẩm theo category.
     * Demo: groupingBy, counting, Stream phức tạp
     */
    public Map<String, Long> countByCategory() {
        return productRepository.findAll().stream()
                .filter(p -> p.getCategory() != null)
                .collect(Collectors.groupingBy(
                        Product::getCategory,    // key
                        Collectors.counting()    // value = count
                ));
    }

    /**
     * Lấy top N sản phẩm rẻ nhất còn hàng.
     * Demo: filter, sorted, limit
     */
    public List<ProductResponse> getCheapestInStock(int limit) {
        return productRepository.findAll().stream()
                .filter(p -> p.getStock() > 0)                                    // Intermediate
                .sorted(Comparator.comparing(Product::getPrice))                  // Intermediate
                .limit(limit)                                                     // Intermediate
                .map(this::toResponse)                                            // Intermediate
                .collect(Collectors.toList());                                    // Terminal
    }

    /**
     * Tính tổng giá trị tồn kho.
     * Demo: mapToDouble, reduce/sum
     */
    public BigDecimal getTotalInventoryValue() {
        return productRepository.findAll().stream()
                .map(p -> p.getPrice().multiply(BigDecimal.valueOf(p.getStock())))
                .reduce(BigDecimal.ZERO, BigDecimal::add); // Terminal: reduce
    }

    /**
     * Kiểm tra còn sản phẩm trong kho không.
     * Demo: anyMatch / allMatch / noneMatch (Terminal)
     */
    public boolean hasProductsInStock() {
        return productRepository.findAll().stream()
                .anyMatch(p -> p.getStock() > 0); // Terminal: anyMatch
    }

    /**
     * Demo Collections sử dụng trong thực tế.
     */
    public Map<String, Object> getCollectionDemo() {
        List<Product> all = productRepository.findAll();

        // List: maintain insertion order, allow duplicates
        List<String> names = all.stream()
                .map(Product::getName)
                .collect(Collectors.toList());

        // Set: unique categories, no order
        Set<String> categories = all.stream()
                .map(Product::getCategory)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet()); // HashSet

        // LinkedHashSet: unique + insertion order
        Set<String> orderedCategories = all.stream()
                .map(Product::getCategory)
                .filter(Objects::nonNull)
                .collect(Collectors.toCollection(LinkedHashSet::new));

        // Map: id → product lookup O(1)
        Map<Long, Product> productMap = all.stream()
                .collect(Collectors.toMap(Product::getId, p -> p));

        // LinkedHashMap: keeps insertion order (for ordered JSON response)
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("total", all.size());
        result.put("categories", orderedCategories);
        result.put("names_sample", names.stream().limit(5).collect(Collectors.toList()));
        return result;
    }

    // ---- Mapper ----

    private ProductResponse toResponse(Product p) {
        return ProductResponse.builder()
                .id(p.getId())
                .name(p.getName())
                .description(p.getDescription())
                .price(p.getPrice())
                .stock(p.getStock())
                .category(p.getCategory())
                .build();
    }
}
