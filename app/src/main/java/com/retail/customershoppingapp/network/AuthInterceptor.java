package com.retail.customershoppingapp.network;

import androidx.annotation.NonNull;

import com.retail.customershoppingapp.session.SessionManager;

import java.io.IOException;

import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

public class AuthInterceptor implements Interceptor {
    private final SessionManager sessionManager;

    public AuthInterceptor(SessionManager sessionManager) {
        this.sessionManager = sessionManager;
    }

    @NonNull
    @Override
    public Response intercept(@NonNull Chain chain) throws IOException {
        Request.Builder builder = chain.request().newBuilder();
        if (sessionManager != null && sessionManager.isLoggedIn()) {
            String token = sessionManager.getAuthToken();
            if (token != null && !token.isEmpty()) {
                builder.addHeader("Authorization", "Bearer " + token);
            }
        }
        return chain.proceed(builder.build());
    }
}
