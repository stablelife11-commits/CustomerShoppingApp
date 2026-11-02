package com.retail.customershoppingapp.ui.order;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import com.retail.customershoppingapp.model.order.OrderRequest;
import com.retail.customershoppingapp.model.order.OrderResponse;
import com.retail.customershoppingapp.network.Resource;
import com.retail.customershoppingapp.repository.OrderRepository;

import java.util.List;

public class OrderViewModel extends AndroidViewModel {

    private final OrderRepository repository;

    public OrderViewModel(@NonNull Application application) {
        super(application);
        repository = new OrderRepository(application);
    }

    public LiveData<Resource<List<OrderResponse>>> checkoutOrders(OrderRequest request) {
        return repository.checkoutOrders(request);
    }

    public LiveData<Resource<List<OrderResponse>>> getOrders() {
        return repository.getOrders();
    }

    public LiveData<Resource<OrderResponse>> getOrderById(Long orderId) {
        return repository.getOrderById(orderId);
    }

    public LiveData<Resource<OrderResponse>> cancelOrder(Long orderId) {
        return repository.cancelOrder(orderId);
    }
}