package com.interview.demo.repository;

import com.interview.demo.entity.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {

    // QueryParam demo: tìm kiếm + lọc theo category
    Page<Product> findByCategoryIgnoreCase(String category, Pageable pageable);

    @Query("SELECT p FROM Product p WHERE " +
           "(cast(:keyword as String) IS NULL OR LOWER(p.name) LIKE LOWER(CONCAT('%', cast(:keyword as String), '%'))) AND " +
           "(cast(:category as String) IS NULL OR LOWER(p.category) = LOWER(cast(:category as String)))")
    Page<Product> search(@Param("keyword") String keyword,
                         @Param("category") String category,
                         Pageable pageable);
}
