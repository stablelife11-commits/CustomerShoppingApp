package com.retail.customershoppingapp.model.order;

import com.google.gson.annotations.SerializedName;
import java.io.Serializable;
import java.util.List;

public class OrderResponse implements Serializable {

    @SerializedName("id")
    private Long id;

    @SerializedName("sellerId")
    private Long sellerId;

    @SerializedName("customerId")
    private Long customerId;

    @SerializedName("customerName")
    private String customerName;

    @SerializedName("customerMobile")
    private String customerMobile;

    @SerializedName("subtotalAmount")
    private Double subtotalAmount;

    @SerializedName("discountAmount")
    private Double discountAmount;

    @SerializedName("totalAmount")
    private Double totalAmount;

    @SerializedName("paidAmount")
    private Double paidAmount;

    @SerializedName("paymentStatus")
    private String paymentStatus;

    @SerializedName("sellerRemarks")
    private String sellerRemarks;

    @SerializedName("orderDate")
    private String orderDate;

    @SerializedName("status")
    private String status;

    @SerializedName("deliveryAddress")
    private String deliveryAddress;

    @SerializedName("items")
    private List<OrderItemResponse> items;

    public Long getId() { return id; }
    public Long getSellerId() { return sellerId; }
    public Long getCustomerId() { return customerId; }
    public String getCustomerName() { return customerName; }
    public String getCustomerMobile() { return customerMobile; }
    public Double getSubtotalAmount() { return subtotalAmount != null ? subtotalAmount : getTotalAmount(); }
    public Double getDiscountAmount() { return discountAmount != null ? discountAmount : 0.0; }
    public Double getTotalAmount() { return totalAmount != null ? totalAmount : 0.0; }
    public Double getPaidAmount() { return paidAmount != null ? paidAmount : 0.0; }
    public String getPaymentStatus() { return paymentStatus != null ? paymentStatus : "UNPAID"; }
    public String getSellerRemarks() { return sellerRemarks; }
    public String getOrderDate() { return orderDate; }
    public String getStatus() { return status != null ? status : "PLACED"; }
    public String getDeliveryAddress() { return deliveryAddress; }
    public List<OrderItemResponse> getItems() { return items; }
}