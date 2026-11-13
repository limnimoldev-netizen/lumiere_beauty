package com.example.backend_lumiere.repository;

import com.example.backend_lumiere.model.Product;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductRepository extends JpaRepository<Product, Long> {

}