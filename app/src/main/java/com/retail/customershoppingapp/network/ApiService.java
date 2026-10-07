package com.retail.customershoppingapp.network;

import com.retail.customershoppingapp.model.auth.AuthResponse;
import com.retail.customershoppingapp.model.auth.LoginRequest;
import com.retail.customershoppingapp.model.auth.RegisterRequest;
import com.retail.customershoppingapp.model.customer.CustomerRequest;
import com.retail.customershoppingapp.model.customer.CustomerResponse;
import com.retail.customershoppingapp.model.order.OrderRequest;
import com.retail.customershoppingapp.model.order.OrderResponse;
import com.retail.customershoppingapp.model.product.ProductResponse;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Path;
import retrofit2.http.Query; // 🟢 NAYA IMPORT

public interface ApiService {

    // Auth
    @POST("api/v1/auth/login")
    Call<AuthResponse> login(@Body LoginRequest request);

    @POST("api/v1/auth/register")
    Call<AuthResponse> register(@Body RegisterRequest request);

    // 🟢 FIX: Pagination support added
    @GET("api/v1/products")
    Call<List<ProductResponse>> getProducts(@Query("page") int page, @Query("size") int size);

    @GET("api/v1/products/{id}")
    Call<ProductResponse> getProductById(@Path("id") Long id);

    // Orders
    @POST("api/v1/orders")
    Call<OrderResponse> createOrder(@Body OrderRequest request);

    @GET("api/v1/orders/my-orders")
    Call<List<OrderResponse>> getOrders();

    // Customers
    @GET("api/v1/customers")
    Call<List<CustomerResponse>> getCustomers();

    @POST("api/v1/customers")
    Call<CustomerResponse> createCustomer(@Body CustomerRequest request);
}