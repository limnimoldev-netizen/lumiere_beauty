package com.example.backend_lumiere.model;

import jakarta.persistence.*;
import java.math.BigDecimal;

@Entity
@Table(name = "products")
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    @Column(name = "brand_id")
    private Long brandId;

    @Column(name = "category_id")
    private Long categoryId;

    @Column(precision = 10, scale = 2)
    private BigDecimal price;

    private String description;

    @Column(name = "image_url")
    private String imageUrl;

    private String status;

    private Integer stock;

    public Product() {
    }   
}