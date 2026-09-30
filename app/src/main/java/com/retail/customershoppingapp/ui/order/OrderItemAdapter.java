package com.retail.customershoppingapp.ui.order;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.retail.customershoppingapp.databinding.ItemOrderProductBinding;
import com.retail.customershoppingapp.model.order.OrderItemResponse;

import java.util.ArrayList;
import java.util.List;

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
            binding.tvProductName.setText(name + " (x" + qty + ")");

            binding.tvProductPrice.setText("₹" + (item.getPrice() != null ? item.getPrice() : "0.00"));
        }
    }
}
