package com.retail.customershoppingapp.ui.product;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.retail.customershoppingapp.R;
import com.retail.customershoppingapp.databinding.ActivityProductDetailBinding;
import com.retail.customershoppingapp.model.product.ProductResponse;
import com.retail.customershoppingapp.model.product.VariantResponse;
import com.retail.customershoppingapp.ui.cart.CartViewModel;
import com.retail.customershoppingapp.ui.checkout.CheckoutActivity;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class ProductDetailActivity extends AppCompatActivity implements VariantAdapter.OnVariantSelectedListener {

    private ActivityProductDetailBinding binding;
    private CartViewModel cartViewModel;
    private ProductResponse product;
    private VariantResponse selectedVariant;
    private VariantAdapter variantAdapter;
    private ImageCarouselAdapter imageCarouselAdapter;
    private int quantity = 1;

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

        boolean isActive = Boolean.TRUE.equals(product.getActive());
        binding.tvDetailStatus.setText(isActive ? "IN STOCK" : "OUT OF STOCK");

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
            binding.tvVariantLabel.setVisibility(View.GONE);
            binding.rvVariants.setVisibility(View.GONE);
            binding.tvDetailPrice.setText("₹" + product.getMinPrice());
        }

        binding.btnQtyPlus.setOnClickListener(v -> {
            quantity++;
            binding.tvDetailQty.setText(String.valueOf(quantity));
        });

        binding.btnQtyMinus.setOnClickListener(v -> {
            if (quantity > 1) {
                quantity--;
                binding.tvDetailQty.setText(String.valueOf(quantity));
            }
        });

        binding.btnDetailAddCart.setOnClickListener(v -> {
            cartViewModel.addToCart(product, selectedVariant, quantity);
            Toast.makeText(ProductDetailActivity.this, "Added to Cart (" + quantity + ")", Toast.LENGTH_SHORT).show();
        });

        binding.btnDetailBuyNow.setOnClickListener(v -> {
            cartViewModel.addToCart(product, selectedVariant, quantity);
            Intent intent = new Intent(ProductDetailActivity.this, CheckoutActivity.class);
            startActivity(intent);
        });
    }

    @Override
    public void onVariantSelected(VariantResponse variant) {
        this.selectedVariant = variant;
        if (variant != null && variant.getSellingPrice() != null) {
            binding.tvDetailPrice.setText("₹" + variant.getSellingPrice());
        } else {
            binding.tvDetailPrice.setText("₹" + product.getMinPrice());
        }
    }
}
