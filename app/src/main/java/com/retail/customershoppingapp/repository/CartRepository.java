package com.retail.customershoppingapp.repository;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.retail.customershoppingapp.model.cart.CartItem;
import com.retail.customershoppingapp.model.cart.CartItemRequestDto;
import com.retail.customershoppingapp.model.cart.CartResponseDto;
import com.retail.customershoppingapp.model.product.ProductResponse;
import com.retail.customershoppingapp.model.product.VariantResponse;
import com.retail.customershoppingapp.network.ApiService;
import com.retail.customershoppingapp.network.RetrofitClient;
import com.retail.customershoppingapp.session.SessionManager;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CartRepository {
    private static final MutableLiveData<List<CartItem>> SHARED_CART_LIVE_DATA = new MutableLiveData<>();
    private static boolean initialized = false;

    private final SessionManager sessionManager;
    private final ApiService apiService;

    public CartRepository(Context context) {
        Context appContext = context.getApplicationContext();
        this.sessionManager = new SessionManager(appContext);
        this.apiService = RetrofitClient.getInstance(appContext).getApi();

        synchronized (CartRepository.class) {
            if (!initialized || SHARED_CART_LIVE_DATA.getValue() == null) {
                SHARED_CART_LIVE_DATA.setValue(sessionManager.getCart());
                initialized = true;
                if (sessionManager.isLoggedIn()) {
                    syncCartFromServer();
                }
            }
        }
    }

    public LiveData<List<CartItem>> getCartLiveData() {
        return SHARED_CART_LIVE_DATA;
    }

    public List<CartItem> getCartItems() {
        List<CartItem> current = SHARED_CART_LIVE_DATA.getValue();
        return current != null ? current : new ArrayList<>();
    }

    public void refreshLocalCart() {
        SHARED_CART_LIVE_DATA.setValue(sessionManager.getCart());
    }

    public void syncCartFromServer() {
        if (!sessionManager.isLoggedIn()) return;
        apiService.getMyCart().enqueue(new Callback<CartResponseDto>() {
            @Override
            public void onResponse(@NonNull Call<CartResponseDto> call, @NonNull Response<CartResponseDto> response) {
                if (response.isSuccessful() && response.body() != null) {
                    applyServerCartResponse(response.body());
                }
            }

            @Override
            public void onFailure(@NonNull Call<CartResponseDto> call, @NonNull Throwable t) {
            }
        });
    }

    public void addToCart(ProductResponse product, VariantResponse variant, int quantity) {
        addToCart(product, variant, quantity, null);
    }

    public void addToCart(ProductResponse product, VariantResponse variant, int quantity, String itemNote) {
        List<CartItem> currentList = new ArrayList<>(getCartItems());
        boolean exists = false;
        String cleanNote = (itemNote != null && !itemNote.trim().isEmpty()) ? itemNote.trim() : null;

        for (CartItem item : currentList) {
            boolean sameProduct = item.getProduct() != null && product != null &&
                    item.getProduct().getId().equals(product.getId());
            boolean sameVariant = (item.getVariant() == null && variant == null) ||
                    (item.getVariant() != null && variant != null && item.getVariant().getId().equals(variant.getId()));

            if (sameProduct && sameVariant) {
                item.setQuantity(item.getQuantity() + quantity);
                if (cleanNote != null) {
                    item.setItemNote(cleanNote);
                }
                exists = true;
                break;
            }
        }

        if (!exists) {
            currentList.add(new CartItem(product, variant, quantity, cleanNote));
        }

        sessionManager.saveCart(currentList);
        SHARED_CART_LIVE_DATA.setValue(currentList);

        if (sessionManager.isLoggedIn() && variant != null && variant.getId() != null) {
            apiService.addCartItem(new CartItemRequestDto(variant.getId(), quantity, cleanNote))
                    .enqueue(new Callback<CartResponseDto>() {
                        @Override
                        public void onResponse(@NonNull Call<CartResponseDto> call, @NonNull Response<CartResponseDto> response) {
                            if (response.isSuccessful() && response.body() != null) {
                                applyServerCartResponse(response.body());
                            }
                        }

                        @Override
                        public void onFailure(@NonNull Call<CartResponseDto> call, @NonNull Throwable t) {
                        }
                    });
        }
    }

    public void updateItemNote(CartItem targetItem, String newNote) {
        List<CartItem> currentList = new ArrayList<>(getCartItems());
        String cleanNote = (newNote != null && !newNote.trim().isEmpty()) ? newNote.trim() : null;
        int currentQty = targetItem.getQuantity();
        Long variantId = targetItem.getVariant() != null ? targetItem.getVariant().getId() : null;

        for (CartItem cartItem : currentList) {
            if (cartItem.equals(targetItem) || isSameCartItem(cartItem, targetItem)) {
                cartItem.setItemNote(cleanNote);
                currentQty = cartItem.getQuantity();
                break;
            }
        }
        sessionManager.saveCart(currentList);
        SHARED_CART_LIVE_DATA.setValue(currentList);

        if (sessionManager.isLoggedIn() && variantId != null) {
            apiService.updateCartItemQuantity(variantId, currentQty, cleanNote != null ? cleanNote : "")
                    .enqueue(new Callback<CartResponseDto>() {
                        @Override
                        public void onResponse(@NonNull Call<CartResponseDto> call, @NonNull Response<CartResponseDto> response) {
                            if (response.isSuccessful() && response.body() != null) {
                                applyServerCartResponse(response.body());
                            }
                        }

                        @Override
                        public void onFailure(@NonNull Call<CartResponseDto> call, @NonNull Throwable t) {
                        }
                    });
        }
    }

    public void updateQuantity(CartItem item, int newQuantity) {
        List<CartItem> currentList = new ArrayList<>(getCartItems());
        Long variantId = item.getVariant() != null ? item.getVariant().getId() : null;
        String currentNote = item.getItemNote();

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
        SHARED_CART_LIVE_DATA.setValue(currentList);

        if (sessionManager.isLoggedIn() && variantId != null) {
            if (newQuantity <= 0) {
                apiService.removeCartItem(variantId).enqueue(new Callback<CartResponseDto>() {
                    @Override
                    public void onResponse(@NonNull Call<CartResponseDto> call, @NonNull Response<CartResponseDto> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            applyServerCartResponse(response.body());
                        }
                    }

                    @Override
                    public void onFailure(@NonNull Call<CartResponseDto> call, @NonNull Throwable t) {
                    }
                });
            } else {
                apiService.updateCartItemQuantity(variantId, newQuantity, currentNote)
                        .enqueue(new Callback<CartResponseDto>() {
                            @Override
                            public void onResponse(@NonNull Call<CartResponseDto> call, @NonNull Response<CartResponseDto> response) {
                                if (response.isSuccessful() && response.body() != null) {
                                    applyServerCartResponse(response.body());
                                }
                            }

                            @Override
                            public void onFailure(@NonNull Call<CartResponseDto> call, @NonNull Throwable t) {
                            }
                        });
            }
        }
    }

    public void removeFromCart(CartItem item) {
        updateQuantity(item, 0);
    }

    public void clearCart() {
        sessionManager.clearCart();
        SHARED_CART_LIVE_DATA.setValue(new ArrayList<>());
        if (sessionManager.isLoggedIn()) {
            apiService.clearServerCart().enqueue(new Callback<Void>() {
                @Override
                public void onResponse(@NonNull Call<Void> call, @NonNull Response<Void> response) {
                }

                @Override
                public void onFailure(@NonNull Call<Void> call, @NonNull Throwable t) {
                }
            });
        }
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

    private void applyServerCartResponse(CartResponseDto serverCart) {
        List<CartItem> localExisting = getCartItems();
        Map<Long, List<String>> cachedImagesByProductId = new HashMap<>();
        for (CartItem existing : localExisting) {
            if (existing.getProduct() != null && existing.getProduct().getId() != null
                    && existing.getProduct().getImageUrls() != null) {
                cachedImagesByProductId.put(existing.getProduct().getId(), existing.getProduct().getImageUrls());
            }
        }

        List<CartItem> syncedList = serverCart.toLocalCartItems();
        for (CartItem synced : syncedList) {
            if (synced.getProduct() != null && synced.getProduct().getId() != null) {
                List<String> imgs = cachedImagesByProductId.get(synced.getProduct().getId());
                if (imgs != null) {
                    synced.getProduct().setImageUrls(imgs);
                }
            }
        }

        sessionManager.saveCart(syncedList);
        SHARED_CART_LIVE_DATA.setValue(syncedList);
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