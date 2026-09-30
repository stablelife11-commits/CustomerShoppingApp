package com.retail.customershoppingapp.repository;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.retail.customershoppingapp.model.auth.AuthResponse;
import com.retail.customershoppingapp.model.auth.LoginRequest;
import com.retail.customershoppingapp.model.auth.RegisterRequest;
import com.retail.customershoppingapp.network.ApiService;
import com.retail.customershoppingapp.network.Resource;
import com.retail.customershoppingapp.network.RetrofitClient;
import com.retail.customershoppingapp.session.SessionManager;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AuthRepository {
    private final ApiService apiService;
    private final SessionManager sessionManager;

    public AuthRepository(Context context) {
        this.apiService = RetrofitClient.getInstance(context).getApi();
        this.sessionManager = new SessionManager(context);
    }

    public LiveData<Resource<AuthResponse>> login(String email, String password) {
        MutableLiveData<Resource<AuthResponse>> result = new MutableLiveData<>();
        result.setValue(Resource.loading(null));

        LoginRequest request = new LoginRequest(email, password);
        apiService.login(request).enqueue(new Callback<AuthResponse>() {
            @Override
            public void onResponse(@NonNull Call<AuthResponse> call, @NonNull Response<AuthResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    AuthResponse auth = response.body();
                    sessionManager.saveAuthToken(auth.getToken());
                    sessionManager.saveUserDetails(auth.getEmail(), auth.getName(), auth.getRole());
                    result.setValue(Resource.success(auth));
                } else {
                    String msg = "Login failed. Please check your credentials.";
                    if (response.code() == 401) {
                        msg = "Invalid email or password.";
                    }
                    result.setValue(Resource.error(msg, null));
                }
            }

            @Override
            public void onFailure(@NonNull Call<AuthResponse> call, @NonNull Throwable t) {
                result.setValue(Resource.error("Network error: " + t.getLocalizedMessage(), null));
            }
        });

        return result;
    }

    public LiveData<Resource<AuthResponse>> register(String name, String email, String password) {
        MutableLiveData<Resource<AuthResponse>> result = new MutableLiveData<>();
        result.setValue(Resource.loading(null));

        RegisterRequest request = new RegisterRequest(name, email, password, "BUYER");
        apiService.register(request).enqueue(new Callback<AuthResponse>() {
            @Override
            public void onResponse(@NonNull Call<AuthResponse> call, @NonNull Response<AuthResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    AuthResponse auth = response.body();
                    sessionManager.saveAuthToken(auth.getToken());
                    sessionManager.saveUserDetails(auth.getEmail(), auth.getName(), auth.getRole());
                    result.setValue(Resource.success(auth));
                } else {
                    result.setValue(Resource.error("Registration failed. Email may already be in use.", null));
                }
            }

            @Override
            public void onFailure(@NonNull Call<AuthResponse> call, @NonNull Throwable t) {
                result.setValue(Resource.error("Network error: " + t.getLocalizedMessage(), null));
            }
        });

        return result;
    }

    public SessionManager getSessionManager() {
        return sessionManager;
    }
}
