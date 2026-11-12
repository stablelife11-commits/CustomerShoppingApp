package com.retail.customershoppingapp.model;

import com.google.gson.annotations.SerializedName;

public class BuyerNotificationDto {
    @SerializedName("id")
    private Long id;

    @SerializedName("orderId")
    private Long orderId;

    @SerializedName("title")
    private String title;

    @SerializedName("message")
    private String message;

    @SerializedName("isRead")
    private Boolean isRead;

    @SerializedName("createdAt")
    private String createdAt;

    public Long getId() { return id; }
    public Long getOrderId() { return orderId; }
    public String getTitle() { return title; }
    public String getMessage() { return message; }
    public boolean isRead() { return Boolean.TRUE.equals(isRead); }
    public String getCreatedAt() { return createdAt; }
}