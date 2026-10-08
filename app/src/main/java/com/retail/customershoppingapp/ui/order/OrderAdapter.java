package com.retail.customershoppingapp.ui.order;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.retail.customershoppingapp.databinding.ItemOrderCardBinding;
import com.retail.customershoppingapp.model.order.OrderItemResponse;
import com.retail.customershoppingapp.model.order.OrderResponse;

import java.util.ArrayList;
import java.util.List;

public class OrderAdapter extends RecyclerView.Adapter<OrderAdapter.ViewHolder> {

    public interface OnOrderClickListener {
        void onOrderClick(OrderResponse order);
    }

    private List<OrderResponse> orders = new ArrayList<>();
    private OnOrderClickListener listener;

    // 🟢 SAFEGUARD 1: Agar Fragment me 'new OrderAdapter()' use hua hai
    public OrderAdapter() {
    }

    // 🟢 SAFEGUARD 2: Agar Fragment me 'new OrderAdapter(this)' use hua hai
    public OrderAdapter(OnOrderClickListener listener) {
        this.listener = listener;
    }

    public void setOrders(List<OrderResponse> orders) {
        this.orders = orders != null ? orders : new ArrayList<>();
        notifyDataSetChanged();
    }

    public void setOnOrderClickListener(OnOrderClickListener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemOrderCardBinding binding = ItemOrderCardBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false
        );
        return new ViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        OrderResponse order = orders.get(position);

        holder.binding.tvOrderId.setText("Order #" + order.getId());
        holder.binding.tvOrderTotal.setText(String.format("Total: ₹%.2f", order.getTotalAmount()));

        String date = order.getOrderDate() != null ? order.getOrderDate().split("T")[0] : "Recent";
        holder.binding.tvOrderDate.setText("Date: " + date);

        String status = order.getStatus() != null ? order.getStatus().toUpperCase() : "PLACED";
        holder.binding.tvOrderStatus.setText(status);

        if ("CONFIRMED".equals(status) || "DELIVERED".equals(status)) {
            holder.binding.tvOrderStatus.setTextColor(Color.parseColor("#388E3C"));
        } else if ("CANCELLED".equals(status)) {
            holder.binding.tvOrderStatus.setTextColor(Color.parseColor("#EF4444"));
        } else {
            holder.binding.tvOrderStatus.setTextColor(Color.parseColor("#F59E0B"));
        }

        StringBuilder itemsStr = new StringBuilder();
        if (order.getItems() != null) {
            for (OrderItemResponse item : order.getItems()) {
                String size = item.getSize() != null ? item.getSize() : "";
                String color = item.getColor() != null ? item.getColor() : "";

                itemsStr.append("• ").append(item.getProductName())
                        .append(" (").append(size).append(" ").append(color).append(")")
                        .append(" x").append(item.getQuantity())
                        .append("\n");
            }
        }
        holder.binding.tvOrderItems.setText(itemsStr.toString().trim());

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onOrderClick(order);
            }
        });
    }

    @Override
    public int getItemCount() {
        return orders.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        final ItemOrderCardBinding binding;

        ViewHolder(ItemOrderCardBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}