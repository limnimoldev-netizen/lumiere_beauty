package com.example.backend_lumiere.service;

import com.example.backend_lumiere.model.Order;
import com.example.backend_lumiere.repository.OrderRepository;
import org.springframework.stereotype.Service;

import java.util.List;


@Service 
public class OrderService {
    private final OrderRepository OrderRepository;

    public OrderService ( OrderRepository OrderRepository ) {
        this.OrderRepository = OrderRepository;
    }

    // get all orders
    public List<Order>getAllOrders(){
        return OrderRepository.findAll();
    }

    // create order
    public Order createOrder( Order order ) {
        order.setId(null);
        return OrderRepository.save(order);
    }
}
