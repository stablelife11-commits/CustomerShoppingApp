package com.retail.customershoppingapp.ui.order;

import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.retail.customershoppingapp.databinding.ActivityOrderDetailBinding;
import com.retail.customershoppingapp.model.order.OrderResponse;
import com.retail.customershoppingapp.network.Resource;

import java.util.Locale;

public class OrderDetailActivity extends AppCompatActivity {

    private ActivityOrderDetailBinding binding;
    private OrderItemAdapter adapter;
    private OrderViewModel orderViewModel;
    private OrderResponse order;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityOrderDetailBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        orderViewModel = new ViewModelProvider(this).get(OrderViewModel.class);
        binding.toolbar.setNavigationOnClickListener(v -> finish());

        adapter = new OrderItemAdapter();
        binding.rvOrderDetailItems.setAdapter(adapter);

        if (getIntent().hasExtra("order_data")) {
            order = (OrderResponse) getIntent().getSerializableExtra("order_data");
            if (order != null) {
                bindOrderDetails(order);
            }
        }
    }

    private void bindOrderDetails(OrderResponse currentOrder) {
        String status = currentOrder.getStatus().toUpperCase(Locale.ROOT);
        binding.tvDetailOrderId.setText(String.format(Locale.getDefault(),
                "Order #%d • %s", currentOrder.getId(), status));

        String dateStr = currentOrder.getOrderDate() != null
                ? currentOrder.getOrderDate().replace("T", " ")
                : "Recently";
        String addressStr = currentOrder.getDeliveryAddress() != null
                ? "\nDelivery Address: " + currentOrder.getDeliveryAddress()
                : "";
        String paymentStr = "\nPayment Status: " + currentOrder.getPaymentStatus()
                + " (Paid: ₹" + String.format(Locale.getDefault(), "%.2f", currentOrder.getPaidAmount()) + ")";

        if ("PLACED".equals(status)) {
            paymentStr += "\n\n[Tap here to Cancel Order]";
            binding.tvDetailOrderDate.setOnClickListener(v -> confirmCancelOrder(currentOrder.getId()));
        } else {
            binding.tvDetailOrderDate.setOnClickListener(null);
        }

        binding.tvDetailOrderDate.setText("Placed on: " + dateStr + addressStr + paymentStr);
        binding.tvDetailOrderTotal.setText(String.format(Locale.getDefault(), "₹%.2f", currentOrder.getTotalAmount()));

        if (currentOrder.getItems() != null) {
            adapter.setItems(currentOrder.getItems());
        }
    }

    private void confirmCancelOrder(Long orderId) {
        new AlertDialog.Builder(this)
                .setTitle("Cancel Order #" + orderId + "?")
                .setMessage("Are you sure you want to cancel this order? Reserved stock will be restored immediately.")
                .setPositiveButton("Yes, Cancel Order", (dialog, which) -> {
                    orderViewModel.cancelOrder(orderId).observe(this, resource -> {
                        if (resource == null) return;
                        if (resource.status == Resource.Status.SUCCESS && resource.data != null) {
                            Toast.makeText(this, "Order cancelled successfully.", Toast.LENGTH_SHORT).show();
                            bindOrderDetails(resource.data);
                        } else if (resource.status == Resource.Status.ERROR) {
                            Toast.makeText(this, resource.message, Toast.LENGTH_LONG).show();
                        }
                    });
                })
                .setNegativeButton("Keep Order", null)
                .show();
    }
}