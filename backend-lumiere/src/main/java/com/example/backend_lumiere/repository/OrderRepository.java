package com.example.backend_lumiere.repository;


import com.example.backend_lumiere.model.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository 

public interface OrderRepository extends JpaRepository<Order, Long>{

    
}
