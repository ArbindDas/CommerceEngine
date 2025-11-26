package com.JSR.order_service.dto;


import java.util.List;

public record InventoryDeductionRequest(
        List<InventoryDeductionItem> items
) {}
