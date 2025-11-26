package com.JSR.order_service.dto;

import com.JSR.order_service.entites.OrderLineItems;

import java.util.List;

public record OrderRequest(
        String orderName,
        List<OrderLIneItemRequest> orderLineItems
) {
}
