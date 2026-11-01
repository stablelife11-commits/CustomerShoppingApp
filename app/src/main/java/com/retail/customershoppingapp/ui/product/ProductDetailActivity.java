package com.retail.customershoppingapp.ui.product;

import android.content.Intent;
import android.graphics.Paint;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.viewpager2.widget.ViewPager2;

import com.retail.customershoppingapp.R;
import com.retail.customershoppingapp.databinding.ActivityProductDetailBinding;
import com.retail.customershoppingapp.model.product.ProductResponse;
import com.retail.customershoppingapp.model.product.VariantResponse;
import com.retail.customershoppingapp.ui.cart.CartViewModel;
import com.retail.customershoppingapp.ui.checkout.CheckoutActivity;

import java.math.BigDecimal;
import java.math.RoundingMode;
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
    private boolean isWishlisted = false;

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
        binding.tvDetailBrand.setText(product.getBrand() != null && !product.getBrand().isEmpty()
                ? product.getBrand().toUpperCase(Locale.ROOT)
                : "FLOWMART WHOLESALE");
        binding.tvDetailName.setText(product.getName() != null ? product.getName() : "Product");

        BigDecimal minPrice = product.getMinPrice();
        binding.tvDetailPrice.setText("₹" + minPrice.stripTrailingZeros().toPlainString());
        if (minPrice.compareTo(BigDecimal.ZERO) > 0) {
            BigDecimal mrp = minPrice.multiply(new BigDecimal("1.25")).setScale(0, RoundingMode.CEILING);
            binding.tvDetailMrp.setText("MRP ₹" + mrp.toPlainString());
            binding.tvDetailMrp.setPaintFlags(binding.tvDetailMrp.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
        }

        StringBuilder descBuilder = new StringBuilder();
        if (product.getDescription() != null && !product.getDescription().isEmpty()) {
            descBuilder.append(product.getDescription()).append("\n\n");
        }
        if (product.getVariants() != null && !product.getVariants().isEmpty()) {
            VariantResponse v0 = product.getVariants().get(0);
            descBuilder.append("• Minimum Order Quantity (MOQ): ").append(v0.getMinimumOrderQuantity()).append(" units\n")
                    .append("• Wholesale Pack Size: ").append(v0.getPackSize()).append(" units per pack");
            if (v0.getGstPercentage() != null && v0.getGstPercentage().doubleValue() > 0) {
                descBuilder.append("\n• GST Rate: ").append(v0.getGstPercentage().stripTrailingZeros().toPlainString()).append("%");
                binding.tvGstBadge.setText("Inclusive of " + v0.getGstPercentage().stripTrailingZeros().toPlainString() + "% GST • B2B Wholesale Rate");
            }
            if (v0.getHsnCode() != null && !v0.getHsnCode().isEmpty()) {
                descBuilder.append(" (HSN Code: ").append(v0.getHsnCode()).append(")");
            }
        }
        binding.tvDetailDesc.setText(descBuilder.toString().trim());

        imageCarouselAdapter = new ImageCarouselAdapter();
        binding.viewPagerImages.setAdapter(imageCarouselAdapter);

        List<String> images = product.getImageUrls();
        if (images == null || images.isEmpty()) {
            images = new ArrayList<>();
            images.add("");
        }
        final int totalImages = images.size();
        imageCarouselAdapter.setImageUrls(images);
        binding.tvImagePageIndicator.setText(String.format(Locale.getDefault(), "1 / %d", totalImages));

        binding.viewPagerImages.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                binding.tvImagePageIndicator.setText(String.format(Locale.getDefault(), "%d / %d", position + 1, totalImages));
            }
        });

        binding.btnDetailWishlist.setOnClickListener(v -> {
            isWishlisted = !isWishlisted;
            binding.btnDetailWishlist.setImageResource(isWishlisted ? R.drawable.ic_heart_filled : R.drawable.ic_heart_outline);
            v.animate().scaleX(1.25f).scaleY(1.25f).setDuration(120)
                    .withEndAction(() -> v.animate().scaleX(1f).scaleY(1f).setDuration(120).start())
                    .start();
        });

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
                Toast.makeText(this, "Please tap a size chip above to select wholesale quantity", Toast.LENGTH_SHORT).show();
                return;
            }

            String buyerNote = binding.etProductNote.getText() != null
                    ? binding.etProductNote.getText().toString().trim()
                    : "";

            int totalItemsAdded = 0;
            for (VariantResponse vResponse : product.getVariants()) {
                if (selections.containsKey(vResponse.getId())) {
                    int qty = selections.get(vResponse.getId());
                    cartViewModel.addToCart(product, vResponse, qty, buyerNote);
                    totalItemsAdded += qty;
                }
            }

            Toast.makeText(this, "Added " + totalItemsAdded + " wholesale units to Cart!", Toast.LENGTH_SHORT).show();
            variantAdapter.clearSelections();
            binding.etProductNote.setText("");
        });

        binding.btnDetailBuyNow.setOnClickListener(v -> {
            HashMap<Long, Integer> selections = variantAdapter.getSelectedQuantities();

            if (selections.isEmpty()) {
                Toast.makeText(this, "Please tap a size chip above to select wholesale quantity", Toast.LENGTH_SHORT).show();
                return;
            }

            String buyerNote = binding.etProductNote.getText() != null
                    ? binding.etProductNote.getText().toString().trim()
                    : "";

            for (VariantResponse vResponse : product.getVariants()) {
                if (selections.containsKey(vResponse.getId())) {
                    int qty = selections.get(vResponse.getId());
                    cartViewModel.addToCart(product, vResponse, qty, buyerNote);
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
                        binding.tvDetailPrice.setText("₹" + variant.getSellingPrice().stripTrailingZeros().toPlainString() + " / pc");
                    } else if (variantAdapter.getSelectedQuantities().isEmpty()) {
                        binding.tvDetailPrice.setText("₹" + product.getMinPrice().stripTrailingZeros().toPlainString());
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }
}