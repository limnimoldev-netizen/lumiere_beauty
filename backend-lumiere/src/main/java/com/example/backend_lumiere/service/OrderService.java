package com.example.backend_lumiere.service;

import com.example.backend_lumiere.dto.CheckoutItem;
import com.example.backend_lumiere.dto.CheckoutRequest;
import com.example.backend_lumiere.model.Order;
import com.example.backend_lumiere.model.OrderItem;
import com.example.backend_lumiere.model.Product;
import com.example.backend_lumiere.repository.OrderRepository;
import com.example.backend_lumiere.repository.ProductRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class OrderService {

    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;

    public OrderService(OrderRepository orderRepository, ProductRepository productRepository) {
        this.orderRepository = orderRepository;
        this.productRepository = productRepository;
    }

    @Transactional
    public Order checkout(CheckoutRequest req) {
        if (req.getItems() == null || req.getItems().isEmpty()) {
            throw new IllegalArgumentException("Cart is empty");
        }

        Order order = new Order();
        order.setCustomerName(req.getCustomerName());
        order.setEmail(req.getEmail());
        order.setPhone(req.getPhone());
        order.setAddress(req.getAddress());
        order.setPaymentMethod(req.getPaymentMethod());
        order.setStatus("PENDING");
        order.setCreatedAt(LocalDateTime.now());

        BigDecimal total = BigDecimal.ZERO;

        for (CheckoutItem ci : req.getItems()) {
            Product product = productRepository.findById(ci.getProductId())
                    .orElseThrow(() -> new IllegalArgumentException("Product not found: " + ci.getProductId()));

            if (product.getStock() < ci.getQuantity()) {
                throw new IllegalArgumentException("Not enough stock for " + product.getName());
            }

            BigDecimal subtotal = product.getPrice().multiply(BigDecimal.valueOf(ci.getQuantity()));

            OrderItem item = new OrderItem();
            item.setOrder(order);
            item.setProductId(product.getId());
            item.setQuantity(ci.getQuantity());
            item.setUnitPrice(product.getPrice());
            item.setSubtotal(subtotal);
            order.getItems().add(item);

            product.setStock(product.getStock() - ci.getQuantity());
            productRepository.save(product);

            total = total.add(subtotal);
        }

        order.setTotalAmount(total);
        return orderRepository.save(order);
    }

    @Transactional(readOnly = true)
    public List<Order> getAllOrders() {
        return orderRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Order getOrderById(Long id) {
        return orderRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Order not found"));
    }

    @Transactional(readOnly = true)
    public List<Order> getOrdersByEmail(String email) {
        return orderRepository.findByEmailIgnoreCaseOrderByCreatedAtDesc(email.trim());
    }
}