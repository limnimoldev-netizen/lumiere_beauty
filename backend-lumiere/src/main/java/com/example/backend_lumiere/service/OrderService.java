package com.example.backend_lumiere.service;

import com.example.backend_lumiere.model.Order;
import com.example.backend_lumiere.model.User;
import com.example.backend_lumiere.repository.OrderRepository;
import com.example.backend_lumiere.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class OrderService {

    private final OrderRepository orderRepository;
    private final UserRepository userRepository;

    public OrderService(OrderRepository orderRepository, UserRepository userRepository) {
        this.orderRepository = orderRepository;
        this.userRepository = userRepository;
    }

    public List<Order> getAllOrders() {
        return orderRepository.findAll();
    }

    public Order getOrderById(Long id) {
        return orderRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Order not found"));
    }

    public List<Order> getOrdersByUser(Long userId) {
        return orderRepository.findByUserId(userId);
    }

    public Order createOrder(Order order) {
        if (order.getUser() == null || order.getUser().getId() == null) {
            throw new RuntimeException("User id is required");
        }

        User user = userRepository.findById(order.getUser().getId())
                .orElseThrow(() -> new RuntimeException("User not found"));

        order.setUser(user);
        return orderRepository.save(order);
    }

    public Order updateOrder(Long id, Order newOrder) {
        Order existing = getOrderById(id);

        existing.setDeliverAddress(newOrder.getDeliverAddress());
        existing.setPaymentMethod(newOrder.getPaymentMethod());

        return orderRepository.save(existing);
    }

    public void deleteOrder(Long id) {
        orderRepository.deleteById(id);
    }
}