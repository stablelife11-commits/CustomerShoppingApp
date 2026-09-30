package com.retail.customershoppingapp.ui.product;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.retail.customershoppingapp.R;
import com.retail.customershoppingapp.databinding.ItemImageSliderBinding;

import java.util.ArrayList;
import java.util.List;

public class ImageCarouselAdapter extends RecyclerView.Adapter<ImageCarouselAdapter.ViewHolder> {

    private List<String> imageUrls = new ArrayList<>();

    public void setImageUrls(List<String> imageUrls) {
        this.imageUrls = imageUrls != null ? imageUrls : new ArrayList<>();
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemImageSliderBinding binding = ItemImageSliderBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false
        );
        return new ViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.bind(imageUrls.get(position));
    }

    @Override
    public int getItemCount() {
        return imageUrls.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        private final ItemImageSliderBinding binding;

        ViewHolder(@NonNull ItemImageSliderBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        void bind(String url) {
            if (url != null && !url.isEmpty()) {
                Glide.with(binding.getRoot().getContext())
                        .load(url)
                        .placeholder(R.drawable.ic_launcher_background)
                        .error(R.drawable.ic_launcher_background)
                        .into(binding.ivSliderImage);
            } else {
                binding.ivSliderImage.setImageResource(R.drawable.ic_launcher_background);
            }
        }
    }
}
