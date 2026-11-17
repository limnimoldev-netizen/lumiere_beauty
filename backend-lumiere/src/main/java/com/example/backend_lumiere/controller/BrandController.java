package com.example.backend_lumiere.controller;

import com.example.backend_lumiere.model.Brand;
import com.example.backend_lumiere.service.BrandService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/brands")
public class BrandController {

    private final BrandService brandService;

    public BrandController(BrandService brandService) {
        this.brandService = brandService;
    }

    @GetMapping
    public List<Brand> getAll() {
        return brandService.getAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Brand> getById(@PathVariable Long id) {
        Brand brand = brandService.getById(id);
        return brand == null ? ResponseEntity.notFound().build() : ResponseEntity.ok(brand);
    }

    @PostMapping
    public ResponseEntity<Brand> create(@RequestBody Brand brand) {
        return ResponseEntity.status(201).body(brandService.create(brand));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Brand> update(@PathVariable Long id, @RequestBody Brand brand) {
        Brand updated = brandService.update(id, brand);
        return updated == null ? ResponseEntity.notFound().build() : ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        return brandService.delete(id) ? ResponseEntity.noContent().build() : ResponseEntity.notFound().build();
    }
}