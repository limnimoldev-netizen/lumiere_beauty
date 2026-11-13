package com.example.backend_lumiere.service;

import com.example.backend_lumiere.model.Product;
import com.example.backend_lumiere.repository.ProductRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ProductService {

    private final ProductRepository productRepository;

    public ProductService(ProductRepository productRepository) {     //provide the ProductRepository automatically
        this.productRepository = productRepository;
    }

    public List<Product> getAllProducts() {
        return productRepository.findAll();
    }

    public Product createProduct(Product product) {
        return productRepository.save(product);
    }
    
    public Product updateProduct(Long id, Product updatedProduct) {

        Product existingProduct = productRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Product not found "));

            existingProduct.setName(updatedProduct.getName());
            existingProduct.setBrandId(updatedProduct.getBrandId());
            existingProduct.setCategoryId(updatedProduct.getCategoryId());
            existingProduct.setPrice(updatedProduct.getPrice());
            existingProduct.setDescription(updatedProduct.getDescription());
            existingProduct.setImageUrl(updatedProduct.getImageUrl());
            existingProduct.setStock(updatedProduct.getStock());
            existingProduct.setStatus(updatedProduct.getStatus());

        return productRepository.save(existingProduct);
    }

    public void deleteProduct(Long id) {
        productRepository.deleteById(id);
    }
}



