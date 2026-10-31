package com.retail.customershoppingapp.model.order;

import com.google.gson.annotations.SerializedName;

public class OrderItemRequest {
    @SerializedName("variantId")
    private Long variantId;

    @SerializedName("quantity")
    private Integer quantity;

    @SerializedName("itemNote")
    private String itemNote;

    public OrderItemRequest(Long variantId, Integer quantity) {
        this(variantId, quantity, null);
    }

    public OrderItemRequest(Long variantId, Integer quantity, String itemNote) {
        this.variantId = variantId;
        this.quantity = quantity;
        this.itemNote = itemNote;
    }

    public Long getVariantId() { return variantId; }
    public void setVariantId(Long variantId) { this.variantId = variantId; }

    public Integer getQuantity() { return quantity; }
    public void setQuantity(Integer quantity) { this.quantity = quantity; }

    public String getItemNote() { return itemNote; }
    public void setItemNote(String itemNote) { this.itemNote = itemNote; }
}