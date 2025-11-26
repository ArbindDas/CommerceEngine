package com.JSR.order_service.dto;

import com.JSR.order_service.entites.OrderLineItems;

import java.util.List;

public record OrderResponse(
        Long id,
        String orderName,
        List<OrderLineItemResponse> orderLineItems
) {
}
