package com.retail.customershoppingapp.ui.checkout;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.retail.customershoppingapp.R;
import com.retail.customershoppingapp.model.cart.CartItem;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

public class CheckoutAdapter extends RecyclerView.Adapter<CheckoutAdapter.ViewHolder> {
    private List<CartItem> cartItems = new ArrayList<>();

    public void setCartItems(List<CartItem> cartItems) {
        this.cartItems = cartItems != null ? cartItems : new ArrayList<>();
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_checkout_card, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        CartItem item = cartItems.get(position);

        String name = (item.getProduct() != null) ? item.getProduct().getName() : "Product";
        String size = (item.getVariant() != null && item.getVariant().getSize() != null) ? item.getVariant().getSize() : "";
        String color = (item.getVariant() != null && item.getVariant().getColor() != null) ? item.getVariant().getColor() : "";

        holder.tvName.setText(name + " (" + size + " " + color + ")");
        holder.tvQty.setText("Qty: " + item.getQuantity());

        // 🟢 PRO FIX: Item price calculation using BigDecimal
        BigDecimal price = BigDecimal.ZERO;
        if (item.getVariant() != null && item.getVariant().getSellingPrice() != null) {
            price = item.getVariant().getSellingPrice();
        }

        BigDecimal itemTotal = price.multiply(BigDecimal.valueOf(item.getQuantity()));

        holder.tvPrice.setText("₹" + itemTotal.setScale(2, RoundingMode.HALF_UP).toPlainString());
    }

    @Override
    public int getItemCount() {
        return cartItems.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvName, tvQty, tvPrice;
        ViewHolder(View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tv_checkout_item_name);
            tvQty = itemView.findViewById(R.id.tv_checkout_item_qty);
            tvPrice = itemView.findViewById(R.id.tv_checkout_item_price);
        }
    }
}