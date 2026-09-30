package com.retail.customershoppingapp.ui.checkout;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.retail.customershoppingapp.databinding.ActivityCheckoutBinding;
import com.retail.customershoppingapp.model.cart.CartItem;
import com.retail.customershoppingapp.model.order.OrderItemRequest;
import com.retail.customershoppingapp.model.order.OrderItemResponse;
import com.retail.customershoppingapp.model.order.OrderResponse;
import com.retail.customershoppingapp.network.Resource;
import com.retail.customershoppingapp.session.SessionManager;
import com.retail.customershoppingapp.ui.cart.CartViewModel;
import com.retail.customershoppingapp.ui.order.OrderItemAdapter;
import com.retail.customershoppingapp.ui.order.OrderViewModel;

import java.util.ArrayList;
import java.util.List;

public class CheckoutActivity extends AppCompatActivity {

    private ActivityCheckoutBinding binding;
    private CartViewModel cartViewModel;
    private OrderViewModel orderViewModel;
    private SessionManager sessionManager;
    private OrderItemAdapter orderItemAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityCheckoutBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        cartViewModel = new ViewModelProvider(this).get(CartViewModel.class);
        orderViewModel = new ViewModelProvider(this).get(OrderViewModel.class);
        sessionManager = new SessionManager(this);

        binding.toolbar.setNavigationOnClickListener(v -> finish());

        orderItemAdapter = new OrderItemAdapter();
        binding.rvCheckoutItems.setAdapter(orderItemAdapter);

        setupData();

        binding.btnPlaceOrder.setOnClickListener(v -> attemptPlaceOrder());
    }

    private void setupData() {
        binding.etAddressName.setText(sessionManager.getUserName());
        binding.tvCheckoutTotal.setText("₹" + cartViewModel.getSubtotal());

        List<CartItem> cartItems = cartViewModel.getCartItems().getValue();
        List<OrderItemResponse> previewItems = new ArrayList<>();
        if (cartItems != null) {
            for (CartItem ci : cartItems) {
                OrderItemResponse item = new OrderItemResponse();
                if (ci.getProduct() != null) {
                    item.setProductId(ci.getProduct().getId());
                    item.setProductName(ci.getProduct().getName());
                }
                item.setQuantity(ci.getQuantity());
                item.setPrice(ci.getTotalPrice());
                previewItems.add(item);
            }
        }
        orderItemAdapter.setItems(previewItems);
    }

    private void attemptPlaceOrder() {
        List<CartItem> cartItems = cartViewModel.getCartItems().getValue();
        if (cartItems == null || cartItems.isEmpty()) {
            Toast.makeText(this, "Your cart is empty", Toast.LENGTH_SHORT).show();
            return;
        }

        List<OrderItemRequest> orderItems = new ArrayList<>();
        for (CartItem ci : cartItems) {
            Long variantId = (ci.getVariant() != null && ci.getVariant().getId() != null) ?
                    ci.getVariant().getId() : 1L; // Fallback to 1L if no variant
            orderItems.add(new OrderItemRequest(variantId, ci.getQuantity()));
        }

        Long customerId = sessionManager.getCustomerId();

        binding.btnPlaceOrder.setEnabled(false);
        binding.btnPlaceOrder.setText("Placing Order...");

        orderViewModel.createOrder(customerId, orderItems).observe(this, resource -> {
            if (resource == null) return;

            if (resource.status == Resource.Status.LOADING) {
                binding.btnPlaceOrder.setEnabled(false);
            } else if (resource.status == Resource.Status.SUCCESS) {
                cartViewModel.clearCart();

                OrderResponse response = resource.data;
                String orderRef = response != null && response.getId() != null ?
                        "RF-" + response.getId() : "RF-" + System.currentTimeMillis() % 10000;

                Intent intent = new Intent(CheckoutActivity.this, OrderSuccessActivity.class);
                intent.putExtra("order_ref", orderRef);
                intent.putExtra("order_data", response);
                startActivity(intent);
                finish();
            } else if (resource.status == Resource.Status.ERROR) {
                binding.btnPlaceOrder.setEnabled(true);
                binding.btnPlaceOrder.setText("Place Order");
                Toast.makeText(CheckoutActivity.this, resource.message, Toast.LENGTH_LONG).show();
            }
        });
    }
}
