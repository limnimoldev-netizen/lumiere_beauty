package com.example.backend_lumiere.controller;

import com.example.backend_lumiere.model.Order;
import com.example.backend_lumiere.service.OrderItemService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/orders")
public class OrderItemController {

    private final OrderItemService orderItemService;

    public OrderItemController(OrderItemService orderItemService) {
        this.orderItemService = orderItemService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Order createOrder(@RequestBody Order order) {
        return orderItemService.createOrder(order);
    }

    @GetMapping
    public List<Order> getAllOrders() {
        return orderItemService.getAllOrders();
    }

    @GetMapping("/{id}")
    public Order getOrderById(@PathVariable Long id) {
        return orderItemService.getOrderById(id);
    }
}