package com.JSR.inventory_service.dto;

public record InventoryRequest(
        String skuCode,
        Integer quantity
) {
}
