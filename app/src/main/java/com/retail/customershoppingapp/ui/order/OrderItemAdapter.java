package com.retail.customershoppingapp.ui.order;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.retail.customershoppingapp.databinding.ItemOrderProductBinding;
import com.retail.customershoppingapp.model.order.OrderItemResponse;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class OrderItemAdapter extends RecyclerView.Adapter<OrderItemAdapter.ViewHolder> {

    private List<OrderItemResponse> items = new ArrayList<>();

    public void setItems(List<OrderItemResponse> items) {
        this.items = items != null ? items : new ArrayList<>();
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemOrderProductBinding binding = ItemOrderProductBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false
        );
        return new ViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.bind(items.get(position));
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        private final ItemOrderProductBinding binding;

        ViewHolder(@NonNull ItemOrderProductBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        void bind(OrderItemResponse item) {
            String name = item.getProductName() != null ? item.getProductName() : "Product";
            int qty = item.getQuantity() != null ? item.getQuantity() : 1;
            BigDecimal unitPrice = item.getPrice() != null ? item.getPrice() : BigDecimal.ZERO;
            BigDecimal lineTotal = unitPrice.multiply(BigDecimal.valueOf(qty));

            StringBuilder label = new StringBuilder(name);
            if (item.getSize() != null || item.getColor() != null) {
                label.append(" (")
                        .append(item.getSize() != null ? item.getSize() : "")
                        .append(item.getColor() != null ? " " + item.getColor() : "")
                        .append(")");
            }
            label.append("\nConfirmed Qty: ").append(qty)
                    .append(" × ₹").append(String.format(Locale.getDefault(), "%.2f", unitPrice));

            if (item.getItemNote() != null && !item.getItemNote().trim().isEmpty()) {
                label.append("\n📝 Note: \"").append(item.getItemNote().trim()).append("\"");
            }

            binding.tvProductName.setText(label.toString());
            binding.tvProductPrice.setText(String.format(Locale.getDefault(), "₹%.2f", lineTotal));
        }
    }
}