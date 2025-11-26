package com.JSR.product_service.dto;

public record ProductResponse(
        String id,
        String name,
        String description,
        java.math.BigDecimal price
) {
}
