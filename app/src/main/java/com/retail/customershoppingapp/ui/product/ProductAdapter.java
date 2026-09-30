package com.retail.customershoppingapp.ui.product;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.retail.customershoppingapp.R;
import com.retail.customershoppingapp.databinding.ItemProductCardBinding;
import com.retail.customershoppingapp.model.product.ProductResponse;

import java.util.ArrayList;
import java.util.List;

public class ProductAdapter extends RecyclerView.Adapter<ProductAdapter.ViewHolder> {

    public interface OnProductClickListener {
        void onProductClick(ProductResponse product);
        void onAddToCartClick(ProductResponse product);
    }

    private List<ProductResponse> products = new ArrayList<>();
    private final OnProductClickListener listener;

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
            binding.tvName.setText(product.getName() != null ? product.getName() : "Product");
            binding.tvBrand.setText(product.getBrand() != null && !product.getBrand().isEmpty() ?
                    product.getBrand().toUpperCase() : "RETAIL FLOW");

            binding.tvPrice.setText("₹" + product.getMinPrice());

            boolean isActive = Boolean.TRUE.equals(product.getActive());
            binding.tvBadge.setText(isActive ? "IN STOCK" : "OUT OF STOCK");
            binding.tvBadge.setBackgroundResource(isActive ? R.color.accent_light : R.color.divider);

            String imageUrl = product.getPrimaryImageUrl();
            if (imageUrl != null && !imageUrl.isEmpty()) {
                Glide.with(binding.getRoot().getContext())
                        .load(imageUrl)
                        .placeholder(R.drawable.ic_launcher_background)
                        .error(R.drawable.ic_launcher_background)
                        .into(binding.ivProduct);
            } else {
                binding.ivProduct.setImageResource(R.drawable.ic_launcher_background);
            }

            binding.getRoot().setOnClickListener(v -> {
                if (listener != null) listener.onProductClick(product);
            });

            binding.btnAddCart.setOnClickListener(v -> {
                if (listener != null) listener.onAddToCartClick(product);
            });
        }
    }
}
