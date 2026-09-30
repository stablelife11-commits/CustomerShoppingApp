package com.retail.customershoppingapp.repository;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.retail.customershoppingapp.model.customer.CustomerRequest;
import com.retail.customershoppingapp.model.customer.CustomerResponse;
import com.retail.customershoppingapp.network.ApiService;
import com.retail.customershoppingapp.network.Resource;
import com.retail.customershoppingapp.network.RetrofitClient;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CustomerRepository {
    private final ApiService apiService;

    public CustomerRepository(Context context) {
        this.apiService = RetrofitClient.getInstance(context).getApi();
    }

    public LiveData<Resource<List<CustomerResponse>>> getCustomers() {
        MutableLiveData<Resource<List<CustomerResponse>>> result = new MutableLiveData<>();
        result.setValue(Resource.loading(null));

        apiService.getCustomers().enqueue(new Callback<List<CustomerResponse>>() {
            @Override
            public void onResponse(@NonNull Call<List<CustomerResponse>> call, @NonNull Response<List<CustomerResponse>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    result.setValue(Resource.success(response.body()));
                } else {
                    result.setValue(Resource.error("Failed to load customer profile.", null));
                }
            }

            @Override
            public void onFailure(@NonNull Call<List<CustomerResponse>> call, @NonNull Throwable t) {
                result.setValue(Resource.error("Network error: " + t.getLocalizedMessage(), null));
            }
        });

        return result;
    }

    public LiveData<Resource<CustomerResponse>> createCustomer(CustomerRequest request) {
        MutableLiveData<Resource<CustomerResponse>> result = new MutableLiveData<>();
        result.setValue(Resource.loading(null));

        apiService.createCustomer(request).enqueue(new Callback<CustomerResponse>() {
            @Override
            public void onResponse(@NonNull Call<CustomerResponse> call, @NonNull Response<CustomerResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    result.setValue(Resource.success(response.body()));
                } else {
                    result.setValue(Resource.error("Failed to create customer profile.", null));
                }
            }

            @Override
            public void onFailure(@NonNull Call<CustomerResponse> call, @NonNull Throwable t) {
                result.setValue(Resource.error("Network error: " + t.getLocalizedMessage(), null));
            }
        });

        return result;
    }
}
