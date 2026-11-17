package com.example.backend_lumiere.repository;

import com.example.backend_lumiere.model.OrderItem;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderItemRepository extends JpaRepository<OrderItem, Long> {
}