package com.retail.customershoppingapp.ui.checkout;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.retail.customershoppingapp.databinding.ActivityCheckoutBinding;
import com.retail.customershoppingapp.model.cart.CartItem;
import com.retail.customershoppingapp.model.order.OrderItemRequest;
import com.retail.customershoppingapp.model.order.OrderRequest;
import com.retail.customershoppingapp.model.order.OrderResponse;
import com.retail.customershoppingapp.network.Resource;
import com.retail.customershoppingapp.session.SessionManager;
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

                BigDecimal total = BigDecimal.ZERO;
                for (CartItem item : cartItems) {
                    BigDecimal itemPrice = BigDecimal.ZERO;
                    if (item.getVariant() != null && item.getVariant().getSellingPrice() != null) {
                        itemPrice = item.getVariant().getSellingPrice();
                    }
                    BigDecimal quantity = BigDecimal.valueOf(item.getQuantity());
                    total = total.add(itemPrice.multiply(quantity));
                }

                binding.tvTotalAmount.setText("₹" + total.setScale(2, RoundingMode.HALF_UP).toPlainString());
            } else {
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
            orderItems.add(new OrderItemRequest(variantId, item.getQuantity(), item.getItemNote()));
        }

        SessionManager sm = new SessionManager(this);
        Long customerId = sm.getCustomerId() > 0 ? sm.getCustomerId() : null;
        OrderRequest request = new OrderRequest(customerId, address, orderItems);

        binding.progressBar.setVisibility(View.VISIBLE);
        binding.btnPlaceOrder.setEnabled(false);

        orderViewModel.checkoutOrders(request).observe(this, resource -> {
            if (resource == null) return;

            if (resource.status == Resource.Status.LOADING) {
                binding.progressBar.setVisibility(View.VISIBLE);
            } else if (resource.status == Resource.Status.SUCCESS) {
                binding.progressBar.setVisibility(View.GONE);
                binding.btnPlaceOrder.setEnabled(true);

                List<OrderResponse> createdOrders = resource.data;
                cartViewModel.clearCart();

                Intent intent = new Intent(this, OrderSuccessActivity.class);
                if (createdOrders != null && !createdOrders.isEmpty()) {
                    OrderResponse first = createdOrders.get(0);
                    StringBuilder ids = new StringBuilder();
                    for (OrderResponse o : createdOrders) {
                        if (ids.length() > 0) ids.append(", ");
                        ids.append("#").append(o.getId());
                    }
                    String refText = createdOrders.size() == 1
                            ? "Order #" + first.getId()
                            : createdOrders.size() + " Seller Orders (" + ids + ")";
                    intent.putExtra("order_ref", refText);
                    intent.putExtra("order_count", createdOrders.size());
                    intent.putExtra("order_data", first);
                }
                startActivity(intent);
                finish();
            } else if (resource.status == Resource.Status.ERROR) {
                binding.progressBar.setVisibility(View.GONE);
                binding.btnPlaceOrder.setEnabled(true);
                Toast.makeText(this, resource.message, Toast.LENGTH_LONG).show();
            }
        });
    }
}