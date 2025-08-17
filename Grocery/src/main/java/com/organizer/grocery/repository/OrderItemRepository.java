package com.organizer.grocery.repository;

import com.organizer.grocery.model.OrderItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OrderItemRepository extends JpaRepository<OrderItem, Long> {
    @Query("SELECT oi FROM OrderItem oi WHERE oi.order.id = :orderId AND oi.productName = :productName")
    List<OrderItem> findByOrderIdAndProductName(@Param("orderId") Long orderId, @Param("productName") String productName);
    List<OrderItem> findByOrderId(Long orderId);
}
