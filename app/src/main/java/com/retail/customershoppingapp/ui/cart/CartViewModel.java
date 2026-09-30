package com.retail.customershoppingapp.ui.cart;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import com.retail.customershoppingapp.model.cart.CartItem;
import com.retail.customershoppingapp.model.product.ProductResponse;
import com.retail.customershoppingapp.model.product.VariantResponse;
import com.retail.customershoppingapp.repository.CartRepository;

import java.math.BigDecimal;
import java.util.List;

public class CartViewModel extends AndroidViewModel {
    private final CartRepository cartRepository;

    public CartViewModel(@NonNull Application application) {
        super(application);
        cartRepository = new CartRepository(application);
    }

    public LiveData<List<CartItem>> getCartItems() {
        return cartRepository.getCartLiveData();
    }

    public void addToCart(ProductResponse product, VariantResponse variant, int quantity) {
        cartRepository.addToCart(product, variant, quantity);
    }

    public void updateQuantity(CartItem item, int quantity) {
        cartRepository.updateQuantity(item, quantity);
    }

    public void removeFromCart(CartItem item) {
        cartRepository.removeFromCart(item);
    }

    public void clearCart() {
        cartRepository.clearCart();
    }

    public BigDecimal getSubtotal() {
        return cartRepository.getCartSubtotal();
    }

    public int getItemCount() {
        return cartRepository.getItemCount();
    }
}
