package com.retail.customershoppingapp.ui.cart;

import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.retail.customershoppingapp.R;
import com.retail.customershoppingapp.databinding.ItemCartItemBinding;
import com.retail.customershoppingapp.model.cart.CartItem;
import com.retail.customershoppingapp.model.product.VariantResponse;

import java.util.ArrayList;
import java.util.List;

public class CartAdapter extends RecyclerView.Adapter<CartAdapter.ViewHolder> {

    public interface OnCartItemActionListener {
        void onQuantityChanged(CartItem item, int newQuantity);
        void onItemRemoved(CartItem item);
        void onNoteEditRequested(CartItem item);
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

    public CartItem getItemAt(int position) {
        if (position >= 0 && position < cartItems.size()) {
            return cartItems.get(position);
        }
        return null;
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

            VariantResponse v = item.getVariant();
            int moq = v != null ? v.getMinimumOrderQuantity() : 1;
            int packSize = v != null ? v.getPackSize() : 1;
            int maxStock = v != null ? v.getStock() : 9999;

            StringBuilder variantInfo = new StringBuilder();
            if (v != null) {
                if (v.getSize() != null) {
                    variantInfo.append("Size: ").append(v.getSize()).append(" ");
                }
                if (v.getColor() != null) {
                    variantInfo.append("| ").append(v.getColor()).append(" ");
                }
                variantInfo.append("• MOQ: ").append(moq).append(" • Pack: ").append(packSize);
            } else {
                variantInfo.append("Standard Wholesale Lot");
            }

            if (item.getItemNote() != null && !item.getItemNote().trim().isEmpty()) {
                variantInfo.append("\n📝 Note: \"").append(item.getItemNote().trim()).append("\" (Tap to edit)");
            } else {
                variantInfo.append("\n📝 + Tap here to add a note for Seller");
            }

            binding.tvCartVariant.setText(variantInfo.toString().trim());
            binding.tvCartVariant.setOnClickListener(vView -> {
                if (listener != null) {
                    listener.onNoteEditRequested(item);
                }
            });

            binding.tvCartPrice.setText("₹" + item.getTotalPrice().stripTrailingZeros().toPlainString());
            binding.tvQuantity.setText(String.valueOf(item.getQuantity()));

            String imgUrl = item.getProduct() != null ? item.getProduct().getPrimaryImageUrl() : null;
            if (imgUrl != null && !imgUrl.isEmpty()) {
                Glide.with(binding.getRoot().getContext())
                        .load(imgUrl)
                        .placeholder(R.drawable.ic_product_placeholder)
                        .error(R.drawable.ic_product_placeholder)
                        .into(binding.ivCartProduct);
            } else {
                binding.ivCartProduct.setImageResource(R.drawable.ic_product_placeholder);
            }

            binding.btnPlus.setOnClickListener(vView -> {
                if (listener != null) {
                    int nextQty = item.getQuantity() + packSize;
                    if (nextQty > maxStock) {
                        Toast.makeText(binding.getRoot().getContext(),
                                "Only " + maxStock + " units available in stock",
                                Toast.LENGTH_SHORT).show();
                        return;
                    }
                    listener.onQuantityChanged(item, nextQty);
                }
            });

            binding.btnMinus.setOnClickListener(vView -> {
                if (listener != null) {
                    int prevQty = item.getQuantity() - packSize;
                    if (prevQty < moq) {
                        Toast.makeText(binding.getRoot().getContext(),
                                "Minimum Order Quantity is " + moq + " units (Swipe or tap trash icon to remove)",
                                Toast.LENGTH_SHORT).show();
                        return;
                    }
                    listener.onQuantityChanged(item, prevQty);
                }
            });

            binding.btnRemove.setOnClickListener(vView -> {
                if (listener != null) {
                    listener.onItemRemoved(item);
                }
            });
        }
    }
}