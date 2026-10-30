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
import java.util.Locale;

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

        StringBuilder descBuilder = new StringBuilder();
        if (product.getDescription() != null && !product.getDescription().isEmpty()) {
            descBuilder.append(product.getDescription()).append("\n\n");
        }
        if (product.getVariants() != null && !product.getVariants().isEmpty()) {
            VariantResponse v0 = product.getVariants().get(0);
            descBuilder.append("• Wholesale MOQ: ").append(v0.getMinimumOrderQuantity()).append(" units\n")
                    .append("• Pack / Set Size: ").append(v0.getPackSize()).append(" units per pack");
            if (v0.getGstPercentage() != null && v0.getGstPercentage().doubleValue() > 0) {
                descBuilder.append("\n• GST: ").append(v0.getGstPercentage().stripTrailingZeros().toPlainString()).append("%");
            }
            if (v0.getHsnCode() != null && !v0.getHsnCode().isEmpty()) {
                descBuilder.append(" (HSN: ").append(v0.getHsnCode()).append(")");
            }
        }
        binding.tvDetailDesc.setText(descBuilder.toString().trim());

        binding.tvDetailPrice.setText("₹" + product.getMinPrice());

        imageCarouselAdapter = new ImageCarouselAdapter();
        binding.viewPagerImages.setAdapter(imageCarouselAdapter);

        List<String> images = product.getImageUrls();
        if (images == null || images.isEmpty()) {
            images = new ArrayList<>();
            images.add("");
        }
        imageCarouselAdapter.setImageUrls(images);

        variantAdapter = new VariantAdapter(this);
        binding.rvVariants.setAdapter(variantAdapter);

        if (product.getVariants() != null && !product.getVariants().isEmpty()) {
            variantAdapter.setVariants(product.getVariants());
        } else {
            binding.layoutSelectSize.setVisibility(View.GONE);
        }

        binding.btnDetailAddCart.setOnClickListener(v -> {
            HashMap<Long, Integer> selections = variantAdapter.getSelectedQuantities();

            if (selections.isEmpty()) {
                Toast.makeText(this, "Please tap a size chip above to select quantity", Toast.LENGTH_SHORT).show();
                return;
            }

            int totalItemsAdded = 0;
            for (VariantResponse vResponse : product.getVariants()) {
                if (selections.containsKey(vResponse.getId())) {
                    int qty = selections.get(vResponse.getId());
                    cartViewModel.addToCart(product, vResponse, qty);
                    totalItemsAdded += qty;
                }
            }

            Toast.makeText(this, "Added " + totalItemsAdded + " wholesale units to Cart!", Toast.LENGTH_SHORT).show();
            variantAdapter.clearSelections();
        });

        binding.btnDetailBuyNow.setOnClickListener(v -> {
            HashMap<Long, Integer> selections = variantAdapter.getSelectedQuantities();

            if (selections.isEmpty()) {
                Toast.makeText(this, "Please tap a size chip above to select quantity", Toast.LENGTH_SHORT).show();
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
        int stock = variant.getStock();
        int moq = variant.getMinimumOrderQuantity();
        int packSize = variant.getPackSize();

        if (stock < moq) {
            Toast.makeText(this,
                    "Insufficient stock (" + stock + " available, MOQ is " + moq + ")",
                    Toast.LENGTH_LONG).show();
            return;
        }

        List<Integer> validQuantities = new ArrayList<>();
        List<String> labels = new ArrayList<>();

        validQuantities.add(0);
        labels.add("0 (Remove Selection)");

        // Generate up to 15 wholesale quantity steps starting at MOQ and stepping by packSize
        int maxSteps = 15;
        int stepCount = 0;
        for (int q = moq; q <= stock && stepCount < maxSteps; q += packSize) {
            validQuantities.add(q);
            if (packSize > 1) {
                labels.add(String.format(Locale.getDefault(), "%d units (%d packs × %d) — ₹%.2f",
                        q, q / packSize, packSize,
                        variant.getSellingPrice().doubleValue() * q));
            } else {
                labels.add(String.format(Locale.getDefault(), "%d units — ₹%.2f",
                        q, variant.getSellingPrice().doubleValue() * q));
            }
            stepCount++;
        }

        String title = String.format(Locale.getDefault(),
                "Size %s (MOQ: %d | Pack: %d | Stock: %d)",
                variant.getSize() != null ? variant.getSize() : "Std",
                moq, packSize, stock);

        new AlertDialog.Builder(this)
                .setTitle(title)
                .setItems(labels.toArray(new String[0]), (dialog, which) -> {
                    int selectedQty = validQuantities.get(which);
                    variantAdapter.updateSelection(variant, selectedQty);

                    if (variant.getSellingPrice() != null && selectedQty > 0) {
                        binding.tvDetailPrice.setText("₹" + variant.getSellingPrice() + " / unit");
                    } else if (variantAdapter.getSelectedQuantities().isEmpty()) {
                        binding.tvDetailPrice.setText("₹" + product.getMinPrice());
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }
}