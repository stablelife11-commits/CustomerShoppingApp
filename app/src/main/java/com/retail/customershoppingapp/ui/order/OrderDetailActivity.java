package com.retail.customershoppingapp.ui.order;

import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.ViewModelProvider;

import com.retail.customershoppingapp.R;
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
                refreshLatestOrderFromServer(order.getId());
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (order != null && order.getId() != null) {
            refreshLatestOrderFromServer(order.getId());
        }
    }

    private void refreshLatestOrderFromServer(Long orderId) {
        if (orderId == null) return;
        orderViewModel.getOrderById(orderId).observe(this, resource -> {
            if (resource != null && resource.status == Resource.Status.SUCCESS && resource.data != null) {
                order = resource.data;
                bindOrderDetails(order);
            }
        });
    }

    private void bindOrderDetails(OrderResponse currentOrder) {
        String status = currentOrder.getStatus().toUpperCase(Locale.ROOT);
        String badgeText = "CONFIRMED".equals(status) ? "SELLER CONFIRMED" : status;
        if ("DELIVERED".equals(status) && currentOrder.getReturnedAmount() > 0) {
            badgeText = "DELIVERED • RETURN CREDITED";
        }

        binding.tvDetailOrderId.setText(String.format(Locale.getDefault(),
                "Order #%d • %s", currentOrder.getId(), badgeText));

        StringBuilder info = new StringBuilder();
        String dateStr = currentOrder.getOrderDate() != null
                ? currentOrder.getOrderDate().replace("T", " ")
                : "Recently";
        if (dateStr.length() > 16) dateStr = dateStr.substring(0, 16);
        info.append("Placed on: ").append(dateStr);

        if (currentOrder.getDeliveryAddress() != null && !currentOrder.getDeliveryAddress().isEmpty()) {
            info.append("\n📍 Delivery Address: ").append(currentOrder.getDeliveryAddress());
        }

        if (currentOrder.getDiscountAmount() > 0) {
            info.append(String.format(Locale.getDefault(),
                    "\n💵 Subtotal: ₹%.2f | Seller Discount: -₹%.2f",
                    currentOrder.getSubtotalAmount(), currentOrder.getDiscountAmount()));
        }

        if (currentOrder.getTaxAmount() > 0) {
            info.append(String.format(Locale.getDefault(),
                    "\n🧾 Included GST Tax: ₹%.2f", currentOrder.getTaxAmount()));
        }

        if (currentOrder.getReturnedAmount() > 0) {
            info.append(String.format(Locale.getDefault(),
                    "\n↩️ Returned Items Credit: -₹%.2f (Original Total: ₹%.2f)",
                    currentOrder.getReturnedAmount(), currentOrder.getTotalAmount()));
        }

        if (currentOrder.getSellerRemarks() != null && !currentOrder.getSellerRemarks().trim().isEmpty()) {
            info.append("\n💬 Seller Note: \"").append(currentOrder.getSellerRemarks().trim()).append("\"");
        }

        info.append(String.format(Locale.getDefault(),
                "\n💳 Payment Status: %s (Paid: ₹%.2f",
                currentOrder.getPaymentStatus(), currentOrder.getPaidAmount()));

        if (currentOrder.getRefundCreditAmount() > 0.009) {
            info.append(String.format(Locale.getDefault(),
                    " | Refund / Credit Due: ₹%.2f)", currentOrder.getRefundCreditAmount()));
        } else {
            info.append(String.format(Locale.getDefault(),
                    " | Balance Due: ₹%.2f)", currentOrder.getBalanceDueAmount()));
        }

        binding.tvDetailOrderDate.setText(info.toString());
        binding.tvDetailOrderTotal.setText(String.format(Locale.getDefault(), "₹%.2f", currentOrder.getNetPayableAmount()));

        if (currentOrder.getItems() != null) {
            adapter.setItems(currentOrder.getItems());
        }

        // Render Dynamic Status Timeline
        int green = ContextCompat.getColor(this, R.color.success);
        int grey = ContextCompat.getColor(this, R.color.text_secondary);
        int red = ContextCompat.getColor(this, R.color.error);

        binding.timelinePlaced.setText("✓ Order Placed by Buyer");
        binding.timelinePlaced.setTextColor(green);

        boolean isConfirmed = "CONFIRMED".equals(status) || "PROCESSING".equals(status) || "SHIPPED".equals(status) || "DELIVERED".equals(status);
        boolean isProcessing = "PROCESSING".equals(status) || "SHIPPED".equals(status) || "DELIVERED".equals(status);
        boolean isShipped = "SHIPPED".equals(status) || "DELIVERED".equals(status);
        boolean isDelivered = "DELIVERED".equals(status);
        boolean isCancelled = "CANCELLED".equals(status) || "REJECTED".equals(status);

        binding.timelineConfirmed.setText(isConfirmed ? "✓ Seller Confirmed Lot" : "○ Seller Confirmation Pending");
        binding.timelineConfirmed.setTextColor(isConfirmed ? green : (isCancelled ? red : grey));

        binding.timelineProcessing.setText(isProcessing ? "✓ Godown Processing & Packing" : "○ Godown Processing");
        binding.timelineProcessing.setTextColor(isProcessing ? green : grey);

        binding.timelineShipped.setText(isShipped ? "✓ Dispatched / Out for Delivery" : "○ Out for Delivery");
        binding.timelineShipped.setTextColor(isShipped ? green : grey);

        if (isDelivered && currentOrder.getReturnedAmount() > 0) {
            binding.timelineDelivered.setText(String.format(Locale.getDefault(),
                    "✓ Delivered to Store (Return Credit: -₹%.2f)", currentOrder.getReturnedAmount()));
        } else {
            binding.timelineDelivered.setText(isDelivered ? "✓ Delivered to Store" : "○ Delivered");
        }
        binding.timelineDelivered.setTextColor(isDelivered ? green : grey);

        if ("PLACED".equals(status)) {
            binding.btnCancelOrder.setVisibility(View.VISIBLE);
            binding.btnCancelOrder.setOnClickListener(v -> confirmCancelOrder(currentOrder.getId()));
        } else {
            binding.btnCancelOrder.setVisibility(View.GONE);
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