package com.retail.customershoppingapp.ui.product;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.retail.customershoppingapp.model.product.ProductResponse;
import com.retail.customershoppingapp.network.Resource;
import com.retail.customershoppingapp.repository.ProductRepository;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ProductViewModel extends AndroidViewModel {
    private final ProductRepository repository;
    private final MutableLiveData<String> searchQuery = new MutableLiveData<>("");
    private final MutableLiveData<String> selectedCategory = new MutableLiveData<>("ALL");
    private final MutableLiveData<String> sortOrder = new MutableLiveData<>("RECOMMENDED");

    public ProductViewModel(@NonNull Application application) {
        super(application);
        repository = new ProductRepository(application);
    }

    // 🟢 FIX: Request exact page and size from Repository
    public LiveData<Resource<List<ProductResponse>>> fetchProducts(int page, int size) {
        return repository.getProducts(page, size);
    }

    public LiveData<Resource<ProductResponse>> getProductById(Long productId) {
        return repository.getProductById(productId);
    }

    public void setSearchQuery(String query) {
        searchQuery.setValue(query != null ? query : "");
    }

    public void setSelectedCategory(String category) {
        selectedCategory.setValue(category != null ? category : "ALL");
    }

    public void setSortOrder(String sort) {
        sortOrder.setValue(sort != null ? sort : "RECOMMENDED");
    }

    public List<ProductResponse> filterAndSort(List<ProductResponse> originalList) {
        if (originalList == null) return new ArrayList<>();

        String query = searchQuery.getValue() != null ? searchQuery.getValue().trim().toLowerCase() : "";
        String category = selectedCategory.getValue() != null ? selectedCategory.getValue() : "ALL";
        String sort = sortOrder.getValue() != null ? sortOrder.getValue() : "RECOMMENDED";

        List<ProductResponse> filtered = new ArrayList<>();
        for (ProductResponse product : originalList) {
            boolean matchesSearch = query.isEmpty() ||
                    (product.getName() != null && product.getName().toLowerCase().contains(query)) ||
                    (product.getBrand() != null && product.getBrand().toLowerCase().contains(query)) ||
                    (product.getCategory() != null && product.getCategory().toLowerCase().contains(query));

            boolean matchesCategory = "ALL".equalsIgnoreCase(category) ||
                    (product.getCategory() != null && product.getCategory().equalsIgnoreCase(category));

            if (matchesSearch && matchesCategory) {
                filtered.add(product);
            }
        }

        if ("PRICE_LOW_HIGH".equals(sort)) {
            Collections.sort(filtered, (p1, p2) -> p1.getMinPrice().compareTo(p2.getMinPrice()));
        } else if ("PRICE_HIGH_LOW".equals(sort)) {
            Collections.sort(filtered, (p1, p2) -> p2.getMinPrice().compareTo(p1.getMinPrice()));
        } else if ("NAME".equals(sort)) {
            Collections.sort(filtered, (p1, p2) -> {
                String n1 = p1.getName() != null ? p1.getName() : "";
                String n2 = p2.getName() != null ? p2.getName() : "";
                return n1.compareToIgnoreCase(n2);
            });
        }

        return filtered;
    }
}