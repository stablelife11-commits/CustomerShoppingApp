package com.retail.customershoppingapp.ui.product;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.retail.customershoppingapp.R;
import com.retail.customershoppingapp.databinding.ItemVariantChipBinding;
import com.retail.customershoppingapp.model.product.VariantResponse;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

public class VariantAdapter extends RecyclerView.Adapter<VariantAdapter.ViewHolder> {

    public interface OnVariantClickListener {
        void onVariantClick(VariantResponse variant);
    }

    private List<VariantResponse> variants = new ArrayList<>();
    private final HashMap<Long, Integer> selectedQuantities = new HashMap<>();
    private final OnVariantClickListener listener;

    public VariantAdapter(OnVariantClickListener listener) {
        this.listener = listener;
    }

    public void setVariants(List<VariantResponse> variants) {
        this.variants = variants != null ? variants : new ArrayList<>();
        this.selectedQuantities.clear();
        notifyDataSetChanged();
    }

    public HashMap<Long, Integer> getSelectedQuantities() {
        return selectedQuantities;
    }

    public void clearSelections() {
        selectedQuantities.clear();
        notifyDataSetChanged();
    }

    public void updateSelection(VariantResponse variant, int qty) {
        if (qty > 0) {
            selectedQuantities.put(variant.getId(), qty);
        } else {
            selectedQuantities.remove(variant.getId());
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemVariantChipBinding binding = ItemVariantChipBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false
        );
        return new ViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.bind(variants.get(position));
    }

    @Override
    public int getItemCount() {
        return variants.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        private final ItemVariantChipBinding binding;

        ViewHolder(@NonNull ItemVariantChipBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        void bind(VariantResponse variant) {
            String sizeLabel = (variant.getSize() != null && !variant.getSize().isEmpty()) ? variant.getSize() : "Standard";
            if (variant.getColor() != null && !variant.getColor().isEmpty() && !sizeLabel.equals("Standard")) {
                sizeLabel += " • " + variant.getColor();
            }
            binding.tvSizeText.setText(sizeLabel);

            int stock = variant.getStock();
            String priceText = variant.getSellingPrice() != null
                    ? "₹" + variant.getSellingPrice().stripTrailingZeros().toPlainString()
                    : "₹0";
            if (stock <= 0) {
                binding.tvVariantChipPrice.setText(priceText + " • Out of Stock");
                binding.tvVariantChipPrice.setTextColor(ContextCompat.getColor(binding.getRoot().getContext(), R.color.error));
            } else {
                binding.tvVariantChipPrice.setText(String.format(Locale.getDefault(), "%s • Stock: %d", priceText, stock));
                binding.tvVariantChipPrice.setTextColor(ContextCompat.getColor(binding.getRoot().getContext(), R.color.text_secondary));
            }

            boolean isSelected = selectedQuantities.containsKey(variant.getId());
            if (isSelected) {
                binding.cardVariant.setStrokeColor(ContextCompat.getColor(binding.getRoot().getContext(), R.color.primary));
                binding.cardVariant.setStrokeWidth(4);
                binding.cardVariant.setCardBackgroundColor(ContextCompat.getColor(binding.getRoot().getContext(), R.color.accent_light));
                binding.tvSelectedQty.setVisibility(View.VISIBLE);

                int qty = selectedQuantities.get(variant.getId());
                binding.tvSelectedQty.setText("✓ " + qty + " pcs");
            } else {
                binding.cardVariant.setStrokeColor(ContextCompat.getColor(binding.getRoot().getContext(), R.color.border));
                binding.cardVariant.setStrokeWidth(2);
                binding.cardVariant.setCardBackgroundColor(ContextCompat.getColor(binding.getRoot().getContext(), R.color.surface));
                binding.tvSelectedQty.setVisibility(View.GONE);
            }

            binding.getRoot().setOnClickListener(v -> {
                if (listener != null) {
                    listener.onVariantClick(variant);
                }
            });
        }
    }
}