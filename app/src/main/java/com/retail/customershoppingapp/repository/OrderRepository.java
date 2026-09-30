package com.retail.customershoppingapp.repository;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.retail.customershoppingapp.model.order.OrderRequest;
import com.retail.customershoppingapp.model.order.OrderResponse;
import com.retail.customershoppingapp.network.ApiService;
import com.retail.customershoppingapp.network.Resource;
import com.retail.customershoppingapp.network.RetrofitClient;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class OrderRepository {
    private final ApiService apiService;

    public OrderRepository(Context context) {
        this.apiService = RetrofitClient.getInstance(context).getApi();
    }

    public LiveData<Resource<OrderResponse>> createOrder(OrderRequest orderRequest) {
        MutableLiveData<Resource<OrderResponse>> result = new MutableLiveData<>();
        result.setValue(Resource.loading(null));

        apiService.createOrder(orderRequest).enqueue(new Callback<OrderResponse>() {
            @Override
            public void onResponse(@NonNull Call<OrderResponse> call, @NonNull Response<OrderResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    result.setValue(Resource.success(response.body()));
                } else {
                    result.setValue(Resource.error("Failed to place order. Status: " + response.code(), null));
                }
            }

            @Override
            public void onFailure(@NonNull Call<OrderResponse> call, @NonNull Throwable t) {
                result.setValue(Resource.error("Network error while placing order: " + t.getLocalizedMessage(), null));
            }
        });

        return result;
    }

    public LiveData<Resource<List<OrderResponse>>> getOrders() {
        MutableLiveData<Resource<List<OrderResponse>>> result = new MutableLiveData<>();
        result.setValue(Resource.loading(null));

        apiService.getOrders().enqueue(new Callback<List<OrderResponse>>() {
            @Override
            public void onResponse(@NonNull Call<List<OrderResponse>> call, @NonNull Response<List<OrderResponse>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    result.setValue(Resource.success(response.body()));
                } else {
                    result.setValue(Resource.error("Unable to load orders.", null));
                }
            }

            @Override
            public void onFailure(@NonNull Call<List<OrderResponse>> call, @NonNull Throwable t) {
                result.setValue(Resource.error("Network error: " + t.getLocalizedMessage(), null));
            }
        });

        return result;
    }
}
