package com.JSR.order_service.service.impl;

import com.JSR.order_service.config.InventoryClient;
import com.JSR.order_service.dto.*;
import com.JSR.order_service.entites.Order;
import com.JSR.order_service.entites.OrderLineItems;
import com.JSR.order_service.exception.InventoryServiceException;
import com.JSR.order_service.exception.OutOfStockException;
import com.JSR.order_service.repository.OrderRepository;
import com.JSR.order_service.service.OrderService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@Transactional
@Slf4j
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;

    private final InventoryClient inventoryClient;

    @Autowired
    public OrderServiceImpl(OrderRepository orderRepository, InventoryClient inventoryClient) {
        this.orderRepository = orderRepository;
        this.inventoryClient = inventoryClient;
    }

    /**
     * MAIN METHOD: Places a new order
     * Flow: Request DTO → Entity → Save to DB → Response DTO
     */
//    @Override
//    public OrderResponse placeOrder(OrderRequest request) {
//
//
//        // STEP 1: Extract SKU codes from order request
//        List<String>skuCodes = request.orderLineItems()
//                .stream()
//                .map(OrderLIneItemRequest::skuCode)
//                .toList();
//
//
//        // STEP 2: call Inventory service to check stock using Feign client
//
//        boolean allInStock = checkInventoryStock(skuCodes);
//
//        if (!allInStock){
//
//            throw new IllegalArgumentException("Some products are not in stock. Please check inventory.");
//        }else {
//
//            // STEP 1: Create a new Order entity
//            Order order = new Order();
//
//            // Set order name from request, or generate random if not provided
//            order.setOrderName(request.orderName() != null ? request.orderName() : UUID.randomUUID().toString());
//
//            // STEP 2: Convert Request DTOs to Entity List
//            List<OrderLineItems> orderLineItems = request.orderLineItems()
//                    .stream()                           // Convert list to stream for processing
//                    .map(requestItem -> mapToOrderLineItem(requestItem , order))      // Convert each DTO to Entity
//                    .toList();                          // Convert stream back to List
//
//            // STEP 3: Set the converted entities to the order
//            order.setOrderLineItemsList(orderLineItems);
//
//            // todo -->  call inventory service  and place order if product is in stock
//            // STEP 4: Save the order to database (this also saves order line items due to CascadeType.ALL)
//            Order savedOrder = orderRepository.save(order);
//
//            // STEP 5: Convert saved Entity to Response DTO and return
//            return mapToResponseDto(savedOrder);
//
//        }
//
//    }


    @Override
    public OrderResponse placeOrder(OrderRequest request) {

        // STEP 1: Extract SKU codes from order request
        List<String> skuCodes = request.orderLineItems()
                .stream()
                .map(OrderLIneItemRequest::skuCode)
                .toList();

        log.info("Placing order for SKUs: {}", skuCodes);

        // STEP 2: call Inventory service to check stock using Feign client
        boolean allInStock;
        try {
            allInStock = checkInventoryStock(skuCodes);
        } catch (InventoryServiceException e) {
            log.error("Inventory service call failed for SKUs {}: {}", skuCodes, e.getMessage(), e);
            throw e; // propagate so that GlobalExceptionHandler returns 503
        }

        if (!allInStock) {
            log.warn("Order cannot be placed. Some products are not in stock: {}", skuCodes);
            throw new IllegalArgumentException("Some products are not in stock. Please check inventory.");
        }

        // STEP 3: Create a new Order entity
        Order order = new Order();
        order.setOrderName(request.orderName() != null ? request.orderName() : UUID.randomUUID().toString());

        // STEP 4: Convert Request DTOs to Entity List
        List<OrderLineItems> orderLineItems = request.orderLineItems()
                .stream()
                .map(requestItem -> mapToOrderLineItem(requestItem, order))
                .toList();

        order.setOrderLineItemsList(orderLineItems);

        // STEP 5: Save the order to database
        Order savedOrder = orderRepository.save(order);
        log.info("Order placed successfully with id: {}", savedOrder.getId());

        // STEP 5: Deduct inventory after order is saved
        deductInventory(request.orderLineItems());

        // STEP 6: Convert saved Entity to Response DTO and return
        return mapToResponseDto(savedOrder);
    }

    /**
     * Call Inventory Microservice using Feign Client to check stock availability
     */
