package com.retail.customershoppingapp.ui.cart;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.retail.customershoppingapp.R;
import com.retail.customershoppingapp.databinding.ItemCartItemBinding;
import com.retail.customershoppingapp.model.cart.CartItem;

import java.util.ArrayList;
import java.util.List;

public class CartAdapter extends RecyclerView.Adapter<CartAdapter.ViewHolder> {

    public interface OnCartItemActionListener {
        void onQuantityChanged(CartItem item, int newQuantity);
        void onItemRemoved(CartItem item);
    }

    private List<CartItem> cartItems = new ArrayList<>();
    private final OnCartItemActionListener listener;

    public CartAdapter(OnCartItemActionListener listener) {
        this.listener = listener;
    }

    public void setCartItems(List<CartItem> cartItems) {
        this.cartItems = cartItems != null ? cartItems : new ArrayList<>();
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemCartItemBinding binding = ItemCartItemBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false
        );
        return new ViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.bind(cartItems.get(position));
    }

    @Override
    public int getItemCount() {
        return cartItems.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        private final ItemCartItemBinding binding;

        ViewHolder(@NonNull ItemCartItemBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        void bind(CartItem item) {
            binding.tvCartName.setText(item.getProduct() != null && item.getProduct().getName() != null ?
                    item.getProduct().getName() : "Item");

            StringBuilder variantInfo = new StringBuilder("Variant: ");
            if (item.getVariant() != null) {
                if (item.getVariant().getSize() != null) {
                    variantInfo.append("Size: ").append(item.getVariant().getSize()).append(" ");
                }
                if (item.getVariant().getColor() != null) {
                    variantInfo.append("Color: ").append(item.getVariant().getColor());
                }
            } else {
                variantInfo.append("Standard");
            }
            binding.tvCartVariant.setText(variantInfo.toString());

            binding.tvCartPrice.setText("₹" + item.getTotalPrice());
            binding.tvQuantity.setText(String.valueOf(item.getQuantity()));

            String imgUrl = item.getProduct() != null ? item.getProduct().getPrimaryImageUrl() : null;
            if (imgUrl != null && !imgUrl.isEmpty()) {
                Glide.with(binding.getRoot().getContext())
                        .load(imgUrl)
                        .placeholder(R.drawable.ic_launcher_background)
                        .error(R.drawable.ic_launcher_background)
                        .into(binding.ivCartProduct);
            } else {
                binding.ivCartProduct.setImageResource(R.drawable.ic_launcher_background);
            }

            binding.btnPlus.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onQuantityChanged(item, item.getQuantity() + 1);
                }
            });

            binding.btnMinus.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onQuantityChanged(item, item.getQuantity() - 1);
                }
            });

            binding.btnRemove.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onItemRemoved(item);
                }
            });
        }
    }
}
