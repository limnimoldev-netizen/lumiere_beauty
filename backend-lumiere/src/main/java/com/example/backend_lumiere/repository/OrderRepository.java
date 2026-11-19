package com.example.backend_lumiere.repository;
import java.util.List;
import com.example.backend_lumiere.model.Order;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderRepository extends JpaRepository<Order, Long> {
    List<Order> findByEmailIgnoreCaseOrderByCreatedAtDesc(String email);
}