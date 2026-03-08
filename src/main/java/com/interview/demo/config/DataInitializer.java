package com.interview.demo.config;

import com.interview.demo.entity.Product;
import com.interview.demo.entity.User;
import com.interview.demo.enums.Role;
import com.interview.demo.repository.ProductRepository;
import com.interview.demo.repository.UserRepository;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;

/**
 * DataInitializer
 *
 * Demo Bean Lifecycle:
 * 1. Spring khởi tạo Bean (Constructor)
 * 2. Inject dependencies
 * 3. @PostConstruct – chạy sau khi inject xong
 *
 * Seed data để test ngay khi chạy project.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class DataInitializer {

    private final UserRepository    userRepository;
    private final ProductRepository productRepository;
    private final PasswordEncoder   passwordEncoder;

    /**
     * Bean Lifecycle: @PostConstruct chạy sau khi Bean được inject đầy đủ.
     * Dùng để khởi tạo data, cache, connection pool, v.v.
     */
    @PostConstruct
    public void init() {
        log.info("=== DataInitializer @PostConstruct – seeding data ===");
        seedUsers();
        seedProducts();
        log.info("=== Data seeding completed ===");
    }

    private void seedUsers() {
        if (userRepository.count() > 0) return;

        List<User> users = List.of(
            User.builder()
                .fullName("Admin User")
                .email("admin@demo.com")
                .password(passwordEncoder.encode("admin123"))
                .role(Role.ADMIN)
                .build(),
            User.builder()
                .fullName("John Doe")
                .email("john@demo.com")
                .password(passwordEncoder.encode("user123"))
                .role(Role.USER)
                .build(),
            User.builder()
                .fullName("Jane Smith")
                .email("jane@demo.com")
                .password(passwordEncoder.encode("user123"))
                .role(Role.USER)
                .build()
        );
        userRepository.saveAll(users);
        log.info("Seeded {} users", users.size());
    }

    private void seedProducts() {
        if (productRepository.count() > 0) return;

        List<Product> products = List.of(
            Product.builder().name("iPhone 15 Pro").description("Apple flagship phone")
                .price(new BigDecimal("29990000")).stock(50).category("phone").build(),
            Product.builder().name("Samsung Galaxy S24").description("Android flagship")
                .price(new BigDecimal("24990000")).stock(40).category("phone").build(),
            Product.builder().name("MacBook Pro M3").description("Apple laptop")
                .price(new BigDecimal("54990000")).stock(20).category("laptop").build(),
            Product.builder().name("Dell XPS 15").description("Windows laptop")
                .price(new BigDecimal("39990000")).stock(15).category("laptop").build(),
            Product.builder().name("AirPods Pro").description("Apple wireless earbuds")
                .price(new BigDecimal("6490000")).stock(100).category("accessories").build(),
            Product.builder().name("iPad Air").description("Apple tablet")
                .price(new BigDecimal("16990000")).stock(30).category("tablet").build(),
            Product.builder().name("Sony WH-1000XM5").description("Noise cancelling headphones")
                .price(new BigDecimal("8990000")).stock(25).category("accessories").build(),
            Product.builder().name("Apple Watch Series 9").description("Apple smartwatch")
                .price(new BigDecimal("12990000")).stock(60).category("accessories").build()
        );
        productRepository.saveAll(products);
        log.info("Seeded {} products", products.size());
    }
}
