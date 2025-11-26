package com.JSR.inventory_service.dto;

public record InventoryResponse(
        Long id,
        String skuCode,
        Boolean isInStock  // true/false based on quantity > 0

) {
}
