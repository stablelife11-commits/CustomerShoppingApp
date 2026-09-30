package com.retail.customershoppingapp.ui.profile;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.retail.customershoppingapp.databinding.FragmentProfileBinding;
import com.retail.customershoppingapp.session.SessionManager;
import com.retail.customershoppingapp.ui.auth.AuthViewModel;
import com.retail.customershoppingapp.ui.auth.LoginActivity;

public class ProfileFragment extends Fragment {

    private FragmentProfileBinding binding;
    private AuthViewModel authViewModel;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentProfileBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        authViewModel = new ViewModelProvider(this).get(AuthViewModel.class);
        SessionManager sessionManager = authViewModel.getSessionManager();

        binding.tvUserName.setText(sessionManager.getUserName().isEmpty() ? "Retail Flow Customer" : sessionManager.getUserName());
        binding.tvUserEmail.setText(sessionManager.getUserEmail().isEmpty() ? "customer@retailflow.com" : sessionManager.getUserEmail());

        binding.itemAbout.setOnClickListener(v ->
                Toast.makeText(requireContext(), "Retail Flow App v1.0.0", Toast.LENGTH_SHORT).show()
        );

        binding.itemLogout.setOnClickListener(v -> {
            authViewModel.logout();
            Intent intent = new Intent(requireContext(), LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
