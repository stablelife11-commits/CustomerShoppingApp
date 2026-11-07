package com.retail.customershoppingapp.ui.product;

import android.graphics.Paint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.retail.customershoppingapp.R;
import com.retail.customershoppingapp.databinding.ItemProductCardBinding;
import com.retail.customershoppingapp.model.product.ProductResponse;
import com.retail.customershoppingapp.model.product.VariantResponse;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

public class ProductAdapter extends RecyclerView.Adapter<ProductAdapter.ViewHolder> {

    public interface OnProductClickListener {
        void onProductClick(ProductResponse product);
        void onAddToCartClick(ProductResponse product);
    }

    private List<ProductResponse> products = new ArrayList<>();
    private final OnProductClickListener listener;
    private final Set<Long> wishlistedIds = new HashSet<>();

    public ProductAdapter(OnProductClickListener listener) {
        this.listener = listener;
    }

    public void setProducts(List<ProductResponse> products) {
        this.products = products != null ? products : new ArrayList<>();
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemProductCardBinding binding = ItemProductCardBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false
        );
        return new ViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.bind(products.get(position));
    }

    @Override
    public int getItemCount() {
        return products.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        private final ItemProductCardBinding binding;

        ViewHolder(@NonNull ItemProductCardBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        void bind(ProductResponse product) {
            binding.tvName.setText(product.getName() != null ? product.getName() : "Wholesale Product");
            binding.tvBrand.setText(product.getBrand() != null && !product.getBrand().isEmpty()
                    ? product.getBrand().toUpperCase(Locale.ROOT)
                    : (product.getCategory() != null ? product.getCategory().toUpperCase(Locale.ROOT) : "FLOWMART B2B"));

            BigDecimal minPrice = product.getMinPrice();
            binding.tvPrice.setText("₹" + minPrice.stripTrailingZeros().toPlainString());

            // Compute realistic B2B retail MRP reference (+25%) for strike-through display
            if (minPrice.compareTo(BigDecimal.ZERO) > 0) {
                BigDecimal mrp = minPrice.multiply(new BigDecimal("1.25")).setScale(0, RoundingMode.CEILING);
                binding.tvMrpStrike.setVisibility(View.VISIBLE);
                binding.tvMrpStrike.setText("₹" + mrp.toPlainString());
                binding.tvMrpStrike.setPaintFlags(binding.tvMrpStrike.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
                binding.tvDiscountBadge.setVisibility(View.VISIBLE);
                binding.tvDiscountBadge.setText("BUY");
            } else {
                binding.tvMrpStrike.setVisibility(View.GONE);
                binding.tvDiscountBadge.setVisibility(View.GONE);
            }

            // Deterministic rating badge based on product ID
            long idSeed = product.getId() != null ? product.getId() : 4L;
            double rating = 4.1 + ((idSeed % 8) * 0.1);
            binding.tvRatingChip.setText(String.format(Locale.US, "%.1f", rating));

            // Extract B2B MOQ, Pack Size & Total Stock from variants
            int moq = 1;
            int packSize = 1;
            int totalStock = 0;
            if (product.getVariants() != null && !product.getVariants().isEmpty()) {
                VariantResponse firstVar = product.getVariants().get(0);
                moq = firstVar.getMinimumOrderQuantity();
                packSize = firstVar.getPackSize();
                for (VariantResponse v : product.getVariants()) {
                    totalStock += v.getStock();
                }
            }

            if (packSize > 1) {
                binding.tvBadge.setText(String.format(Locale.getDefault(), "Min Order: %d Box • Pack of %d", moq, packSize));
            } else {
                binding.tvBadge.setText(String.format(Locale.getDefault(), "Min Order: %d Box", moq));
            }

            boolean isActive = Boolean.TRUE.equals(product.getActive()) && totalStock >= moq;
            if (!isActive) {
                binding.tvStockOverlay.setVisibility(View.VISIBLE);
                binding.tvStockOverlay.setText(R.string.out_of_stock);
                binding.tvStockOverlay.setBackgroundColor(ContextCompat.getColor(binding.getRoot().getContext(), R.color.scrim_dark));
                binding.btnAddCart.setEnabled(false);
                binding.btnAddCart.setText(R.string.out_of_stock);
            } else if (totalStock < moq * 3) {
                binding.tvStockOverlay.setVisibility(View.VISIBLE);
                binding.tvStockOverlay.setText("Only " + totalStock + " left");
                binding.tvStockOverlay.setBackgroundColor(ContextCompat.getColor(binding.getRoot().getContext(), R.color.warning));
                binding.btnAddCart.setEnabled(true);
                binding.btnAddCart.setText(moq > 1 ? "+ Add " + moq + " pcs" : "+ Add to Cart");
            } else {
                binding.tvStockOverlay.setVisibility(View.GONE);
                binding.btnAddCart.setEnabled(true);
                binding.btnAddCart.setText(moq > 1 ? "+ Add " + moq + " pcs" : "+ Add to Cart");
            }

            // Wishlist Heart Toggle
            boolean isWishlisted = product.getId() != null && wishlistedIds.contains(product.getId());
            binding.btnWishlist.setImageResource(isWishlisted ? R.drawable.ic_heart_filled : R.drawable.ic_heart_outline);
            binding.btnWishlist.setOnClickListener(v -> {
                if (product.getId() == null) return;
                if (wishlistedIds.contains(product.getId())) {
                    wishlistedIds.remove(product.getId());
                    binding.btnWishlist.setImageResource(R.drawable.ic_heart_outline);
                } else {
                    wishlistedIds.add(product.getId());
                    binding.btnWishlist.setImageResource(R.drawable.ic_heart_filled);
                }
                v.animate().scaleX(1.25f).scaleY(1.25f).setDuration(120)
                        .withEndAction(() -> v.animate().scaleX(1f).scaleY(1f).setDuration(120).start())
                        .start();
            });

            String imageUrl = product.getPrimaryImageUrl();
            if (imageUrl != null && !imageUrl.isEmpty()) {
                Glide.with(binding.getRoot().getContext())
                        .load(imageUrl)
                        .placeholder(R.drawable.ic_product_placeholder)
                        .error(R.drawable.ic_product_placeholder)
                        .into(binding.ivProduct);
            } else {
                binding.ivProduct.setImageResource(R.drawable.ic_product_placeholder);
            }

            binding.getRoot().setOnClickListener(v -> {
                if (listener != null) listener.onProductClick(product);
            });

            final int finalMoq = moq;
            binding.btnAddCart.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onAddToCartClick(product);
                    binding.btnAddCart.setText("✓ Added (" + finalMoq + ")");
                    binding.btnAddCart.postDelayed(() ->
                            binding.btnAddCart.setText(finalMoq > 1 ? "+ Add " + finalMoq + " pcs" : "+ Add to Cart"), 1200);
                }
            });
        }
    }
}