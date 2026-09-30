package com.retail.customershoppingapp.repository;

import android.content.Context;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.retail.customershoppingapp.model.cart.CartItem;
import com.retail.customershoppingapp.model.product.ProductResponse;
import com.retail.customershoppingapp.model.product.VariantResponse;
import com.retail.customershoppingapp.session.SessionManager;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class CartRepository {
    private final SessionManager sessionManager;
    private final MutableLiveData<List<CartItem>> cartLiveData = new MutableLiveData<>();

    public CartRepository(Context context) {
        this.sessionManager = new SessionManager(context);
        cartLiveData.setValue(sessionManager.getCart());
    }

    public LiveData<List<CartItem>> getCartLiveData() {
        return cartLiveData;
    }

    public List<CartItem> getCartItems() {
        List<CartItem> current = cartLiveData.getValue();
        return current != null ? current : new ArrayList<>();
    }

    public void addToCart(ProductResponse product, VariantResponse variant, int quantity) {
        List<CartItem> currentList = new ArrayList<>(getCartItems());
        boolean exists = false;

        for (CartItem item : currentList) {
            boolean sameProduct = item.getProduct() != null && product != null &&
                    item.getProduct().getId().equals(product.getId());
            boolean sameVariant = (item.getVariant() == null && variant == null) ||
                    (item.getVariant() != null && variant != null && item.getVariant().getId().equals(variant.getId()));

            if (sameProduct && sameVariant) {
                item.setQuantity(item.getQuantity() + quantity);
                exists = true;
                break;
            }
        }

        if (!exists) {
            currentList.add(new CartItem(product, variant, quantity));
        }

        sessionManager.saveCart(currentList);
        cartLiveData.setValue(currentList);
    }

    public void updateQuantity(CartItem item, int newQuantity) {
        List<CartItem> currentList = new ArrayList<>(getCartItems());
        for (int i = 0; i < currentList.size(); i++) {
            CartItem cartItem = currentList.get(i);
            if (cartItem.equals(item) || isSameCartItem(cartItem, item)) {
                if (newQuantity <= 0) {
                    currentList.remove(i);
                } else {
                    cartItem.setQuantity(newQuantity);
                }
                break;
            }
        }
        sessionManager.saveCart(currentList);
        cartLiveData.setValue(currentList);
    }

    public void removeFromCart(CartItem item) {
        updateQuantity(item, 0);
    }

    public void clearCart() {
        sessionManager.clearCart();
        cartLiveData.setValue(new ArrayList<>());
    }

    public BigDecimal getCartSubtotal() {
        BigDecimal total = BigDecimal.ZERO;
        for (CartItem item : getCartItems()) {
            total = total.add(item.getTotalPrice());
        }
        return total;
    }

    public int getItemCount() {
        int count = 0;
        for (CartItem item : getCartItems()) {
            count += item.getQuantity();
        }
        return count;
    }

    private boolean isSameCartItem(CartItem a, CartItem b) {
        if (a == null || b == null) return false;
        boolean sameProduct = a.getProduct() != null && b.getProduct() != null &&
                a.getProduct().getId().equals(b.getProduct().getId());
        boolean sameVariant = (a.getVariant() == null && b.getVariant() == null) ||
                (a.getVariant() != null && b.getVariant() != null && a.getVariant().getId().equals(b.getVariant().getId()));
        return sameProduct && sameVariant;
    }
}
