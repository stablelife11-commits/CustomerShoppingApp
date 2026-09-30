package com.retail.customershoppingapp.ui.order;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.retail.customershoppingapp.databinding.ItemOrderCardBinding;
import com.retail.customershoppingapp.model.order.OrderResponse;

import java.util.ArrayList;
import java.util.List;

public class OrderAdapter extends RecyclerView.Adapter<OrderAdapter.ViewHolder> {

    public interface OnOrderClickListener {
        void onOrderClick(OrderResponse order);
    }

    private List<OrderResponse> orders = new ArrayList<>();
    private final OnOrderClickListener listener;

    public OrderAdapter(OnOrderClickListener listener) {
        this.listener = listener;
    }

    public void setOrders(List<OrderResponse> orders) {
        this.orders = orders != null ? orders : new ArrayList<>();
        notifyDataSetChanged();
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
        holder.bind(orders.get(position));
    }

    @Override
    public int getItemCount() {
        return orders.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        private final ItemOrderCardBinding binding;

        ViewHolder(@NonNull ItemOrderCardBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        void bind(OrderResponse order) {
            binding.tvOrderId.setText("Order #" + (order.getId() != null ? order.getId() : "N/A"));
            binding.tvOrderDate.setText("Placed on: " + (order.getOrderDate() != null ? order.getOrderDate() : "Recently"));

            int itemCount = order.getItems() != null ? order.getItems().size() : 0;
            binding.tvItemCount.setText(itemCount + " Item" + (itemCount == 1 ? "" : "s"));

            binding.tvOrderTotal.setText("₹" + (order.getTotalAmount() != null ? order.getTotalAmount() : "0.00"));
            binding.tvOrderStatus.setText("PLACED");

            binding.btnOrderDetails.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onOrderClick(order);
                }
            });

            binding.getRoot().setOnClickListener(v -> {
                if (listener != null) {
                    listener.onOrderClick(order);
                }
            });
        }
    }
}
