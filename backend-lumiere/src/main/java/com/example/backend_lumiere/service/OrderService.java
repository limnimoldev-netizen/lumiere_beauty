package com.example.backend_lumiere.service;

import com.example.backend_lumiere.dto.OrderResponse;
import com.example.backend_lumiere.model.Order;
import com.example.backend_lumiere.model.OrderItem;
import com.example.backend_lumiere.model.Product;
import com.example.backend_lumiere.repository.OrderRepository;
import com.example.backend_lumiere.repository.ProductRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

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
    public Order createOrder(Order request) {
        if (request.getItems() == null || request.getItems().isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Order must have at least one item");
        }

        Order order = new Order();
        order.setUserId(request.getUserId());
        order.setDeliveryAddress(request.getDeliveryAddress());
        order.setPaymentMethod(request.getPaymentMethod());

        for (OrderItem requestItem : request.getItems()) {
            if (requestItem.getProduct() == null || requestItem.getProduct().getId() == null) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Each item needs a product id");
            }
            if (requestItem.getQuantity() == null || requestItem.getQuantity() <= 0) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Quantity must be greater than 0");
            }
            if (requestItem.getUnitPrice() == null || requestItem.getUnitPrice() < 0) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Unit price is required");
            }

            Product product = productRepository.findById(requestItem.getProduct().getId())
                    .orElseThrow(() -> new ResponseStatusException(
                            HttpStatus.NOT_FOUND,
                            "Product not found: " + requestItem.getProduct().getId()));

            OrderItem item = new OrderItem();
            item.setProduct(product);
            item.setQuantity(requestItem.getQuantity());
            item.setUnitPrice(requestItem.getUnitPrice());

            order.addItem(item);
        }

        return orderRepository.save(order);
    }

    public List<OrderResponse> getAllOrders() {
        return orderRepository.findAll().stream().map(OrderResponse::from).toList();
    }

    public OrderResponse getOrderById(Long id) {
        return orderRepository.findById(id).map(OrderResponse::from)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Order not found: " + id));
    }
}
