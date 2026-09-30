package com.retail.customershoppingapp.ui.order;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import com.retail.customershoppingapp.model.order.OrderItemRequest;
import com.retail.customershoppingapp.model.order.OrderRequest;
import com.retail.customershoppingapp.model.order.OrderResponse;
import com.retail.customershoppingapp.network.Resource;
import com.retail.customershoppingapp.repository.OrderRepository;

import java.util.List;

public class OrderViewModel extends AndroidViewModel {
    private final OrderRepository orderRepository;

    public OrderViewModel(@NonNull Application application) {
        super(application);
        orderRepository = new OrderRepository(application);
    }

    public LiveData<Resource<OrderResponse>> createOrder(Long customerId, List<OrderItemRequest> items) {
        OrderRequest request = new OrderRequest(customerId, items);
        return orderRepository.createOrder(request);
    }

    public LiveData<Resource<List<OrderResponse>>> getOrders() {
        return orderRepository.getOrders();
    }
}
