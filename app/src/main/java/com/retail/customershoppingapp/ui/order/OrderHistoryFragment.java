package com.retail.customershoppingapp.ui.order;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.retail.customershoppingapp.databinding.FragmentOrderHistoryBinding;
import com.retail.customershoppingapp.model.order.OrderResponse;
import com.retail.customershoppingapp.network.Resource;

import java.util.ArrayList;
import java.util.List;

public class OrderHistoryFragment extends Fragment implements OrderAdapter.OnOrderClickListener {

    private FragmentOrderHistoryBinding binding;
    private OrderViewModel orderViewModel;
    private OrderAdapter orderAdapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentOrderHistoryBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        orderViewModel = new ViewModelProvider(this).get(OrderViewModel.class);
        orderAdapter = new OrderAdapter(this);
        binding.rvOrders.setAdapter(orderAdapter);

        binding.swipeRefresh.setOnRefreshListener(this::loadOrders);
        loadOrders();
    }

    private void loadOrders() {
        orderViewModel.getOrders().observe(getViewLifecycleOwner(), resource -> {
            if (resource == null) return;

            binding.swipeRefresh.setRefreshing(false);

            if (resource.status == Resource.Status.LOADING) {
                binding.progressBar.setVisibility(View.VISIBLE);
                binding.layoutEmptyOrders.setVisibility(View.GONE);
            } else if (resource.status == Resource.Status.SUCCESS) {
                binding.progressBar.setVisibility(View.GONE);

                List<OrderResponse> orders = resource.data != null ? resource.data : new ArrayList<>();
                if (orders.isEmpty()) {
                    binding.layoutEmptyOrders.setVisibility(View.VISIBLE);
                    binding.rvOrders.setVisibility(View.GONE);
                } else {
                    binding.layoutEmptyOrders.setVisibility(View.GONE);
                    binding.rvOrders.setVisibility(View.VISIBLE);
                    orderAdapter.setOrders(orders);
                }
            } else if (resource.status == Resource.Status.ERROR) {
                binding.progressBar.setVisibility(View.GONE);
                binding.layoutEmptyOrders.setVisibility(View.VISIBLE);
            }
        });
    }

    @Override
    public void onOrderClick(OrderResponse order) {
        Intent intent = new Intent(requireContext(), OrderDetailActivity.class);
        intent.putExtra("order_data", order);
        startActivity(intent);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
