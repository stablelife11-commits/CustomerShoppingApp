package com.retail.customershoppingapp.repository;

import android.content.Context;

import com.retail.customershoppingapp.session.SessionManager;

public class CustomerRepository {
    private final SessionManager sessionManager;

    public CustomerRepository(Context context) {
        this.sessionManager = new SessionManager(context);
    }

    public SessionManager getSessionManager() {
        return sessionManager;
    }
}