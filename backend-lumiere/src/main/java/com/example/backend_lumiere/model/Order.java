package com.example.backend_lumiere.model;

import jakarta.persistence.*;

@Entity
@Table(name = "orders")
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "deliver_address", nullable = false)
    private String deliverAddress;

    @Column(name = "payment_method", nullable = false, length = 50)
    private String paymentMethod;

    public Order() {
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }

    public String getDeliverAddress() { return deliverAddress; }
    public void setDeliverAddress(String deliverAddress) { this.deliverAddress = deliverAddress; }

    public String getPaymentMethod() { return paymentMethod; }
    public void setPaymentMethod(String paymentMethod) { this.paymentMethod = paymentMethod; }
}