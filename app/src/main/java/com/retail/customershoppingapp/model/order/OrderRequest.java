package com.retail.customershoppingapp.model.order;

import java.util.List;

public class OrderRequest {
    private Long customerId;

    // 🟢 NAYA: Backend ko Delivery Address bhejne ke liye
    private String deliveryAddress;

    private List<OrderItemRequest> items;

    public OrderRequest(Long customerId, String deliveryAddress, List<OrderItemRequest> items) {
        this.customerId = customerId;
        this.deliveryAddress = deliveryAddress;
        this.items = items;
    }

    public Long getCustomerId() { return customerId; }
    public String getDeliveryAddress() { return deliveryAddress; }
    public List<OrderItemRequest> getItems() { return items; }
}