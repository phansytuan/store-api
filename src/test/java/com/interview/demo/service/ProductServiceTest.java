package com.interview.demo.service;

import com.interview.demo.dto.response.ProductResponse;
import com.interview.demo.entity.Product;
import com.interview.demo.repository.ProductRepository;
import com.interview.demo.service.impl.ProductService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * ProductService Unit Test
 *
 * Demo @Mock vs @Spy:
 * - @Mock: ProductRepository – không hit DB thật
 * - @Spy: ProductService (nếu muốn override 1 method, giữ logic thật)
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("ProductService Tests")
class ProductServiceTest {

    // @Mock: dependency ngoài – không chạy thật
    @Mock
    private ProductRepository productRepository;

    // @InjectMocks: service cần test
    @InjectMocks
    private ProductService productService;

    private List<Product> buildTestProducts() {
        return List.of(
            Product.builder().id(1L).name("iPhone").price(new BigDecimal("25000000"))
                .stock(5).category("phone").build(),
            Product.builder().id(2L).name("Samsung").price(new BigDecimal("20000000"))
                .stock(3).category("phone").build(),
            Product.builder().id(3L).name("MacBook").price(new BigDecimal("50000000"))
                .stock(2).category("laptop").build(),
            Product.builder().id(4L).name("AirPods").price(new BigDecimal("6000000"))
                .stock(0).category("accessories").build() // out of stock
        );
    }

    @Test
    @DisplayName("getAllCategories - should return unique sorted categories")
    void getAllCategories_shouldReturnUniqueSortedList() {
        when(productRepository.findAll()).thenReturn(buildTestProducts());

        List<String> categories = productService.getAllCategories();

        assertEquals(3, categories.size());
        assertEquals("accessories", categories.get(0)); // sorted A-Z
        assertEquals("laptop",      categories.get(1));
        assertEquals("phone",       categories.get(2));
    }

    @Test
    @DisplayName("countByCategory - should return correct counts")
    void countByCategory_shouldGroupCorrectly() {
        when(productRepository.findAll()).thenReturn(buildTestProducts());

        Map<String, Long> counts = productService.countByCategory();

        assertEquals(2L, counts.get("phone"));
        assertEquals(1L, counts.get("laptop"));
        assertEquals(1L, counts.get("accessories"));
    }

    @Test
    @DisplayName("getCheapestInStock - should exclude out-of-stock products")
    void getCheapestInStock_shouldExcludeOutOfStock() {
        when(productRepository.findAll()).thenReturn(buildTestProducts());

        List<ProductResponse> result = productService.getCheapestInStock(10);

        // AirPods có stock=0 phải bị loại
        assertTrue(result.stream().noneMatch(p -> p.getName().equals("AirPods")));
        assertEquals(3, result.size()); // chỉ có 3 sản phẩm còn hàng
    }

    @Test
    @DisplayName("hasProductsInStock - should return true when stock available")
    void hasProductsInStock_shouldReturnTrue() {
        when(productRepository.findAll()).thenReturn(buildTestProducts());

        assertTrue(productService.hasProductsInStock());
    }

    @Test
    @DisplayName("getTotalInventoryValue - should calculate correctly")
    void getTotalInventoryValue_shouldCalculateCorrectly() {
        when(productRepository.findAll()).thenReturn(buildTestProducts());

        BigDecimal total = productService.getTotalInventoryValue();

        // iPhone: 25M * 5 = 125M
        // Samsung: 20M * 3 = 60M
        // MacBook: 50M * 2 = 100M
        // AirPods: 6M * 0 = 0
        // Total = 285M
        assertEquals(new BigDecimal("285000000"), total);
    }

    // ---- @Spy Demo ----

    @Test
    @DisplayName("@Spy Demo - override getAllCategories but keep real logic elsewhere")
    void spyDemo_overrideOneMethod() {
        // Tạo Spy từ object thật
        ProductService spyService = spy(new ProductService(productRepository));

        when(productRepository.findAll()).thenReturn(buildTestProducts());

        // Override chỉ getAllCategories – trả về cố định
        doReturn(List.of("FIXED_CATEGORY")).when(spyService).getAllCategories();

        List<String> categories = spyService.getAllCategories();
        assertEquals(List.of("FIXED_CATEGORY"), categories);

        // Nhưng getById, getProducts, v.v. vẫn dùng logic thật
        // (chỉ getAllCategories bị override)
    }
}
