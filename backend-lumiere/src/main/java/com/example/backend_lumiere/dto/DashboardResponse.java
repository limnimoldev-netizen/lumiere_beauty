package com.example.backend_lumiere.dto;

import java.math.BigDecimal;
import java.util.List;

public record DashboardResponse(
        long totalProducts,
        long activeProducts,
        long lowStock,
        BigDecimal stockValue,
        long totalOrders,
        List<DashboardProduct> products) {

    public record DashboardProduct(
            Long id,
            String code,
            String name,
            String category,
            BigDecimal price,
            String status,
            String imageUrl) {
    }
}