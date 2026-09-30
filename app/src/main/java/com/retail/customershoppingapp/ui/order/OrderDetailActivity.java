package com.retail.customershoppingapp.ui.order;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.retail.customershoppingapp.databinding.ActivityOrderDetailBinding;
import com.retail.customershoppingapp.model.order.OrderResponse;

public class OrderDetailActivity extends AppCompatActivity {

    private ActivityOrderDetailBinding binding;
    private OrderItemAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityOrderDetailBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.toolbar.setNavigationOnClickListener(v -> finish());

        adapter = new OrderItemAdapter();
        binding.rvOrderDetailItems.setAdapter(adapter);

        if (getIntent().hasExtra("order_data")) {
            OrderResponse order = (OrderResponse) getIntent().getSerializableExtra("order_data");
            if (order != null) {
                binding.tvDetailOrderId.setText("Order #" + (order.getId() != null ? order.getId() : "N/A"));
                binding.tvDetailOrderDate.setText("Placed on: " + (order.getOrderDate() != null ? order.getOrderDate() : "Recently"));
                binding.tvDetailOrderTotal.setText("₹" + (order.getTotalAmount() != null ? order.getTotalAmount() : "0.00"));

                if (order.getItems() != null) {
                    adapter.setItems(order.getItems());
                }
            }
        }
    }
}