//    private boolean checkInventoryStock(List<String> skuCodes) {
//        try {
//            List<InventoryResponse> inventoryResponses = inventoryClient.checkStock(skuCodes);
//
//            // Check if all items are in stock
//            return inventoryResponses != null &&
//                    inventoryResponses.size() == skuCodes.size() &&
//                    inventoryResponses.stream().allMatch(InventoryResponse::isInStock);
//
//        } catch (Exception e) {
//            throw new RuntimeException("Error checking inventory: " + e.getMessage());
//        }
//    }


//    private boolean checkInventoryStock(List<String> skuCodes) {
//        try {
//            List<InventoryResponse> inventoryResponses = inventoryClient.checkStock(skuCodes);
//
//            // Find SKUs that are out of stock
//            List<String> outOfStockSkus = inventoryResponses.stream()
//                    .filter(resp -> !resp.isInStock())
//                    .map(InventoryResponse::skuCode)
//                    .toList();
//
//            if (!outOfStockSkus.isEmpty()) {
//                throw new OutOfStockException(outOfStockSkus);
//            }
//
//            return true;
//
//        } catch (Exception e) {
//            throw new InventoryServiceException("Error calling inventory service", e);
//        }
//    }



    private boolean checkInventoryStock(List<String> skuCodes) {
        try {
            List<InventoryResponse> inventoryResponses = inventoryClient.checkStock(skuCodes);

            // Find SKUs that are out of stock
            List<String> outOfStockSkus = inventoryResponses.stream()
                    .filter(resp -> !resp.isInStock())
                    .map(InventoryResponse::skuCode)
                    .toList();

            if (!outOfStockSkus.isEmpty()) {
                throw new OutOfStockException(outOfStockSkus);  // Will propagate naturally
            }

            return true;

        } catch (OutOfStockException e) {
            throw e; // DO NOT wrap out-of-stock in InventoryServiceException
        } catch (Exception e) {
            // Only catch network/service failures
            throw new InventoryServiceException("Error calling inventory service", e);
        }
    }


    private void deductInventory(List<OrderLIneItemRequest> orderLineItems) {
        try {
            // Create deduction request
            List<InventoryDeductionItem> deductionItems = orderLineItems.stream()
                    .map(item -> new InventoryDeductionItem(item.skuCode(), item.quantity()))
                    .toList();

            InventoryDeductionRequest deductionRequest = new InventoryDeductionRequest(deductionItems);

            // Call inventory service to deduct stock
            inventoryClient.deductInventory(deductionRequest);

        } catch (Exception e) {
            throw new RuntimeException("Failed to deduct inventory: " + e.getMessage());
        }
    }





    /**
     * STEP 5: Convert Saved Order Entity to Response DTO
     * This ensures we don't expose our database entities directly
     */
    private OrderResponse mapToResponseDto(Order savedOrder) {
        // Convert each OrderLineItems entity to OrderLineItemResponse DTO
        List<OrderLineItemResponse> orderLineItemResponses = savedOrder.getOrderLineItemsList()
                .stream()                                   // Stream the entity list
                .map(this::mapToOrderLineItemsResponse)     // Convert each entity to response DTO
                .toList();                                  // Convert back to list

        // Create final response with all converted data
        return new OrderResponse(
                savedOrder.getId(),                     // Order ID from database
                savedOrder.getOrderName(),              // Order name
                orderLineItemResponses                  // List of Response DTOs (NOT entities)
        );
    }

    /**
     * HELPER: Convert OrderLineItems ENTITY to OrderLineItemResponse DTO
     * This is used in STEP 5
     */
    private OrderLineItemResponse mapToOrderLineItemsResponse(OrderLineItems orderLineItems) {
        return new OrderLineItemResponse(
                orderLineItems.getId(),             // ID generated by database
                orderLineItems.getSkuCode(),        // Product code
                orderLineItems.getPrice(),          // Price
                orderLineItems.getQuantity()        // Quantity

        );
    }

    /**
     * HELPER: Convert OrderLIneItemRequest DTO to OrderLineItems ENTITY
     * This is used in STEP 2
     */
    private OrderLineItems mapToOrderLineItem(OrderLIneItemRequest request , Order order) {
        OrderLineItems orderLineItems = new OrderLineItems();
        orderLineItems.setSkuCode(request.skuCode());       // Set product code from request
        orderLineItems.setQuantity(request.quantity());     // Set quantity from request
        orderLineItems.setPrice(request.price());           // Set price from request
        orderLineItems.setOrder(order);                   // ✅ SET THE ORDER REFERENC
        return orderLineItems;                              // Return the created entity
    }
}