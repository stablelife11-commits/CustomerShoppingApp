package com.retail.customershoppingapp.ui.home;

import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.retail.customershoppingapp.databinding.FragmentHomeBinding;
import com.retail.customershoppingapp.model.product.ProductResponse;
import com.retail.customershoppingapp.model.product.VariantResponse;
import com.retail.customershoppingapp.network.Resource;
import com.retail.customershoppingapp.session.SessionManager;
import com.retail.customershoppingapp.ui.cart.CartViewModel;
import com.retail.customershoppingapp.ui.product.ProductAdapter;
import com.retail.customershoppingapp.ui.product.ProductDetailActivity;
import com.retail.customershoppingapp.ui.product.ProductListActivity;
import com.retail.customershoppingapp.ui.product.ProductViewModel;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class HomeFragment extends Fragment implements ProductAdapter.OnProductClickListener, CategoryAdapter.OnCategoryClickListener {

    private FragmentHomeBinding binding;
    private ProductViewModel productViewModel;
    private CartViewModel cartViewModel;
    private ProductAdapter productAdapter;
    private ProductAdapter dealsAdapter;
    private CategoryAdapter categoryAdapter;
    private ObjectAnimator shimmerAnimator;

    private final Handler bannerHandler = new Handler(Looper.getMainLooper());
    private int currentBannerSlide = 0;
    private final String[] bannerTags = {"WHOLESALE MEGA MELA", "DIRECT MILL DISPATCH", "GST INVOICE READY"};
    private final String[] bannerTitles = {"Festive Garment Lots", "Bulk Innerwear Packs", "New Season Arrivals"};
    private final String[] bannerSubs = {
            "Up to 25% margin on bulk innerwear & apparel packs",
            "Order in set-wise packs directly from verified sellers",
            "Negotiate custom rates with sellers before confirmation"
    };

    private final Runnable bannerRunnable = new Runnable() {
        @Override
        public void run() {
            if (binding == null) return;
            currentBannerSlide = (currentBannerSlide + 1) % bannerTitles.length;
            binding.tvBannerTag.setText(bannerTags[currentBannerSlide]);
            binding.tvBannerTitle.setText(bannerTitles[currentBannerSlide]);
            binding.tvBannerSub.setText(bannerSubs[currentBannerSlide]);

            binding.dot1.setAlpha(currentBannerSlide == 0 ? 1.0f : 0.4f);
            binding.dot2.setAlpha(currentBannerSlide == 1 ? 1.0f : 0.4f);
            binding.dot3.setAlpha(currentBannerSlide == 2 ? 1.0f : 0.4f);

            bannerHandler.postDelayed(this, 4000);
        }
    };

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentHomeBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        productViewModel = new ViewModelProvider(this).get(ProductViewModel.class);
        cartViewModel = new ViewModelProvider(requireActivity()).get(CartViewModel.class);

        SessionManager sm = new SessionManager(requireContext());
        if (!sm.getUserName().isEmpty()) {
            binding.tvDeliveryLocation.setText("Deliver to: " + sm.getUserName() + " • Wholesale Account");
        }

        setupRecyclerViews();
        setupSearchAndActions();
        loadData();

        binding.swipeRefresh.setOnRefreshListener(this::loadData);
        binding.btnRetry.setOnClickListener(v -> loadData());
    }

    private void setupRecyclerViews() {
        productAdapter = new ProductAdapter(this);
        binding.rvProducts.setAdapter(productAdapter);

        dealsAdapter = new ProductAdapter(this);
        binding.rvDeals.setAdapter(dealsAdapter);

        categoryAdapter = new CategoryAdapter(this);
        binding.rvCategories.setAdapter(categoryAdapter);
    }

    private void setupSearchAndActions() {
        binding.etSearch.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                String query = binding.etSearch.getText() != null ? binding.etSearch.getText().toString().trim() : "";
                openProductListWithSearch(query);
                return true;
            }
            return false;
        });

        View.OnClickListener openAllProducts = v -> {
            Intent intent = new Intent(requireContext(), ProductListActivity.class);
            startActivity(intent);
        };

        binding.btnBannerExplore.setOnClickListener(openAllProducts);
        binding.tvViewAllDeals.setOnClickListener(openAllProducts);
        binding.tvViewAllProducts.setOnClickListener(openAllProducts);
        binding.tvViewAllCategories.setOnClickListener(openAllProducts);

        binding.ivSearchMic.setOnClickListener(v -> openProductListWithSearch(""));
        binding.ivSearchCamera.setOnClickListener(v -> openProductListWithSearch(""));
        binding.btnHeaderNotifications.setOnClickListener(v ->
                com.retail.customershoppingapp.utils.NotificationHelper.checkAndShowNotifications(requireContext(), true)
        );
    }

    private void startShimmer() {
        if (binding == null) return;
        binding.layoutShimmer.setVisibility(View.VISIBLE);
        if (shimmerAnimator == null) {
            shimmerAnimator = ObjectAnimator.ofFloat(binding.layoutShimmer, "alpha", 1.0f, 0.45f);
            shimmerAnimator.setDuration(750);
            shimmerAnimator.setRepeatMode(ValueAnimator.REVERSE);
            shimmerAnimator.setRepeatCount(ValueAnimator.INFINITE);
        }
        if (!shimmerAnimator.isRunning()) {
            shimmerAnimator.start();
        }
    }

    private void stopShimmer() {
        if (shimmerAnimator != null && shimmerAnimator.isRunning()) {
            shimmerAnimator.cancel();
        }
        if (binding != null) {
            binding.layoutShimmer.setAlpha(1.0f);
            binding.layoutShimmer.setVisibility(View.GONE);
        }
    }

    private void loadData() {
        productViewModel.fetchProducts(0, 20).observe(getViewLifecycleOwner(), resource -> {
            if (resource == null || binding == null) return;

            binding.swipeRefresh.setRefreshing(false);

            if (resource.status == Resource.Status.LOADING) {
                startShimmer();
                binding.layoutError.setVisibility(View.GONE);
            } else if (resource.status == Resource.Status.SUCCESS) {
                stopShimmer();
                binding.layoutError.setVisibility(View.GONE);

                List<ProductResponse> products = resource.data != null ? resource.data : new ArrayList<>();
                productAdapter.setProducts(products);

                // Populate Deals of the Day rail with top 6 items
                List<ProductResponse> dealsList = products.size() > 6 ? products.subList(0, 6) : products;
                if (dealsList.isEmpty()) {
                    binding.layoutDealsSection.setVisibility(View.GONE);
                } else {
                    binding.layoutDealsSection.setVisibility(View.VISIBLE);
                    dealsAdapter.setProducts(dealsList);
                }

                Set<String> categorySet = new HashSet<>();
                for (ProductResponse p : products) {
                    if (p.getCategory() != null && !p.getCategory().isEmpty()) {
                        categorySet.add(p.getCategory());
                    }
                }
                if (categorySet.isEmpty()) {
                    categorySet.add("Undergarments");
                    categorySet.add("Apparel");
                    categorySet.add("Menswear");
                    categorySet.add("Kids Wear");
                    categorySet.add("Winter Wear");
                }
                categoryAdapter.setCategories(new ArrayList<>(categorySet));

            } else if (resource.status == Resource.Status.ERROR) {
                stopShimmer();
                binding.layoutError.setVisibility(View.VISIBLE);
                binding.tvErrorMsg.setText(resource.message != null ? resource.message : "Failed to load products");
            }
        });
        com.retail.customershoppingapp.utils.NotificationHelper.checkAndShowNotifications(requireContext(), false);
    }

    private void openProductListWithSearch(String query) {
        Intent intent = new Intent(requireContext(), ProductListActivity.class);
        intent.putExtra("search_query", query);
        startActivity(intent);
    }

    @Override
    public void onProductClick(ProductResponse product) {
        Intent intent = new Intent(requireContext(), ProductDetailActivity.class);
        intent.putExtra("product_data", product);
        startActivity(intent);
    }

    @Override
    public void onAddToCartClick(ProductResponse product) {
        if (product.getVariants() != null && !product.getVariants().isEmpty()) {
            VariantResponse firstVar = product.getVariants().get(0);
            int moq = firstVar.getMinimumOrderQuantity();
            if (firstVar.getStock() < moq) {
                Toast.makeText(requireContext(), "Out of stock for minimum wholesale quantity (" + moq + ")", Toast.LENGTH_SHORT).show();
                return;
            }
            cartViewModel.addToCart(product, firstVar, moq);
            Toast.makeText(requireContext(), "Added " + moq + " unit(s) (MOQ) to Cart", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(requireContext(), "No variants available", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onCategoryClick(String categoryName) {
        Intent intent = new Intent(requireContext(), ProductListActivity.class);
        intent.putExtra("category_filter", categoryName);
        startActivity(intent);
    }

    @Override
    public void onResume() {
        super.onResume();
        bannerHandler.postDelayed(bannerRunnable, 4000);
    }

    @Override
    public void onPause() {
        super.onPause();
        bannerHandler.removeCallbacks(bannerRunnable);
    }

    @Override
    public void onDestroyView() {
        bannerHandler.removeCallbacks(bannerRunnable);
        stopShimmer();
        super.onDestroyView();
        binding = null;
    }
}