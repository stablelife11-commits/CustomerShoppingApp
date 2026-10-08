package com.retail.customershoppingapp.ui.product;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.retail.customershoppingapp.R;
import com.retail.customershoppingapp.databinding.ItemVariantChipBinding;
import com.retail.customershoppingapp.model.product.VariantResponse;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class VariantAdapter extends RecyclerView.Adapter<VariantAdapter.ViewHolder> {

    public interface OnVariantClickListener {
        void onVariantClick(VariantResponse variant);
    }

    private List<VariantResponse> variants = new ArrayList<>();
    // 🟢 MULTI-SELECTION MAP: Variant ID ke sath uski Quantity save karega
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

    // 🟢 Activity ko map bhejne ke liye
    public HashMap<Long, Integer> getSelectedQuantities() {
        return selectedQuantities;
    }

    public void clearSelections() {
        selectedQuantities.clear();
        notifyDataSetChanged();
    }

    // 🟢 NAYA LOGIC: Multiple items ki quantity update karna
    public void updateSelection(VariantResponse variant, int qty) {
        if (qty > 0) {
            selectedQuantities.put(variant.getId(), qty);
        } else {
            selectedQuantities.remove(variant.getId()); // Agar user 0 select kare to hata do
        }
        notifyDataSetChanged(); // UI refresh
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
                sizeLabel += " (" + variant.getColor() + ")";
            }
            binding.tvSizeText.setText(sizeLabel);

            // 🟢 Check if this specific variant is in our HashMap
            boolean isSelected = selectedQuantities.containsKey(variant.getId());

            if (isSelected) {
                // Selected UI
                binding.cardVariant.setStrokeColor(binding.getRoot().getContext().getColor(R.color.primary));
                binding.cardVariant.setStrokeWidth(4);
                binding.cardVariant.setCardBackgroundColor(binding.getRoot().getContext().getColor(R.color.accent_light));
                binding.tvSelectedQty.setVisibility(View.VISIBLE);

                int qty = selectedQuantities.get(variant.getId());
                binding.tvSelectedQty.setText("Qty: " + qty);
            } else {
                // Normal UI
                binding.cardVariant.setStrokeColor(binding.getRoot().getContext().getColor(R.color.border));
                binding.cardVariant.setStrokeWidth(2);
                binding.cardVariant.setCardBackgroundColor(binding.getRoot().getContext().getColor(R.color.surface));
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