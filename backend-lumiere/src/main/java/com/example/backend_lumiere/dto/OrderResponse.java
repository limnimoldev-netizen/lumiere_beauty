package com.example.backend_lumiere.dto;

import com.example.backend_lumiere.model.Order;

public record OrderResponse(Long id, Long userId, Long deliveryAddress, Long paymentMethod) {
    public static OrderResponse from(Order order) {
        return new OrderResponse(order.getId(), order.getUserId(),
                order.getDeliveryAddress(), order.getPaymentMethod());
    }
}
