package com.JSR.inventory_service.dto;

import java.util.List;

// DTO (Data Transfer Object) representing a request to deduct inventory
public record InventoryDeductionRequest(
        List<InventoryDeductionItem> items  // List of items to deduct from inventory
) {
    // Using 'record' automatically creates constructor, getters, equals, hashCode, and toString
}
