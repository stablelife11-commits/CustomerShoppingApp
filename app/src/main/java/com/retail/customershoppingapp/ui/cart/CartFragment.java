package com.retail.customershoppingapp.ui.cart;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.retail.customershoppingapp.databinding.FragmentCartBinding;
import com.retail.customershoppingapp.model.cart.CartItem;
import com.retail.customershoppingapp.ui.checkout.CheckoutActivity;

import java.util.List;

public class CartFragment extends Fragment implements CartAdapter.OnCartItemActionListener {

    private FragmentCartBinding binding;
    private CartViewModel cartViewModel;
    private CartAdapter cartAdapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentCartBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        cartViewModel = new ViewModelProvider(requireActivity()).get(CartViewModel.class);
        cartAdapter = new CartAdapter(this);
        binding.rvCartItems.setAdapter(cartAdapter);

        cartViewModel.getCartItems().observe(getViewLifecycleOwner(), this::updateCartUI);

        binding.btnCheckout.setOnClickListener(v -> {
            if (cartViewModel.getItemCount() > 0) {
                Intent intent = new Intent(requireContext(), CheckoutActivity.class);
                startActivity(intent);
            }
        });
    }

    private void updateCartUI(List<CartItem> items) {
        if (items == null || items.isEmpty()) {
            binding.layoutEmptyCart.setVisibility(View.VISIBLE);
            binding.layoutCartContent.setVisibility(View.GONE);
        } else {
            binding.layoutEmptyCart.setVisibility(View.GONE);
            binding.layoutCartContent.setVisibility(View.VISIBLE);
            cartAdapter.setCartItems(items);

            binding.tvCartSubtotal.setText("₹" + cartViewModel.getSubtotal());
            binding.tvCartTotal.setText("₹" + cartViewModel.getSubtotal());
        }
    }

    @Override
    public void onQuantityChanged(CartItem item, int newQuantity) {
        cartViewModel.updateQuantity(item, newQuantity);
    }

    @Override
    public void onItemRemoved(CartItem item) {
        cartViewModel.removeFromCart(item);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
