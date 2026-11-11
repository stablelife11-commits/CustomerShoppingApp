package com.retail.customershoppingapp;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.os.Bundle;
import android.view.Menu;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.badge.BadgeDrawable;
import com.retail.customershoppingapp.databinding.ActivityMainBinding;
import com.retail.customershoppingapp.session.SessionManager;
import com.retail.customershoppingapp.ui.auth.LoginActivity;
import com.retail.customershoppingapp.ui.cart.CartFragment;
import com.retail.customershoppingapp.ui.cart.CartViewModel;
import com.retail.customershoppingapp.ui.category.CategoriesFragment;
import com.retail.customershoppingapp.ui.home.HomeFragment;
import com.retail.customershoppingapp.ui.order.OrderHistoryFragment;
import com.retail.customershoppingapp.ui.profile.ProfileFragment;
import com.retail.customershoppingapp.utils.UpdateHelper;

public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;
    private SessionManager sessionManager;
    private CartViewModel cartViewModel;

    private Fragment homeFragment;
    private Fragment categoriesFragment;
    private Fragment cartFragment;
    private Fragment ordersFragment;
    private Fragment profileFragment;

    private FragmentManager fm;
    private Fragment activeFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        sessionManager = new SessionManager(this);
        if (!sessionManager.isLoggedIn()) {
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return;
        }

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        cartViewModel = new ViewModelProvider(this).get(CartViewModel.class);

        setupBottomNavIconsAndColors();
        setupFragments(savedInstanceState);
        setupBottomNavigation();
        observeCartBadge();
        if ("orders".equals(getIntent().getStringExtra("open_tab"))) {
            binding.bottomNavigation.setSelectedItemId(R.id.nav_orders);
        }

        // Check for OTA app updates on startup
        UpdateHelper.checkForUpdate(this, false);
    }

    private void setupBottomNavIconsAndColors() {
        Menu menu = binding.bottomNavigation.getMenu();
        if (menu.findItem(R.id.nav_home) != null) {
            menu.findItem(R.id.nav_home).setIcon(R.drawable.ic_nav_home);
        }
        if (menu.findItem(R.id.nav_categories) != null) {
            menu.findItem(R.id.nav_categories).setIcon(R.drawable.ic_nav_categories);
        }
        if (menu.findItem(R.id.nav_cart) != null) {
            menu.findItem(R.id.nav_cart).setIcon(R.drawable.ic_nav_cart);
        }
        if (menu.findItem(R.id.nav_orders) != null) {
            menu.findItem(R.id.nav_orders).setIcon(R.drawable.ic_nav_orders);
        }
        if (menu.findItem(R.id.nav_account) != null) {
            menu.findItem(R.id.nav_account).setIcon(R.drawable.ic_nav_account);
        }

        int[][] states = new int[][]{
                new int[]{android.R.attr.state_checked},
                new int[]{-android.R.attr.state_checked}
        };
        int[] colors = new int[]{
                ContextCompat.getColor(this, R.color.primary),
                ContextCompat.getColor(this, R.color.text_secondary)
        };
        ColorStateList navColorStateList = new ColorStateList(states, colors);
        binding.bottomNavigation.setItemIconTintList(navColorStateList);
        binding.bottomNavigation.setItemTextColor(navColorStateList);
        // BottomNavigationView को दोबारा डबल पैडिंग लगाने से रोकें
        androidx.core.view.ViewCompat.setOnApplyWindowInsetsListener(binding.bottomNavigation, (v, insets) -> insets);
    }

    private void setupFragments(Bundle savedInstanceState) {
        fm = getSupportFragmentManager();
        int containerId = binding.fragmentContainer.getId();
        if (savedInstanceState == null) {
            homeFragment = new HomeFragment();
            categoriesFragment = new CategoriesFragment();
            cartFragment = new CartFragment();
            ordersFragment = new OrderHistoryFragment();
            profileFragment = new ProfileFragment();

            fm.beginTransaction()
                    .add(containerId, profileFragment, "5").hide(profileFragment)
                    .add(containerId, ordersFragment, "4").hide(ordersFragment)
                    .add(containerId, cartFragment, "3").hide(cartFragment)
                    .add(containerId, categoriesFragment, "2").hide(categoriesFragment)
                    .add(containerId, homeFragment, "1")
                    .commit();
            activeFragment = homeFragment;
            binding.bottomNavigation.setSelectedItemId(R.id.nav_home);
        } else {
            homeFragment = fm.findFragmentByTag("1");
            categoriesFragment = fm.findFragmentByTag("2");
            cartFragment = fm.findFragmentByTag("3");
            ordersFragment = fm.findFragmentByTag("4");
            profileFragment = fm.findFragmentByTag("5");

            int selectedId = binding.bottomNavigation.getSelectedItemId();
            if (selectedId == R.id.nav_categories) activeFragment = categoriesFragment;
            else if (selectedId == R.id.nav_cart) activeFragment = cartFragment;
            else if (selectedId == R.id.nav_orders) activeFragment = ordersFragment;
            else if (selectedId == R.id.nav_account) activeFragment = profileFragment;
            else activeFragment = homeFragment;
        }
    }

    private void setupBottomNavigation() {
        binding.bottomNavigation.setOnItemSelectedListener(item -> {
            Fragment target = homeFragment;
            int itemId = item.getItemId();

            if (itemId == R.id.nav_home) {
                target = homeFragment;
            } else if (itemId == R.id.nav_categories) {
                target = categoriesFragment;
            } else if (itemId == R.id.nav_cart) {
                target = cartFragment;
            } else if (itemId == R.id.nav_orders) {
                target = ordersFragment;
            } else if (itemId == R.id.nav_account) {
                target = profileFragment;
            }

            if (target != activeFragment) {
                fm.beginTransaction().hide(activeFragment).show(target).commit();
                activeFragment = target;
            }
            return true;
        });
    }

    private void observeCartBadge() {
        cartViewModel.getCartItems().observe(this, items -> {
            int count = cartViewModel.getItemCount();
            BadgeDrawable badge = binding.bottomNavigation.getOrCreateBadge(R.id.nav_cart);
            if (count > 0) {
                badge.setVisible(true);
                badge.setNumber(count);
            } else {
                badge.setVisible(false);
            }
        });
    }
}