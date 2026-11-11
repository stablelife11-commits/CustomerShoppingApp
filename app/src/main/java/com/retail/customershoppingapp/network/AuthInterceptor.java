package com.retail.customershoppingapp.network;

import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Looper;

import androidx.annotation.NonNull;

import com.retail.customershoppingapp.session.SessionManager;
import com.retail.customershoppingapp.ui.auth.LoginActivity;

import java.io.IOException;

import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

public class AuthInterceptor implements Interceptor {
    private final Context appContext;
    private final SessionManager sessionManager;

    public AuthInterceptor(Context appContext, SessionManager sessionManager) {
        this.appContext = appContext;
        this.sessionManager = sessionManager;
    }

    public AuthInterceptor(SessionManager sessionManager) {
        this.appContext = null;
        this.sessionManager = sessionManager;
    }

    @NonNull
    @Override
    public Response intercept(@NonNull Chain chain) throws IOException {
        Request original = chain.request();
        String path = original.url().encodedPath();
        Request.Builder builder = original.newBuilder();

        boolean isPublicEndpoint = path.contains("/auth/") || path.contains("/app-update/");

        if (sessionManager != null && sessionManager.isLoggedIn() && !isPublicEndpoint) {
            String token = sessionManager.getAuthToken();
            if (token != null && !token.isEmpty()) {
                builder.header("Authorization", "Bearer " + token);
            }
        }

        Response response = chain.proceed(builder.build());

        if (response.code() == 401 && !isPublicEndpoint && sessionManager != null) {
            sessionManager.logout();
            if (appContext != null) {
                new Handler(Looper.getMainLooper()).post(() -> {
                    Intent intent = new Intent(appContext, LoginActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    appContext.startActivity(intent);
                });
            }
        }
        return response;
    }
}