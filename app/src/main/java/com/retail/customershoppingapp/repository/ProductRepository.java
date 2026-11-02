package com.retail.customershoppingapp.repository;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.retail.customershoppingapp.model.product.ProductResponse;
import com.retail.customershoppingapp.network.ApiService;
import com.retail.customershoppingapp.network.Resource;
import com.retail.customershoppingapp.network.RetrofitClient;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ProductRepository {
    private final ApiService apiService;

    public ProductRepository(Context context) {
        this.apiService = RetrofitClient.getInstance(context).getApi();
    }

    public LiveData<Resource<List<ProductResponse>>> getProducts(int page, int size) {
        return getProducts(page, size, null, null, null);
    }

    public LiveData<Resource<List<ProductResponse>>> getProducts(
            int page, int size, String keyword, String category, String brand) {
        MutableLiveData<Resource<List<ProductResponse>>> result = new MutableLiveData<>();
        result.setValue(Resource.loading(null));

        String cleanKeyword = (keyword != null && !keyword.trim().isEmpty()) ? keyword.trim() : null;
        String cleanCategory = (category != null && !category.trim().isEmpty() && !"ALL".equalsIgnoreCase(category.trim()))
                ? category.trim() : null;
        String cleanBrand = (brand != null && !brand.trim().isEmpty()) ? brand.trim() : null;

        apiService.getProducts(page, size, cleanKeyword, cleanCategory, cleanBrand).enqueue(new Callback<List<ProductResponse>>() {
            @Override
            public void onResponse(@NonNull Call<List<ProductResponse>> call, @NonNull Response<List<ProductResponse>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    result.setValue(Resource.success(response.body()));
                } else {
                    result.setValue(Resource.error("Failed to load products (" + response.code() + ")", null));
                }
            }

            @Override
            public void onFailure(@NonNull Call<List<ProductResponse>> call, @NonNull Throwable t) {
                result.setValue(Resource.error("Unable to connect to server: " + t.getLocalizedMessage(), null));
            }
        });

        return result;
    }

    public LiveData<Resource<ProductResponse>> getProductById(Long productId) {
        MutableLiveData<Resource<ProductResponse>> result = new MutableLiveData<>();
        result.setValue(Resource.loading(null));

        apiService.getProductById(productId).enqueue(new Callback<ProductResponse>() {
            @Override
            public void onResponse(@NonNull Call<ProductResponse> call, @NonNull Response<ProductResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    result.setValue(Resource.success(response.body()));
                } else {
                    result.setValue(Resource.error("Product not found.", null));
                }
            }

            @Override
            public void onFailure(@NonNull Call<ProductResponse> call, @NonNull Throwable t) {
                result.setValue(Resource.error("Unable to load product: " + t.getLocalizedMessage(), null));
            }
        });

        return result;
    }
}