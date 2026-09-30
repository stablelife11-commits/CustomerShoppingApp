package com.retail.customershoppingapp.ui.category;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.retail.customershoppingapp.databinding.FragmentCategoriesBinding;
import com.retail.customershoppingapp.model.product.ProductResponse;
import com.retail.customershoppingapp.network.Resource;
import com.retail.customershoppingapp.ui.home.CategoryAdapter;
import com.retail.customershoppingapp.ui.product.ProductListActivity;
import com.retail.customershoppingapp.ui.product.ProductViewModel;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class CategoriesFragment extends Fragment implements CategoryAdapter.OnCategoryClickListener {

    private FragmentCategoriesBinding binding;
    private ProductViewModel productViewModel;
    private CategoryAdapter categoryAdapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentCategoriesBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        productViewModel = new ViewModelProvider(this).get(ProductViewModel.class);
        categoryAdapter = new CategoryAdapter(this);
        binding.rvCategoriesGrid.setAdapter(categoryAdapter);

        productViewModel.fetchProducts().observe(getViewLifecycleOwner(), resource -> {
            if (resource != null && resource.status == Resource.Status.SUCCESS) {
                List<ProductResponse> products = resource.data != null ? resource.data : new ArrayList<>();
                Set<String> categorySet = new HashSet<>();
                for (ProductResponse p : products) {
                    if (p.getCategory() != null && !p.getCategory().isEmpty()) {
                        categorySet.add(p.getCategory());
                    }
                }
                if (categorySet.isEmpty()) {
                    categorySet.add("Apparel");
                    categorySet.add("Footwear");
                    categorySet.add("Electronics");
                    categorySet.add("Accessories");
                    categorySet.add("Home & Living");
                }
                categoryAdapter.setCategories(new ArrayList<>(categorySet));
            }
        });
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
