package com.example.backend_lumiere.repository;

import com.example.backend_lumiere.model.Category;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CategoryRepository extends JpaRepository<Category, Long> {
}
