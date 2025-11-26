package com.JSR.order_service.dto;

import java.math.BigDecimal;

public record OrderLineItemResponse(

        Long id,
        String skuCode,
        BigDecimal price,
        Integer quantity

) {
}
