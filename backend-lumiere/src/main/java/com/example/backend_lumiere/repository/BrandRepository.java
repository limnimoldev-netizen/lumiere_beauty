package com.example.backend_lumiere.repository;

import com.example.backend_lumiere.model.Brand;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BrandRepository extends JpaRepository<Brand, Long> {
}