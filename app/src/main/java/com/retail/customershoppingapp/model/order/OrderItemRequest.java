package com.retail.customershoppingapp.model.order;

import com.google.gson.annotations.SerializedName;

public class OrderItemRequest {
    @SerializedName("variantId")
    private Long variantId;

    @SerializedName("quantity")
    private Integer quantity;

    public OrderItemRequest(Long variantId, Integer quantity) {
        this.variantId = variantId;
        this.quantity = quantity;
    }

    public Long getVariantId() {
        return variantId;
    }

    public void setVariantId(Long variantId) {
        this.variantId = variantId;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }
}
