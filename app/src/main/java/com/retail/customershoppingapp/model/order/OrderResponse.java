package com.retail.customershoppingapp.model.order;

import com.google.gson.annotations.SerializedName;
import java.io.Serializable; // 🟢 NAYA IMPORT
import java.util.List;

// 🟢 FIX: implements Serializable lagaya gaya hai
public class OrderResponse implements Serializable {

    @SerializedName("id")
    private Long id;

    @SerializedName("customerId")
    private Long customerId;

    @SerializedName("customerName")
    private String customerName;

    @SerializedName("totalAmount")
    private Double totalAmount;

    @SerializedName("orderDate")
    private String orderDate;

    @SerializedName("status")
    private String status;

    @SerializedName("deliveryAddress")
    private String deliveryAddress;

    @SerializedName("items")
    private List<OrderItemResponse> items;

    // Getters
    public Long getId() { return id; }
    public Long getCustomerId() { return customerId; }
    public String getCustomerName() { return customerName; }
    public Double getTotalAmount() { return totalAmount; }
    public String getOrderDate() { return orderDate; }
    public String getStatus() { return status; }
    public String getDeliveryAddress() { return deliveryAddress; }
    public List<OrderItemResponse> getItems() { return items; }
}