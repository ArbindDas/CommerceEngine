package com.JSR.order_service.dto;

public record InventoryResponse(

        String skuCode,
        boolean isInStock,
        Integer quantity
) {

}
