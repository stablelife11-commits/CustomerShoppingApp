package com.retail.customershoppingapp.ui.checkout;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.retail.customershoppingapp.MainActivity;
import com.retail.customershoppingapp.databinding.ActivityCheckoutBinding;
import com.retail.customershoppingapp.model.cart.CartItem;
import com.retail.customershoppingapp.model.order.OrderItemRequest;
import com.retail.customershoppingapp.model.order.OrderRequest;
import com.retail.customershoppingapp.network.Resource;
import com.retail.customershoppingapp.ui.cart.CartViewModel;
import com.retail.customershoppingapp.ui.order.OrderViewModel;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

public class CheckoutActivity extends AppCompatActivity {

    private ActivityCheckoutBinding binding;
    private CartViewModel cartViewModel;
    private OrderViewModel orderViewModel;
    private CheckoutAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityCheckoutBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.toolbar.setNavigationOnClickListener(v -> finish());

        cartViewModel = new ViewModelProvider(this).get(CartViewModel.class);
        orderViewModel = new ViewModelProvider(this).get(OrderViewModel.class);

        adapter = new CheckoutAdapter();
        binding.rvCheckoutItems.setLayoutManager(new LinearLayoutManager(this));
        binding.rvCheckoutItems.setAdapter(adapter);

        cartViewModel.getCartItems().observe(this, cartItems -> {
            if (cartItems != null && !cartItems.isEmpty()) {
                adapter.setCartItems(cartItems);

                // 🟢 PRO FIX: Total calculation using BigDecimal
                BigDecimal total = BigDecimal.ZERO;
                for (CartItem item : cartItems) {
                    BigDecimal itemPrice = BigDecimal.ZERO;
                    if (item.getVariant() != null && item.getVariant().getSellingPrice() != null) {
                        itemPrice = item.getVariant().getSellingPrice();
                    }
                    BigDecimal quantity = BigDecimal.valueOf(item.getQuantity());
                    total = total.add(itemPrice.multiply(quantity));
                }

                // BigDecimal formatting without String.format
                binding.tvTotalAmount.setText("₹" + total.setScale(2, RoundingMode.HALF_UP).toPlainString());
            } else {
                Toast.makeText(this, "Cart is empty", Toast.LENGTH_SHORT).show();
                finish();
            }
        });

        binding.btnPlaceOrder.setOnClickListener(v -> placeOrder());
    }

    private void placeOrder() {
        String address = binding.etDeliveryAddress.getText() != null ? binding.etDeliveryAddress.getText().toString().trim() : "";

        if (address.isEmpty()) {
            binding.etDeliveryAddress.setError("Please enter delivery address");
            binding.etDeliveryAddress.requestFocus();
            return;
        }

        List<CartItem> cartItems = cartViewModel.getCartItems().getValue();
        if (cartItems == null || cartItems.isEmpty()) return;

        List<OrderItemRequest> orderItems = new ArrayList<>();
        for (CartItem item : cartItems) {
            Long variantId = (item.getVariant() != null) ? item.getVariant().getId() : 0L;
            orderItems.add(new OrderItemRequest(variantId, item.getQuantity()));
        }

        Long dummyCustomerId = 1L; // Managed by JWT in backend now
        OrderRequest request = new OrderRequest(dummyCustomerId, address, orderItems);

        binding.progressBar.setVisibility(View.VISIBLE);
        binding.btnPlaceOrder.setEnabled(false);

        orderViewModel.createOrder(request).observe(this, resource -> {
            if (resource != null) {
                binding.progressBar.setVisibility(View.GONE);
                binding.btnPlaceOrder.setEnabled(true);

                if (resource.status == Resource.Status.SUCCESS) {
                    Toast.makeText(this, "Order Placed Successfully!", Toast.LENGTH_SHORT).show();
                    cartViewModel.clearCart();

                    Intent intent = new Intent(this, MainActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    finish();
                } else if (resource.status == Resource.Status.ERROR) {
                    Toast.makeText(this, resource.message, Toast.LENGTH_LONG).show();
                }
            }
        });
    }
}