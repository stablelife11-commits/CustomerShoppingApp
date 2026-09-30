package com.retail.customershoppingapp.ui.home;

import android.content.Intent;
import android.os.Bundle;
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
import com.retail.customershoppingapp.network.Resource;
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
    private CategoryAdapter categoryAdapter;

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

        setupRecyclerViews();
        setupSearchAndActions();
        loadData();

        binding.swipeRefresh.setOnRefreshListener(this::loadData);
        binding.btnRetry.setOnClickListener(v -> loadData());
    }

    private void setupRecyclerViews() {
        productAdapter = new ProductAdapter(this);
        binding.rvProducts.setAdapter(productAdapter);

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

        binding.btnBannerExplore.setOnClickListener(v -> {
            Intent intent = new Intent(requireContext(), ProductListActivity.class);
            startActivity(intent);
        });
    }

    private void loadData() {
        productViewModel.fetchProducts().observe(getViewLifecycleOwner(), resource -> {
            if (resource == null) return;

            binding.swipeRefresh.setRefreshing(false);

            if (resource.status == Resource.Status.LOADING) {
                binding.progressBar.setVisibility(View.VISIBLE);
                binding.layoutError.setVisibility(View.GONE);
            } else if (resource.status == Resource.Status.SUCCESS) {
                binding.progressBar.setVisibility(View.GONE);
                binding.layoutError.setVisibility(View.GONE);

                List<ProductResponse> products = resource.data != null ? resource.data : new ArrayList<>();
                productAdapter.setProducts(products);

                // Extract unique categories from product list
                Set<String> categorySet = new HashSet<>();
                for (ProductResponse p : products) {
                    if (p.getCategory() != null && !p.getCategory().isEmpty()) {
                        categorySet.add(p.getCategory());
                    }
                }
                if (categorySet.isEmpty()) {
                    categorySet.add("Apparel");
                    categorySet.add("Electronics");
                    categorySet.add("Footwear");
                }
                categoryAdapter.setCategories(new ArrayList<>(categorySet));

            } else if (resource.status == Resource.Status.ERROR) {
                binding.progressBar.setVisibility(View.GONE);
                binding.layoutError.setVisibility(View.VISIBLE);
                binding.tvErrorMsg.setText(resource.message != null ? resource.message : "Failed to load products");
            }
        });
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
            cartViewModel.addToCart(product, product.getVariants().get(0), 1);
        } else {
            cartViewModel.addToCart(product, null, 1);
        }
        Toast.makeText(requireContext(), "Added to Cart", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onCategoryClick(String categoryName) {
        Intent intent = new Intent(requireContext(), ProductListActivity.class);
        intent.putExtra("category_filter", categoryName);
        startActivity(intent);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
