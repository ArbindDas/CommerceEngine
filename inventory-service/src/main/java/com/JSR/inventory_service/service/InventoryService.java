package com.JSR.inventory_service.service;

import com.JSR.inventory_service.dto.InventoryDeductionItem;
import com.JSR.inventory_service.dto.InventoryRequest;
import com.JSR.inventory_service.dto.InventoryResponse;
import com.JSR.inventory_service.entites.Inventory;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Inventory Service Interface
 *
 * This interface defines the business operations related to inventory.
 *
 * Key points for microservices:
 *  - Each microservice has its own database (Inventory Service handles only inventory).
 *  - Other services (like Order Service) call Inventory Service via REST/Feign/WebClient.
 *  - This interface separates contract (what the service can do) from implementation (how it does it).
 */

public interface InventoryService {

    /**
     * METHOD 1: isInStock()
     * ---------------------------------------------
     * PURPOSE:
     *   - Checks whether each requested SKU code exists and has stock available.
     *
     * USAGE:
     *   - Called by Order Service to validate before placing an order.
     *   - Can also be used by Product Service or API Gateway to show stock status.
     *
     * @param skuCode List of SKU codes to check
     * @return List of InventoryResponse objects indicating if each item is in stock
     */
    List<InventoryResponse> isInStock(List<String> skuCode);

    /**
     * METHOD 2: areAllInStock()
     * ---------------------------------------------
     * PURPOSE:
     *   - Validates if ALL requested items are available in stock.
     *   - Ensures that orders are not partially fulfilled if some items are out of stock.
     *
     * USAGE:
     *   - Order Service calls this method to validate the full order before confirmation.
     *
     * @param skuCodes List of SKU codes to check
     * @return true if all items are in stock; false if any item is out of stock
     */
    boolean areAllInStock(List<String> skuCodes);

    /**
     * METHOD 3: checkStockWithDetails()
     * ---------------------------------------------
     * PURPOSE:
     *   - Provides detailed stock information for each SKU.
     *   - Can include quantity, availability, and extended details if needed in the future.
     *
     * USAGE:
     *   - Useful for dashboards, reporting, or Order Service when detailed stock info is needed.
     *
     * @param skuCodes List of SKU codes to check
     * @return List of InventoryResponse objects with detailed stock information
     */
    List<InventoryResponse> checkStockWithDetails(List<String> skuCodes);


    InventoryResponse createInventory(InventoryRequest request);

    List<InventoryResponse>getAllInventory();

    public void deductInventory(List<InventoryDeductionItem> deductionItems);

}
