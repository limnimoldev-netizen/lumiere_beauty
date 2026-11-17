package com.example.backend_lumiere.service;

import com.example.backend_lumiere.dto.DashboardResponse;
import com.example.backend_lumiere.dto.DashboardResponse.DashboardProduct;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

@Service
public class DashboardService {

    private static final int LOW_STOCK_LIMIT = 10;   // stock below this = "Low stock"
    private static final int CATALOGUE_PREVIEW = 5;  // rows shown in "Product catalogue"

    private final JdbcTemplate jdbcTemplate;

    public DashboardService(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public DashboardResponse getDashboard() {

        // 1) the 4 stat cards, calculated in one query
        String statsSql = """
                SELECT COUNT(*) AS total,
                       COUNT(*) FILTER (WHERE LOWER(status) = 'active') AS active,
                       COUNT(*) FILTER (WHERE COALESCE(stock, 0) < ?) AS low,
                       COALESCE(SUM(price * COALESCE(stock, 0)), 0) AS stock_value
                FROM products
                """;

        Object[] stats = jdbcTemplate.queryForObject(statsSql,
                (rs, rowNum) -> new Object[] {
                        rs.getLong("total"),
                        rs.getLong("active"),
                        rs.getLong("low"),
                        rs.getBigDecimal("stock_value")
                },
                LOW_STOCK_LIMIT);

        // 2) total orders (same value the old endpoint returned)
        Long totalOrders = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM orders", Long.class);

        // 3) first 5 products for the "Product catalogue" list (with category name)
        String productsSql = """
                SELECT p.id, p.name, c.name AS category, p.price, p.status, p.image_url
                FROM products p
                LEFT JOIN categories c ON c.id = p.category_id
                ORDER BY p.id
                LIMIT ?
                """;

        List<DashboardProduct> products = jdbcTemplate.query(productsSql,
                (rs, rowNum) -> {
                    long id = rs.getLong("id");
                    return new DashboardProduct(
                            id,
                            "P-" + (1000 + id),            // P-1001 style label
                            rs.getString("name"),
                            rs.getString("category"),
                            rs.getBigDecimal("price"),
                            rs.getString("status"),
                            rs.getString("image_url"));
                },
                CATALOGUE_PREVIEW);

        return new DashboardResponse(
                (Long) stats[0],
                (Long) stats[1],
                (Long) stats[2],
                (BigDecimal) stats[3],
                totalOrders == null ? 0 : totalOrders,
                products);
    }
}