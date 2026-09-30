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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityOrderSuccessBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        String orderRef = getIntent().getStringExtra("order_ref");
        if (orderRef != null) {
            binding.tvOrderRef.setText(orderRef);
        }

        if (getIntent().hasExtra("order_data")) {
            orderResponse = (OrderResponse) getIntent().getSerializableExtra("order_data");
        }

        binding.btnViewOrder.setOnClickListener(v -> {
            if (orderResponse != null) {
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
