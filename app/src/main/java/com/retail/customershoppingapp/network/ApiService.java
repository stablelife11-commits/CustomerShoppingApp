package com.retail.customershoppingapp.network;

import com.retail.customershoppingapp.model.AppUpdateResponse;
import com.retail.customershoppingapp.model.auth.AuthResponse;
import com.retail.customershoppingapp.model.auth.LoginRequest;
import com.retail.customershoppingapp.model.auth.RegisterRequest;
import com.retail.customershoppingapp.model.cart.CartItemRequestDto;
import com.retail.customershoppingapp.model.cart.CartResponseDto;
import com.retail.customershoppingapp.model.order.OrderRequest;
import com.retail.customershoppingapp.model.order.OrderResponse;
import com.retail.customershoppingapp.model.product.ProductResponse;

import com.retail.customershoppingapp.model.BuyerNotificationDto;
import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.PATCH;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface ApiService {

    // Auth
    @POST("api/v1/auth/login")
    Call<AuthResponse> login(@Body LoginRequest request);

    @POST("api/v1/auth/register")
    Call<AuthResponse> register(@Body RegisterRequest request);

    // App OTA Update Check
    @GET("api/v1/app-update/check")
    Call<AppUpdateResponse> checkAppUpdate(@Query("appType") String appType);

    // Products with server-side pagination, keyword, category, and brand filtering
    @GET("api/v1/products")
    Call<List<ProductResponse>> getProducts(
            @Query("page") int page,
            @Query("size") int size,
            @Query("keyword") String keyword,
            @Query("category") String category,
            @Query("brand") String brand
    );

    @GET("api/v1/products/{id}")
    Call<ProductResponse> getProductById(@Path("id") Long id);

    // Persistent Server-Backed Buyer Cart
    @GET("api/v1/cart")
    Call<CartResponseDto> getMyCart();

    @POST("api/v1/cart/items")
    Call<CartResponseDto> addCartItem(@Body CartItemRequestDto request);

    @PUT("api/v1/cart/items/{variantId}")
    Call<CartResponseDto> updateCartItemQuantity(
            @Path("variantId") Long variantId,
            @Query("quantity") int quantity,
            @Query("itemNote") String itemNote
    );

    @DELETE("api/v1/cart/items/{variantId}")
    Call<CartResponseDto> removeCartItem(@Path("variantId") Long variantId);

    @DELETE("api/v1/cart")
    Call<Void> clearServerCart();

    // Multi-Seller Checkout (returns all split orders created across sellers)
    @POST("api/v1/orders/checkout")
    Call<List<OrderResponse>> checkoutOrders(@Body OrderRequest request);

    // Legacy single-order endpoint
    @POST("api/v1/orders")
    Call<OrderResponse> createOrder(@Body OrderRequest request);

    @GET("api/v1/orders/my-orders")
    Call<List<OrderResponse>> getOrders(
            @Query("page") int page,
            @Query("size") int size
    );

    @GET("api/v1/orders/{id}")
    Call<OrderResponse> getOrderById(@Path("id") Long id);

    // Buyer Order Cancellation (allowed when order is PLACED)
    @PATCH("api/v1/orders/{id}/status")
    Call<OrderResponse> updateOrderStatus(@Path("id") Long id, @Query("status") String status);
    @GET("api/v1/notifications/my")
    Call<List<BuyerNotificationDto>> getMyNotifications();

    @PATCH("api/v1/notifications/mark-all-read")
    Call<Void> markAllNotificationsRead();
    // 🟢 नया: FCM Token सर्वर पर सेव करने के लिए यह लाइन जोड़ें
    @POST("api/v1/notifications/fcm-token")
    Call<Void> updateFcmToken(@Body java.util.Map<String, String> body);
}

