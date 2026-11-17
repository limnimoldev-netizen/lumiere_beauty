package com.example.backend_lumiere.service;

import com.example.backend_lumiere.model.Brand;
import com.example.backend_lumiere.repository.BrandRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class BrandService {

    private final BrandRepository brandRepository;

    public BrandService(BrandRepository brandRepository) {
        this.brandRepository = brandRepository;
    }

    public List<Brand> getAll() {
        return brandRepository.findAll();
    }

    public Brand getById(Long id) {
        return brandRepository.findById(id).orElse(null);
    }

    public Brand create(Brand brand) {
        return brandRepository.save(brand);
    }

    public Brand update(Long id, Brand data) {
        Brand brand = brandRepository.findById(id).orElse(null);
        if (brand == null) return null;
        brand.setName(data.getName());
        brand.setDescription(data.getDescription());
        return brandRepository.save(brand);
    }

    public boolean delete(Long id) {
        if (!brandRepository.existsById(id)) return false;
        brandRepository.deleteById(id);
        return true;
    }
}