package com.retail.customershoppingapp.model.cart;

import com.retail.customershoppingapp.model.product.ProductResponse;
import com.retail.customershoppingapp.model.product.VariantResponse;

import java.io.Serializable;
import java.math.BigDecimal;

public class CartItem implements Serializable {
    private ProductResponse product;
    private VariantResponse variant;
    private int quantity;
    private String itemNote;

    public CartItem(ProductResponse product, VariantResponse variant, int quantity) {
        this(product, variant, quantity, null);
    }

    public CartItem(ProductResponse product, VariantResponse variant, int quantity, String itemNote) {
        this.product = product;
        this.variant = variant;
        this.quantity = quantity;
        this.itemNote = itemNote;
    }

    public ProductResponse getProduct() { return product; }
    public void setProduct(ProductResponse product) { this.product = product; }

    public VariantResponse getVariant() { return variant; }
    public void setVariant(VariantResponse variant) { this.variant = variant; }

    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }

    public String getItemNote() { return itemNote; }
    public void setItemNote(String itemNote) { this.itemNote = itemNote; }

    public BigDecimal getUnitPrice() {
        if (variant != null && variant.getSellingPrice() != null) {
            return variant.getSellingPrice();
        }
        if (product != null) {
            return product.getMinPrice();
        }
        return BigDecimal.ZERO;
    }

    public BigDecimal getTotalPrice() {
        return getUnitPrice().multiply(BigDecimal.valueOf(quantity));
    }
}