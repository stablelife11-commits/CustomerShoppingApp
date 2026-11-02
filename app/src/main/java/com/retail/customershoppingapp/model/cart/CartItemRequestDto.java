package com.retail.customershoppingapp.model.cart;

import com.google.gson.annotations.SerializedName;

public class CartItemRequestDto {

    @SerializedName("variantId")
    private Long variantId;

    @SerializedName("quantity")
    private Integer quantity;

    @SerializedName("itemNote")
    private String itemNote;

    public CartItemRequestDto() {
    }

    public CartItemRequestDto(Long variantId, Integer quantity, String itemNote) {
        this.variantId = variantId;
        this.quantity = quantity;
        this.itemNote = itemNote;
    }

    public Long getVariantId() { return variantId; }
    public Integer getQuantity() { return quantity; }
    public String getItemNote() { return itemNote; }
}