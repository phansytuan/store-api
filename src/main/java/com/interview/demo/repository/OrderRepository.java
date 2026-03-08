package com.interview.demo.repository;

import com.interview.demo.entity.Order;
import com.interview.demo.enums.OrderStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {

    // PathParam: lấy orders của user cụ thể
    Page<Order> findByUserId(Long userId, Pageable pageable);

    // PathParam + QueryParam: lọc theo status
    Page<Order> findByUserIdAndStatus(Long userId, OrderStatus status, Pageable pageable);

    Optional<Order> findByIdAndUserId(Long orderId, Long userId);

    @Query("SELECT SUM(o.totalAmount) FROM Order o WHERE o.status = :status")
    Double sumTotalByStatus(@Param("status") OrderStatus status);
}
