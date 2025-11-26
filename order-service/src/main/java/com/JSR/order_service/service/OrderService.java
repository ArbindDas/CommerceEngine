package com.JSR.order_service.service;


import com.JSR.order_service.dto.OrderRequest;
import com.JSR.order_service.dto.OrderResponse;

public interface OrderService {



    OrderResponse placeOrder(OrderRequest request);

}
