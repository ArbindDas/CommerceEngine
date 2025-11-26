
package com.JSR.inventory_service.dto;

// DTO representing a single item in the deduction request
public record InventoryDeductionItem(
        String skuCode,    // SKU code of the product to deduct
        Integer quantity   // Quantity to deduct
) {
    // 'record' provides immutable fields and auto-generated constructor & getters
}
