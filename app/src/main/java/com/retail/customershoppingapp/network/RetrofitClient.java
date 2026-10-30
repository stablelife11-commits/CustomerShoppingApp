package com.retail.customershoppingapp.network;

import android.content.Context;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.retail.customershoppingapp.BuildConfig;
import com.retail.customershoppingapp.session.SessionManager;

import java.util.Map;
import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import okhttp3.ResponseBody;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class RetrofitClient {
    private static RetrofitClient instance;
    private final ApiService apiService;

    private RetrofitClient(Context context) {
        SessionManager sessionManager = new SessionManager(context.getApplicationContext());

        HttpLoggingInterceptor loggingInterceptor = new HttpLoggingInterceptor();
        loggingInterceptor.setLevel(BuildConfig.DEBUG ? HttpLoggingInterceptor.Level.BODY : HttpLoggingInterceptor.Level.NONE);

        OkHttpClient okHttpClient = new OkHttpClient.Builder()
                .connectTimeout(30, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .writeTimeout(30, TimeUnit.SECONDS)
                .addInterceptor(new AuthInterceptor(context.getApplicationContext(), sessionManager))
                .addInterceptor(loggingInterceptor)
                .build();

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(BuildConfig.BASE_URL)
                .client(okHttpClient)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        apiService = retrofit.create(ApiService.class);
    }

    public static synchronized RetrofitClient getInstance(Context context) {
        if (instance == null) {
            instance = new RetrofitClient(context);
        }
        return instance;
    }

    public ApiService getApi() {
        return apiService;
    }

    public static String extractErrorMessage(Response<?> response, String fallbackMessage) {
        if (response == null) return fallbackMessage;
        if (response.code() == 429) {
            return "Too many attempts. Please wait a minute and try again.";
        }
        try {
            ResponseBody errorBody = response.errorBody();
            if (errorBody != null) {
                String raw = errorBody.string();
                JsonElement element = JsonParser.parseString(raw);
                if (element != null && element.isJsonObject()) {
                    JsonObject obj = element.getAsJsonObject();
                    if (obj.has("fieldErrors") && obj.get("fieldErrors").isJsonObject()) {
                        JsonObject fields = obj.getAsJsonObject("fieldErrors");
                        StringBuilder sb = new StringBuilder();
                        for (Map.Entry<String, JsonElement> entry : fields.entrySet()) {
                            if (sb.length() > 0) sb.append("\n");
                            sb.append("• ").append(entry.getValue().getAsString());
                        }
                        if (sb.length() > 0) return sb.toString();
                    }
                    if (obj.has("message") && !obj.get("message").isJsonNull()) {
                        return obj.get("message").getAsString();
                    }
                }
            }
        } catch (Exception ignored) {
        }
        return fallbackMessage + " (HTTP " + response.code() + ")";
    }
}