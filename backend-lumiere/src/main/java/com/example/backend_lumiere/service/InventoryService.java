package com.example.backend_lumiere.service;

import com.example.backend_lumiere.model.Inventory;
import com.example.backend_lumiere.repository.InventoryRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class InventoryService {

    private final InventoryRepository inventoryRepository;

    public InventoryService(InventoryRepository inventoryRepository) {
        this.inventoryRepository = inventoryRepository;
    }

    public List<Inventory> getAll() {
        return inventoryRepository.findAll();
    }

    public Inventory getById(Long id) {
        return inventoryRepository.findById(id).orElse(null);
    }

    public Inventory create(Inventory inventory) {
        return inventoryRepository.save(inventory);
    }

    public Inventory update(Long id, Inventory data) {
        Inventory inventory = inventoryRepository.findById(id).orElse(null);
        if (inventory == null) return null;
        inventory.setProductId(data.getProductId());
        inventory.setQuantity(data.getQuantity());
        return inventoryRepository.save(inventory);
    }

    public boolean delete(Long id) {
        if (!inventoryRepository.existsById(id)) return false;
        inventoryRepository.deleteById(id);
        return true;
    }
}