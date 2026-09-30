package com.retail.customershoppingapp.ui.auth;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import com.retail.customershoppingapp.model.auth.AuthResponse;
import com.retail.customershoppingapp.network.Resource;
import com.retail.customershoppingapp.repository.AuthRepository;
import com.retail.customershoppingapp.session.SessionManager;

public class AuthViewModel extends AndroidViewModel {
    private final AuthRepository authRepository;

    public AuthViewModel(@NonNull Application application) {
        super(application);
        authRepository = new AuthRepository(application);
    }

    public LiveData<Resource<AuthResponse>> login(String email, String password) {
        return authRepository.login(email, password);
    }

    public LiveData<Resource<AuthResponse>> register(String name, String email, String password) {
        return authRepository.register(name, email, password);
    }

    public boolean isLoggedIn() {
        return authRepository.getSessionManager().isLoggedIn();
    }

    public SessionManager getSessionManager() {
        return authRepository.getSessionManager();
    }

    public void logout() {
        authRepository.getSessionManager().logout();
    }
}
