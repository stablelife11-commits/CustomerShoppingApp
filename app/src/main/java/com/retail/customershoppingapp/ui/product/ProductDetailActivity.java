package com.retail.customershoppingapp.ui.product;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.retail.customershoppingapp.databinding.ActivityProductDetailBinding;
import com.retail.customershoppingapp.model.product.ProductResponse;
import com.retail.customershoppingapp.model.product.VariantResponse;
import com.retail.customershoppingapp.ui.cart.CartViewModel;
import com.retail.customershoppingapp.ui.checkout.CheckoutActivity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class ProductDetailActivity extends AppCompatActivity implements VariantAdapter.OnVariantClickListener {

    private ActivityProductDetailBinding binding;
    private CartViewModel cartViewModel;
    private ProductResponse product;
    private VariantAdapter variantAdapter;
    private ImageCarouselAdapter imageCarouselAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityProductDetailBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        cartViewModel = new ViewModelProvider(this).get(CartViewModel.class);
        binding.toolbar.setNavigationOnClickListener(v -> finish());

        if (getIntent().hasExtra("product_data")) {
            product = (ProductResponse) getIntent().getSerializableExtra("product_data");
        }

        if (product == null) {
            Toast.makeText(this, "Product details unavailable", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        setupUI();
    }

    private void setupUI() {
        binding.tvDetailBrand.setText(product.getBrand() != null && !product.getBrand().isEmpty() ?
                product.getBrand().toUpperCase() : "RETAIL FLOW");
        binding.tvDetailName.setText(product.getName() != null ? product.getName() : "Product");
        binding.tvDetailDesc.setText(product.getDescription() != null && !product.getDescription().isEmpty() ?
                product.getDescription() : "No detailed description provided.");

        binding.tvDetailPrice.setText("₹" + product.getMinPrice());

        imageCarouselAdapter = new ImageCarouselAdapter();
        binding.viewPagerImages.setAdapter(imageCarouselAdapter);

        List<String> images = product.getImageUrls();
        if (images == null || images.isEmpty()) {
            images = new ArrayList<>();
            images.add(""); // Placeholder
        }
        imageCarouselAdapter.setImageUrls(images);

        variantAdapter = new VariantAdapter(this);
        binding.rvVariants.setAdapter(variantAdapter);

        if (product.getVariants() != null && !product.getVariants().isEmpty()) {
            variantAdapter.setVariants(product.getVariants());
        } else {
            binding.layoutSelectSize.setVisibility(View.GONE);
        }

        // 🟢 BULK ADD TO CART
        binding.btnDetailAddCart.setOnClickListener(v -> {
            HashMap<Long, Integer> selections = variantAdapter.getSelectedQuantities();

            if (selections.isEmpty()) {
                Toast.makeText(this, "Please select at least one size", Toast.LENGTH_SHORT).show();
                return;
            }

            int totalItemsAdded = 0;
            // Iterate and add all selected sizes to Cart
            for (VariantResponse vResponse : product.getVariants()) {
                if (selections.containsKey(vResponse.getId())) {
                    int qty = selections.get(vResponse.getId());
                    cartViewModel.addToCart(product, vResponse, qty);
                    totalItemsAdded += qty;
                }
            }

            Toast.makeText(this, "Added " + totalItemsAdded + " items to Cart!", Toast.LENGTH_SHORT).show();
            variantAdapter.clearSelections(); // Cart mein dalne ke baad selection clear kar do
        });

        // 🟢 BULK BUY NOW
        binding.btnDetailBuyNow.setOnClickListener(v -> {
            HashMap<Long, Integer> selections = variantAdapter.getSelectedQuantities();

            if (selections.isEmpty()) {
                Toast.makeText(this, "Please select at least one size", Toast.LENGTH_SHORT).show();
                return;
            }

            for (VariantResponse vResponse : product.getVariants()) {
                if (selections.containsKey(vResponse.getId())) {
                    int qty = selections.get(vResponse.getId());
                    cartViewModel.addToCart(product, vResponse, qty);
                }
            }
            Intent intent = new Intent(ProductDetailActivity.this, CheckoutActivity.class);
            startActivity(intent);
        });
    }

    @Override
    public void onVariantClick(VariantResponse variant) {
        // 🟢 "0 (Remove)" option add kiya taaki select karne ke baad deselect bhi kar sakein
        String[] quantities = {"0 (Remove)", "1", "2", "3", "4", "5", "6", "7", "8", "9", "10"};

        new AlertDialog.Builder(this)
                .setTitle("Select Quantity (Size: " + variant.getSize() + ")")
                .setItems(quantities, (dialog, which) -> {
                    int qty = which; // Array index: 0 = Remove, 1 = Qty 1, 2 = Qty 2...

                    // Stock check
                    if (variant.getStock() != null && qty > variant.getStock()) {
                        Toast.makeText(this, "Only " + variant.getStock() + " left in stock!", Toast.LENGTH_LONG).show();
                        qty = variant.getStock();
                    }

                    // Adapter mein map update karein
                    variantAdapter.updateSelection(variant, qty);

                    // Price display update (Jo aakhri size select hoga uska price dikhega)
                    if (variant.getSellingPrice() != null && qty > 0) {
                        binding.tvDetailPrice.setText("₹" + variant.getSellingPrice());
                    } else if (variantAdapter.getSelectedQuantities().isEmpty()) {
                        binding.tvDetailPrice.setText("₹" + product.getMinPrice());
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }
}