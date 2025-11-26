package com.JSR.order_service.dto;

import java.math.BigDecimal;

public record OrderLIneItemRequest(

        String skuCode,
        BigDecimal price,
        Integer quantity
) {
}
