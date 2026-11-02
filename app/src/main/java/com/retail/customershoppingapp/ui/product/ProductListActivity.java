package com.retail.customershoppingapp.ui.product;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.retail.customershoppingapp.R;
import com.retail.customershoppingapp.databinding.ActivityProductListBinding;
import com.retail.customershoppingapp.model.product.ProductResponse;
import com.retail.customershoppingapp.model.product.VariantResponse;
import com.retail.customershoppingapp.network.Resource;
import com.retail.customershoppingapp.ui.cart.CartViewModel;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class ProductListActivity extends AppCompatActivity implements ProductAdapter.OnProductClickListener {

    private ActivityProductListBinding binding;
    private ProductViewModel productViewModel;
    private CartViewModel cartViewModel;
    private ProductAdapter productAdapter;
    private GridLayoutManager gridLayoutManager;
    private final List<ProductResponse> rawProducts = new ArrayList<>();

    private int currentPage = 0;
    private boolean isLoading = false;
    private boolean isLastPage = false;
    private final int PAGE_SIZE = 20;
    private boolean isTwoColumnGrid = true;

    private final Handler searchHandler = new Handler(Looper.getMainLooper());
    private Runnable searchRunnable;

    private int sortIndex = 0;
    private final String[] sortOptions = {"Recommended", "Price: Low to High", "Price: High to Low", "Name (A–Z)"};
    private final String[] sortKeys = {"RECOMMENDED", "PRICE_LOW_HIGH", "PRICE_HIGH_LOW", "NAME"};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityProductListBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        productViewModel = new ViewModelProvider(this).get(ProductViewModel.class);
        cartViewModel = new ViewModelProvider(this).get(CartViewModel.class);

        gridLayoutManager = new GridLayoutManager(this, 2);
        binding.rvProductList.setLayoutManager(gridLayoutManager);
        productAdapter = new ProductAdapter(this);
        binding.rvProductList.setAdapter(productAdapter);

        binding.btnBack.setOnClickListener(v -> finish());

        binding.btnToggleLayout.setOnClickListener(v -> {
            isTwoColumnGrid = !isTwoColumnGrid;
            gridLayoutManager.setSpanCount(isTwoColumnGrid ? 2 : 1);
            binding.btnToggleLayout.setImageResource(isTwoColumnGrid ? R.drawable.ic_list_view : R.drawable.ic_grid_view);
            productAdapter.notifyItemRangeChanged(0, productAdapter.getItemCount());
        });

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
                if (searchRunnable != null) {
                    searchHandler.removeCallbacks(searchRunnable);
                }
                final String query = s != null ? s.toString() : "";
                searchRunnable = () -> {
                    productViewModel.setSearchQuery(query);
                    resetAndReloadFromServer();
                };
                searchHandler.postDelayed(searchRunnable, 350);
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        binding.chipSort.setOnClickListener(v -> {
            new AlertDialog.Builder(this)
                    .setTitle("Sort Wholesale Catalog By")
                    .setSingleChoiceItems(sortOptions, sortIndex, (dialog, which) -> {
                        sortIndex = which;
                        productViewModel.setSortOrder(sortKeys[sortIndex]);
                        binding.chipSort.setText("Sort: " + sortOptions[sortIndex]);
                        applyFilters();
                        dialog.dismiss();
                    })
                    .show();
        });

        binding.chipCategory.setOnClickListener(v -> {
            productViewModel.setSelectedCategory("ALL");
            binding.chipCategory.setText("Category: All");
            resetAndReloadFromServer();
        });

        binding.chipInStock.setOnCheckedChangeListener((buttonView, isChecked) -> applyFilters());

        binding.btnResetFilters.setOnClickListener(v -> {
            if (searchRunnable != null) {
                searchHandler.removeCallbacks(searchRunnable);
            }
            binding.etSearchList.setText("");
            productViewModel.setSearchQuery("");
            productViewModel.setSelectedCategory("ALL");
            productViewModel.setSortOrder("RECOMMENDED");
            sortIndex = 0;
            binding.chipSort.setText("Sort: Recommended");
            binding.chipCategory.setText("Category: All");
            binding.chipInStock.setChecked(false);
            resetAndReloadFromServer();
        });

        binding.rvProductList.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                if (dy > 0 && gridLayoutManager != null) {
                    int visibleItemCount = gridLayoutManager.getChildCount();
                    int totalItemCount = gridLayoutManager.getItemCount();
                    int pastVisibleItems = gridLayoutManager.findFirstVisibleItemPosition();

                    if (!isLoading && !isLastPage) {
                        if ((visibleItemCount + pastVisibleItems) >= totalItemCount) {
                            currentPage++;
                            loadProducts(currentPage);
                        }
                    }
                }
            }
        });

        loadProducts(currentPage);
    }

    private void resetAndReloadFromServer() {
        currentPage = 0;
        isLastPage = false;
        loadProducts(0);
    }

    private void loadProducts(int page) {
        isLoading = true;
        productViewModel.fetchProducts(page, PAGE_SIZE).observe(this, resource -> {
            if (resource == null) return;
            if (resource.status == Resource.Status.LOADING) {
                if (page == 0) binding.progressBar.setVisibility(View.VISIBLE);
            } else if (resource.status == Resource.Status.SUCCESS) {
                binding.progressBar.setVisibility(View.GONE);
                isLoading = false;

                List<ProductResponse> newProducts = resource.data != null ? resource.data : new ArrayList<>();
                if (newProducts.size() < PAGE_SIZE) {
                    isLastPage = true;
                }

                if (page == 0) {
                    rawProducts.clear();
                }
                rawProducts.addAll(newProducts);
                applyFilters();

            } else if (resource.status == Resource.Status.ERROR) {
                binding.progressBar.setVisibility(View.GONE);
                isLoading = false;
                Toast.makeText(ProductListActivity.this, resource.message, Toast.LENGTH_SHORT).show();
                applyFilters();
            }
        });
    }

    private void applyFilters() {
        List<ProductResponse> filtered = productViewModel.filterAndSort(rawProducts);

        if (binding.chipInStock.isChecked()) {
            List<ProductResponse> inStockOnly = new ArrayList<>();
            for (ProductResponse p : filtered) {
                if (Boolean.TRUE.equals(p.getActive()) && p.getVariants() != null) {
                    int stock = 0;
                    int moq = p.getVariants().isEmpty() ? 1 : p.getVariants().get(0).getMinimumOrderQuantity();
                    for (VariantResponse v : p.getVariants()) {
                        stock += v.getStock();
                    }
                    if (stock >= moq) {
                        inStockOnly.add(p);
                    }
                }
            }
            filtered = inStockOnly;
        }

        productAdapter.setProducts(filtered);
        binding.tvResultCount.setText(String.format(Locale.getDefault(),
                "Showing %d wholesale product(s)", filtered.size()));

        if (filtered.isEmpty() && !isLoading) {
            binding.layoutEmptySearch.setVisibility(View.VISIBLE);
            binding.rvProductList.setVisibility(View.GONE);
        } else {
            binding.layoutEmptySearch.setVisibility(View.GONE);
            binding.rvProductList.setVisibility(View.VISIBLE);
        }
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
            VariantResponse firstVar = product.getVariants().get(0);
            int moq = firstVar.getMinimumOrderQuantity();
            if (firstVar.getStock() < moq) {
                Toast.makeText(this, "Out of stock for minimum wholesale quantity (" + moq + ")", Toast.LENGTH_SHORT).show();
                return;
            }
            cartViewModel.addToCart(product, firstVar, moq);
            Toast.makeText(this, "Added " + moq + " unit(s) (MOQ) to Cart", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "No variants available", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onDestroy() {
        if (searchRunnable != null) {
            searchHandler.removeCallbacks(searchRunnable);
        }
        super.onDestroy();
    }
}