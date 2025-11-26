package com.JSR.order_service.dto;


public record InventoryDeductionItem(
        String skuCode,
        Integer quantity
) {}