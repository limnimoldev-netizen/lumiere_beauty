package com.example.backend_lumiere.repository;

import com.example.backend_lumiere.model.Inventory;
import org.springframework.data.jpa.repository.JpaRepository;

public interface InventoryRepository extends JpaRepository<Inventory, Long> {
}