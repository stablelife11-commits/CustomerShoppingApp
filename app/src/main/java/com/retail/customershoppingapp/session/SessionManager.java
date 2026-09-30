package com.retail.customershoppingapp.session;

import android.content.Context;
import android.content.SharedPreferences;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.retail.customershoppingapp.model.cart.CartItem;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class SessionManager {
    private static final String PREF_NAME = "RetailFlowSession";
    private static final String KEY_TOKEN = "jwt_token";
    private static final String KEY_EMAIL = "user_email";
    private static final String KEY_NAME = "user_name";
    private static final String KEY_ROLE = "user_role";
    private static final String KEY_CUSTOMER_ID = "customer_id";
    private static final String KEY_CART = "cart_items";

    private final SharedPreferences pref;
    private final SharedPreferences.Editor editor;
    private final Gson gson;

    public SessionManager(Context context) {
        pref = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        editor = pref.edit();
        gson = new Gson();
    }

    public void saveAuthToken(String token) {
        editor.putString(KEY_TOKEN, token);
        editor.apply();
    }

    public String getAuthToken() {
        return pref.getString(KEY_TOKEN, null);
    }

    public boolean isLoggedIn() {
        return getAuthToken() != null && !getAuthToken().trim().isEmpty();
    }

    public void saveUserDetails(String email, String name, String role) {
        editor.putString(KEY_EMAIL, email);
        editor.putString(KEY_NAME, name);
        editor.putString(KEY_ROLE, role);
        editor.apply();
    }

    public String getUserEmail() {
        return pref.getString(KEY_EMAIL, "");
    }

    public String getUserName() {
        return pref.getString(KEY_NAME, "");
    }

    public String getUserRole() {
        return pref.getString(KEY_ROLE, "");
    }

    public void saveCustomerId(Long customerId) {
        if (customerId != null) {
            editor.putLong(KEY_CUSTOMER_ID, customerId);
        } else {
            editor.remove(KEY_CUSTOMER_ID);
        }
        editor.apply();
    }

    public Long getCustomerId() {
        long id = pref.getLong(KEY_CUSTOMER_ID, -1L);
        return id != -1L ? id : 1L; // Fallback to 1L if not explicitly set
    }

    public void saveCart(List<CartItem> cartList) {
        String json = gson.toJson(cartList);
        editor.putString(KEY_CART, json);
        editor.apply();
    }

    public List<CartItem> getCart() {
        String json = pref.getString(KEY_CART, null);
        if (json == null) {
            return new ArrayList<>();
        }
        Type type = new TypeToken<List<CartItem>>() {}.getType();
        List<CartItem> cart = gson.fromJson(json, type);
        return cart != null ? cart : new ArrayList<>();
    }

    public void clearCart() {
        editor.remove(KEY_CART);
        editor.apply();
    }

    public void logout() {
        editor.clear();
        editor.apply();
    }
}
