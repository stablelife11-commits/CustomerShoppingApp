package com.retail.customershoppingapp.ui.product;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.retail.customershoppingapp.databinding.ActivityProductListBinding;
import com.retail.customershoppingapp.model.product.ProductResponse;
import com.retail.customershoppingapp.network.Resource;
import com.retail.customershoppingapp.ui.cart.CartViewModel;

import java.util.ArrayList;
import java.util.List;

public class ProductListActivity extends AppCompatActivity implements ProductAdapter.OnProductClickListener {

    private ActivityProductListBinding binding;
    private ProductViewModel productViewModel;
    private CartViewModel cartViewModel;
    private ProductAdapter productAdapter;
    private List<ProductResponse> rawProducts = new ArrayList<>();

    // 🟢 PAGINATION STATE
    private int currentPage = 0;
    private boolean isLoading = false;
    private boolean isLastPage = false;
    private final int PAGE_SIZE = 20;

    private int sortIndex = 0;
    private final String[] sortOptions = {"Recommended", "Price: Low to High", "Price: High to Low", "Name"};
    private final String[] sortKeys = {"RECOMMENDED", "PRICE_LOW_HIGH", "PRICE_HIGH_LOW", "NAME"};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityProductListBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        productViewModel = new ViewModelProvider(this).get(ProductViewModel.class);
        cartViewModel = new ViewModelProvider(this).get(CartViewModel.class);

        productAdapter = new ProductAdapter(this);
        binding.rvProductList.setAdapter(productAdapter);

        binding.btnBack.setOnClickListener(v -> finish());

        Intent intent = getIntent();
        if (intent.hasExtra("search_query")) {
            String q = intent.getStringExtra("search_query");
            binding.etSearchList.setText(q);
            productViewModel.setSearchQuery(q);
        }
        if (intent.hasExtra("category_filter")) {
            String cat = intent.getStringExtra("category_filter");
            productViewModel.setSelectedCategory(cat);
            binding.chipCategory.setText("Category: " + cat);
        }

        binding.etSearchList.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                productViewModel.setSearchQuery(s.toString());
                applyFilters();
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        binding.chipSort.setOnClickListener(v -> {
            sortIndex = (sortIndex + 1) % sortOptions.length;
            productViewModel.setSortOrder(sortKeys[sortIndex]);
            binding.chipSort.setText("Sort: " + sortOptions[sortIndex]);
            applyFilters();
        });

        binding.chipCategory.setOnClickListener(v -> {
            productViewModel.setSelectedCategory("ALL");
            binding.chipCategory.setText("Category: All");
            applyFilters();
        });

        // 🟢 FIX: Infinite Scroll Listener
        binding.rvProductList.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                if (dy > 0) { // If scrolling down
                    GridLayoutManager layoutManager = (GridLayoutManager) recyclerView.getLayoutManager();
                    if (layoutManager != null) {
                        int visibleItemCount = layoutManager.getChildCount();
                        int totalItemCount = layoutManager.getItemCount();
                        int pastVisibleItems = layoutManager.findFirstVisibleItemPosition();

                        if (!isLoading && !isLastPage) {
                            if ((visibleItemCount + pastVisibleItems) >= totalItemCount) {
                                currentPage++;
                                loadProducts(currentPage);
                            }
                        }
                    }
                }
            }
        });

        loadProducts(currentPage);
    }

    private void loadProducts(int page) {
        isLoading = true;
        // 🟢 Fetch specific page
        productViewModel.fetchProducts(page, PAGE_SIZE).observe(this, resource -> {
            if (resource == null) return;
            if (resource.status == Resource.Status.LOADING) {
                if (page == 0) binding.progressBar.setVisibility(View.VISIBLE);
            } else if (resource.status == Resource.Status.SUCCESS) {
                binding.progressBar.setVisibility(View.GONE);
                isLoading = false;

                List<ProductResponse> newProducts = resource.data != null ? resource.data : new ArrayList<>();

                // Agar 20 se kam products aaye hain, iska matlab list khatam ho gayi hai
                if (newProducts.size() < PAGE_SIZE) {
                    isLastPage = true;
                }

                if (page == 0) {
                    rawProducts.clear(); // Naya search ya first load
                }
                rawProducts.addAll(newProducts); // Puraane products ke aage naye products jodna
                applyFilters();

            } else if (resource.status == Resource.Status.ERROR) {
                binding.progressBar.setVisibility(View.GONE);
                isLoading = false;
                Toast.makeText(ProductListActivity.this, resource.message, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void applyFilters() {
        List<ProductResponse> filtered = productViewModel.filterAndSort(rawProducts);
        productAdapter.setProducts(filtered);
    }

    @Override
    public void onProductClick(ProductResponse product) {
        Intent intent = new Intent(ProductListActivity.this, ProductDetailActivity.class);
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
        Toast.makeText(this, "Added to Cart", Toast.LENGTH_SHORT).show();
    }
}