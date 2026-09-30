package com.retail.customershoppingapp.ui.product;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.retail.customershoppingapp.databinding.ItemVariantChipBinding;
import com.retail.customershoppingapp.model.product.VariantResponse;

import java.util.ArrayList;
import java.util.List;

public class VariantAdapter extends RecyclerView.Adapter<VariantAdapter.ViewHolder> {

    public interface OnVariantSelectedListener {
        void onVariantSelected(VariantResponse variant);
    }

    private List<VariantResponse> variants = new ArrayList<>();
    private VariantResponse selectedVariant;
    private final OnVariantSelectedListener listener;

    public VariantAdapter(OnVariantSelectedListener listener) {
        this.listener = listener;
    }

    public void setVariants(List<VariantResponse> variants) {
        this.variants = variants != null ? variants : new ArrayList<>();
        if (!this.variants.isEmpty()) {
            this.selectedVariant = this.variants.get(0);
            if (listener != null) {
                listener.onVariantSelected(selectedVariant);
            }
        }
        notifyDataSetChanged();
    }

    public VariantResponse getSelectedVariant() {
        return selectedVariant;
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
            StringBuilder label = new StringBuilder();
            if (variant.getSize() != null && !variant.getSize().isEmpty()) {
                label.append("Size: ").append(variant.getSize()).append(" ");
            }
            if (variant.getColor() != null && !variant.getColor().isEmpty()) {
                label.append("Color: ").append(variant.getColor()).append(" ");
            }
            if (variant.getSellingPrice() != null) {
                label.append("(₹").append(variant.getSellingPrice()).append(")");
            }

            binding.chipVariant.setText(label.toString());

            boolean isSelected = selectedVariant != null && selectedVariant.getId() != null &&
                    selectedVariant.getId().equals(variant.getId());
            binding.chipVariant.setChecked(isSelected);

            binding.chipVariant.setOnClickListener(v -> {
                selectedVariant = variant;
                notifyDataSetChanged();
                if (listener != null) {
                    listener.onVariantSelected(variant);
                }
            });
        }
    }
}
