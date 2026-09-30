package com.retail.customershoppingapp.model.cart;

import com.retail.customershoppingapp.model.product.ProductResponse;
import com.retail.customershoppingapp.model.product.VariantResponse;

import java.io.Serializable;
import java.math.BigDecimal;

public class CartItem implements Serializable {
    private ProductResponse product;
    private VariantResponse variant;
    private int quantity;

    public CartItem(ProductResponse product, VariantResponse variant, int quantity) {
        this.product = product;
        this.variant = variant;
        this.quantity = quantity;
    }

    public ProductResponse getProduct() {
        return product;
    }

    public void setProduct(ProductResponse product) {
        this.product = product;
    }

    public VariantResponse getVariant() {
        return variant;
    }

    public void setVariant(VariantResponse variant) {
        this.variant = variant;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

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
