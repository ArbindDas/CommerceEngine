package com.JSR.inventory_service.service.impl;

import com.JSR.inventory_service.dto.InventoryDeductionItem;
import com.JSR.inventory_service.dto.InventoryRequest;
import com.JSR.inventory_service.dto.InventoryResponse;
import com.JSR.inventory_service.entites.Inventory;
import com.JSR.inventory_service.repository.InventoryRepository;
import com.JSR.inventory_service.service.InventoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Inventory Service Implementation
 *
 * This class handles all business logic related to inventory.
 * It is part of the Inventory Microservice, which is responsible only for:
 *  - Checking stock availability
 *  - Validating product availability for orders
 *  - Returning inventory details
 *
 * This service is isolated (because microservices architecture requires separation of responsibilities).
 */
@Service
public class InventoryServiceImpl implements InventoryService {

    private final InventoryRepository inventoryRepository;

    @Autowired
    public InventoryServiceImpl(InventoryRepository inventoryRepository) {
        this.inventoryRepository = inventoryRepository;
    }

    /**
     * METHOD 1: isInStock()
     * ---------------------------------------------
     * PURPOSE:
     *   - Used by other microservices (Order-Service, Product-Service)
     *   - Checks if the requested SKU codes exist and have available stock.
     *
     * USAGE:
     *   - GET /api/inventory?skuCode=apple&skuCode=banana
     *   - Called by Order Service before placing an order.
     *
     * WHY IMPORTANT FOR MICROSERVICES:
     *   - Each service has its own database.
     *   - Order Service cannot access Inventory DB directly.
     *   - So it calls this method through REST/Feign/WebClient.
     *
     * @param skuCodes List of SKU codes to check
     * @return a list of InventoryResponse objects
     */
    @Override
    @Transactional(readOnly = true)
    public List<InventoryResponse> isInStock(List<String> skuCodes) {
        return inventoryRepository.findBySkuCodeIn(skuCodes)
                .stream()
                .map(inventory -> new InventoryResponse(
                        inventory.getId(),
                        inventory.getSkuCode(),
                        inventory.getQuantity() > 0
                ))
                .toList();
    }

    /**
     * METHOD 2: areAllInStock()
     * ---------------------------------------------
     * PURPOSE:
     *   - Validates if ALL requested items are in stock (used before confirming an order).
     *   - If ANY item is out of stock → the whole order should fail.
     *
     * USAGE:
     *   - Order Service calls this to validate ordering multiple items.
     *
     * WHY IMPORTANT FOR MICROSERVICES:
     *   - Avoids partial order completion.
     *   - Prevents Order Service from placing invalid orders.
     *
     * @param skuCodes List of SKU codes to check
     * @return true ONLY if all items are in stock
     */
    @Override
    @Transactional(readOnly = true)
    public boolean areAllInStock(List<String> skuCodes) {
        List<Inventory> inventories = inventoryRepository.findBySkuCodeIn(skuCodes);

        // Check if we found all SKUs and each one has quantity > 0
        return inventories.size() == skuCodes.size() &&
                inventories.stream().allMatch(inv -> inv.getQuantity() > 0);
    }

    /**
     * METHOD 3: checkStockWithDetails()
     * ---------------------------------------------
     * PURPOSE:
     *   - Provides detailed stock information for each item.
     *   - Similar to isInStock() but may be extended to include more details later
     *     (like quantity left, reserved stock, etc.)
     *
     * USAGE:
     *   - Used by dashboards or admin panel
     *   - Used by Order Service if detailed response is required
     *
     * WHY IMPORTANT FOR MICROSERVICES:
     *   - Keeps "Inventory-related logic" inside this microservice
     *   - Other services should not know how inventory is stored or calculated
     *
     * @param skuCodes List of SKUs to check
     * @return detailed stock status for each item
     */
    @Override
    @Transactional(readOnly = true)
    public List<InventoryResponse> checkStockWithDetails(List<String> skuCodes) {
        return inventoryRepository.findBySkuCodeIn(skuCodes)
                .stream()
                .map(inventory -> new InventoryResponse(
                        inventory.getId(),
                        inventory.getSkuCode(),
                        inventory.getQuantity() > 0
                ))
                .toList();
    }

    @Override
    public InventoryResponse createInventory(InventoryRequest request) {
        Inventory inventory = new Inventory();
        inventory.setQuantity(request.quantity());
        inventory.setSkuCode(request.skuCode());

        Inventory savedInventory = inventoryRepository.save(inventory);
//        return  new InventoryResponse(
//                savedInventory.getId(),
//                savedInventory.getSkuCode(),
//                savedInventory.getQuantity()>0
//        );

     return  mapToInventoryResponse(savedInventory);
    }

    @Override
    public List<InventoryResponse> getAllInventory() {
        return inventoryRepository.findAll()
                .stream()
                .map(this::mapToInventoryResponse)
                .toList();


    }

    @Override
// NEW: Method to deduct inventory
    @Transactional // Ensures all DB operations in this method are atomic; will rollback on exceptions
    public void deductInventory(List<InventoryDeductionItem> deductionItems) {

        // Loop through each item in the deduction request
        for (InventoryDeductionItem item : deductionItems) {

            // Fetch inventory record by SKU code from the database
            // If not found, throw RuntimeException
            Inventory inventory = inventoryRepository.findBySkuCode(item.skuCode())
                    .orElseThrow(() -> new RuntimeException("Product not found: " + item.skuCode()));

            // Check if there is enough quantity in stock
            // If not enough, throw RuntimeException
            if (inventory.getQuantity() < item.quantity()) {
                throw new RuntimeException("Insufficient stock for: " + item.skuCode());
            }

            // Deduct the requested quantity from the current stock
            inventory.setQuantity(inventory.getQuantity() - item.quantity());

            // Save the updated inventory back to the database
            inventoryRepository.save(inventory);
        }

    }

    // Helper method to map Inventory entity to InventoryResponse DTO
    private InventoryResponse mapToInventoryResponse(Inventory inventory){
        return new InventoryResponse(
                inventory.getId(),                  // ID of the inventory record
                inventory.getSkuCode(),             // SKU code of the product
                inventory.getQuantity() > 0         // true if in stock, false if quantity is 0
        );
    }



}
