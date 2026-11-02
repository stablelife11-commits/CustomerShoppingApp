package com.retail.customershoppingapp.ui.checkout;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.retail.customershoppingapp.MainActivity;
import com.retail.customershoppingapp.databinding.ActivityOrderSuccessBinding;
import com.retail.customershoppingapp.model.order.OrderResponse;
import com.retail.customershoppingapp.ui.order.OrderDetailActivity;

public class OrderSuccessActivity extends AppCompatActivity {

    private ActivityOrderSuccessBinding binding;
    private OrderResponse orderResponse;
    private int orderCount = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityOrderSuccessBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        String orderRef = getIntent().getStringExtra("order_ref");
        if (orderRef != null) {
            binding.tvOrderRef.setText(orderRef);
        }

        orderCount = getIntent().getIntExtra("order_count", 1);
        if (orderCount > 1) {
            binding.btnViewOrder.setText("View All " + orderCount + " Seller Orders");
        }

        if (getIntent().hasExtra("order_data")) {
            orderResponse = (OrderResponse) getIntent().getSerializableExtra("order_data");
        }

        binding.btnViewOrder.setOnClickListener(v -> {
            if (orderCount > 1) {
                Intent intent = new Intent(OrderSuccessActivity.this, MainActivity.class);
                intent.putExtra("open_tab", "orders");
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
            } else if (orderResponse != null) {
                Intent intent = new Intent(OrderSuccessActivity.this, OrderDetailActivity.class);
                intent.putExtra("order_data", orderResponse);
                startActivity(intent);
            }
            finish();
        });

        binding.btnContinueShopping.setOnClickListener(v -> {
            Intent intent = new Intent(OrderSuccessActivity.this, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        });
    }
}