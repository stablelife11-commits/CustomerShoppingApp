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
import java.util.Locale;

public class OrderAdapter extends RecyclerView.Adapter<OrderAdapter.ViewHolder> {

    public interface OnOrderClickListener {
        void onOrderClick(OrderResponse order);
    }

    private List<OrderResponse> orders = new ArrayList<>();
    private OnOrderClickListener listener;

    public OrderAdapter() {
    }

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

        StringBuilder totalLabel = new StringBuilder(
                String.format(Locale.getDefault(), "Net Total: ₹%.2f", order.getNetPayableAmount())
        );
        if (order.getDiscountAmount() > 0) {
            totalLabel.append(String.format(Locale.getDefault(), " (Discount: -₹%.2f)", order.getDiscountAmount()));
        }
        if (order.getReturnedAmount() > 0) {
            totalLabel.append(String.format(Locale.getDefault(), " (Return Credit: -₹%.2f)", order.getReturnedAmount()));
        }
        holder.binding.tvOrderTotal.setText(totalLabel.toString());

        String date = order.getOrderDate() != null ? order.getOrderDate().split("T")[0] : "Recent";
        holder.binding.tvOrderDate.setText("Date: " + date + " • Payment: " + order.getPaymentStatus());

        String status = order.getStatus() != null ? order.getStatus().toUpperCase(Locale.ROOT) : "PLACED";
        String badgeText = "CONFIRMED".equals(status) ? "SELLER CONFIRMED" : status;
        if ("DELIVERED".equals(status) && order.getReturnedAmount() > 0) {
            badgeText = "DELIVERED • RETURN CREDITED";
        }
        holder.binding.tvOrderStatus.setText(badgeText);

        if ("CONFIRMED".equals(status) || "DELIVERED".equals(status)) {
            holder.binding.tvOrderStatus.setTextColor(Color.parseColor("#388E3C"));
        } else if ("CANCELLED".equals(status) || "REJECTED".equals(status)) {
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
                        .append(" — ").append(item.getQuantity())
                        .append(" × ₹").append(item.getPrice() != null ? item.getPrice() : "0.00");

                if (item.getItemNote() != null && !item.getItemNote().trim().isEmpty()) {
                    itemsStr.append(" [Note: ").append(item.getItemNote().trim()).append("]");
                }
                itemsStr.append("\n");
            }
        }
        if (order.getSellerRemarks() != null && !order.getSellerRemarks().trim().isEmpty()) {
            itemsStr.append("💬 Seller Note: ").append(order.getSellerRemarks().trim());
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