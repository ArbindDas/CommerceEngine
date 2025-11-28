//package com.JSR.order_service.service.impl;
//
//import com.JSR.order_service.config.InventoryClient;
//import com.JSR.order_service.dto.*;
//import com.JSR.order_service.entites.Order;
//import com.JSR.order_service.entites.OrderLineItems;
//import com.JSR.order_service.exception.InventoryServiceException;
//import com.JSR.order_service.exception.OutOfStockException;
//import com.JSR.order_service.repository.OrderRepository;
//import com.JSR.order_service.service.OrderService;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.stereotype.Service;
//import org.springframework.transaction.annotation.Transactional;
//
//import java.util.List;
//import java.util.UUID;
//
//@Service
//@Transactional
//@Slf4j
//public class OrderServiceImpl implements OrderService {
//
//    private final OrderRepository orderRepository;
//
//    private final InventoryClient inventoryClient;
//
//    @Autowired
//    public OrderServiceImpl(OrderRepository orderRepository, InventoryClient inventoryClient) {
//        this.orderRepository = orderRepository;
//        this.inventoryClient = inventoryClient;
//    }
//
//
//
//    @Override
//    public OrderResponse placeOrder(OrderRequest request) {
//
//        // STEP 1: Extract SKU codes from order request
//        List<String> skuCodes = request.orderLineItems()
//                .stream()
//                .map(OrderLIneItemRequest::skuCode)
//                .toList();
//
//        log.info("Placing order for SKUs: {}", skuCodes);
//
//        // STEP 2: call Inventory service to check stock using Feign client
//        boolean allInStock;
//        try {
//            allInStock = checkInventoryStock(skuCodes);
//        } catch (InventoryServiceException e) {
//            log.error("Inventory service call failed for SKUs {}: {}", skuCodes, e.getMessage(), e);
//            throw e; // propagate so that GlobalExceptionHandler returns 503
//        }
//
//        if (!allInStock) {
//            log.warn("Order cannot be placed. Some products are not in stock: {}", skuCodes);
//            throw new IllegalArgumentException("Some products are not in stock. Please check inventory.");
//        }
//
//        // STEP 3: Create a new Order entity
//        Order order = new Order();
//        order.setOrderName(request.orderName() != null ? request.orderName() : UUID.randomUUID().toString());
//
//        // STEP 4: Convert Request DTOs to Entity List
//        List<OrderLineItems> orderLineItems = request.orderLineItems()
//                .stream()
//                .map(requestItem -> mapToOrderLineItem(requestItem, order))
//                .toList();
//
//        order.setOrderLineItemsList(orderLineItems);
//
//        // STEP 5: Save the order to database
//        Order savedOrder = orderRepository.save(order);
//        log.info("Order placed successfully with id: {}", savedOrder.getId());
//
//        // STEP 5: Deduct inventory after order is saved
//        deductInventory(request.orderLineItems());
//
//        // STEP 6: Convert saved Entity to Response DTO and return
//        return mapToResponseDto(savedOrder);
//    }
//
//
//
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
//                throw new OutOfStockException(outOfStockSkus);  // Will propagate naturally
//            }
//
//            return true;
//
//        } catch (OutOfStockException e) {
//            throw e; // DO NOT wrap out-of-stock in InventoryServiceException
//        } catch (Exception e) {
//            // Only catch network/service failures
//            throw new InventoryServiceException("Error calling inventory service", e);
//        }
//    }
//
//
//    private void deductInventory(List<OrderLIneItemRequest> orderLineItems) {
//        try {
//            // Create deduction request
//            List<InventoryDeductionItem> deductionItems = orderLineItems.stream()
//                    .map(item -> new InventoryDeductionItem(item.skuCode(), item.quantity()))
//                    .toList();
//
//            InventoryDeductionRequest deductionRequest = new InventoryDeductionRequest(deductionItems);
//
//            // Call inventory service to deduct stock
//            inventoryClient.deductInventory(deductionRequest);
//
//        } catch (Exception e) {
//            throw new RuntimeException("Failed to deduct inventory: " + e.getMessage());
//        }
//    }
//
//
//
//
//
//    /**
//     * STEP 5: Convert Saved Order Entity to Response DTO
//     * This ensures we don't expose our database entities directly
//     */
//    private OrderResponse mapToResponseDto(Order savedOrder) {
//        // Convert each OrderLineItems entity to OrderLineItemResponse DTO
//        List<OrderLineItemResponse> orderLineItemResponses = savedOrder.getOrderLineItemsList()
//                .stream()                                   // Stream the entity list
//                .map(this::mapToOrderLineItemsResponse)     // Convert each entity to response DTO
//                .toList();                                  // Convert back to list
//
//        // Create final response with all converted data
//        return new OrderResponse(
//                savedOrder.getId(),                     // Order ID from database
//                savedOrder.getOrderName(),              // Order name
//                orderLineItemResponses                  // List of Response DTOs (NOT entities)
//        );
//    }
//
//    /**
//     * HELPER: Convert OrderLineItems ENTITY to OrderLineItemResponse DTO
//     * This is used in STEP 5
//     */
//    private OrderLineItemResponse mapToOrderLineItemsResponse(OrderLineItems orderLineItems) {
//        return new OrderLineItemResponse(
//                orderLineItems.getId(),             // ID generated by database
//                orderLineItems.getSkuCode(),        // Product code
//                orderLineItems.getPrice(),          // Price
//                orderLineItems.getQuantity()        // Quantity
//
//        );
//    }
//
//    /**
//     * HELPER: Convert OrderLIneItemRequest DTO to OrderLineItems ENTITY
//     * This is used in STEP 2
//     */
//    private OrderLineItems mapToOrderLineItem(OrderLIneItemRequest request , Order order) {
//        OrderLineItems orderLineItems = new OrderLineItems();
//        orderLineItems.setSkuCode(request.skuCode());       // Set product code from request
//        orderLineItems.setQuantity(request.quantity());     // Set quantity from request
//        orderLineItems.setPrice(request.price());           // Set price from request
//        orderLineItems.setOrder(order);                   // ✅ SET THE ORDER REFERENC
//        return orderLineItems;                              // Return the created entity
//    }
//}
//
//package com.JSR.order_service.service.impl;
//import com.JSR.order_service.config.InventoryClient;
//import com.JSR.order_service.dto.*;
//import com.JSR.order_service.entites.Order;
//import com.JSR.order_service.entites.OrderLineItems;
//import com.JSR.order_service.exception.InventoryServiceException;
//import com.JSR.order_service.exception.OutOfStockException;
//import com.JSR.order_service.repository.OrderRepository;
//import com.JSR.order_service.service.OrderService;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.stereotype.Service;
//import org.springframework.transaction.annotation.Transactional;
//
//import java.util.List;
//import java.util.UUID;
//import java.util.concurrent.CompletableFuture;
//import java.util.concurrent.CompletionException;
//
//@Service
//@Transactional
//@Slf4j
//public class OrderServiceImpl implements OrderService {
//
//    private final OrderRepository orderRepository;
//    private final InventoryClient inventoryClient;
//
//    @Autowired
//    public OrderServiceImpl(OrderRepository orderRepository, InventoryClient inventoryClient) {
//        this.orderRepository = orderRepository;
//        this.inventoryClient = inventoryClient;
//    }
//
//    @Override
//    public OrderResponse placeOrder(OrderRequest request) {
//        // STEP 1: Extract SKU codes from order request
//        List<String> skuCodes = request.orderLineItems()
//                .stream()
//                .map(OrderLIneItemRequest::skuCode)
//                .toList();
//
//        log.info("Placing order for SKUs: {}", skuCodes);
//
//        // STEP 2: Check inventory stock asynchronously
//        boolean allInStock;
//        try {
//            allInStock = checkInventoryStock(skuCodes).join(); // Block until completion
//        } catch (CompletionException e) {
//            Throwable cause = e.getCause();
//            if (cause instanceof OutOfStockException) {
//                throw (OutOfStockException) cause;
//            } else if (cause instanceof InventoryServiceException) {
//                throw (InventoryServiceException) cause;
//            } else {
//                log.error("Unexpected error during inventory check for SKUs {}: {}", skuCodes, cause.getMessage(), cause);
//                throw new InventoryServiceException("Unexpected error during inventory check", cause);
//            }
//        }
//
//        if (!allInStock) {
//            log.warn("Order cannot be placed. Some products are not in stock: {}", skuCodes);
//            throw new IllegalArgumentException("Some products are not in stock. Please check inventory.");
//        }
//
//        // STEP 3: Create a new Order entity
//        Order order = new Order();
//        order.setOrderName(request.orderName() != null ? request.orderName() : UUID.randomUUID().toString());
//
//        // STEP 4: Convert Request DTOs to Entity List
//        List<OrderLineItems> orderLineItems = request.orderLineItems()
//                .stream()
//                .map(requestItem -> mapToOrderLineItem(requestItem, order))
//                .toList();
//
//        order.setOrderLineItemsList(orderLineItems);
//
//        // STEP 5: Save the order to database
//        Order savedOrder = orderRepository.save(order);
//        log.info("Order placed successfully with id: {}", savedOrder.getId());
//
//        // STEP 6: Deduct inventory asynchronously (fire and forget)
//        deductInventoryAsync(request.orderLineItems());
//
//        // STEP 7: Convert saved Entity to Response DTO and return
//        return mapToResponseDto(savedOrder);
//    }
//
//    /**
//     * Asynchronous inventory stock check
//     */
//    private CompletableFuture<Boolean> checkInventoryStock(List<String> skuCodes) {
//        return inventoryClient.checkStock(skuCodes)
//                .thenApply(inventoryResponses -> {
//                    // Find SKUs that are out of stock
//                    List<String> outOfStockSkus = inventoryResponses.stream()
//                            .filter(resp -> !resp.isInStock())
//                            .map(InventoryResponse::skuCode)
//                            .toList();
//
//                    if (!outOfStockSkus.isEmpty()) {
//                        throw new CompletionException(new OutOfStockException(outOfStockSkus));
//                    }
//                    return true;
//                })
//                .exceptionally(throwable -> {
//                    // Handle service failures
//                    if (throwable.getCause() instanceof OutOfStockException) {
//                        throw new CompletionException(throwable.getCause());
//                    } else {
//                        log.error("Inventory service call failed for SKUs {}: {}", skuCodes, throwable.getMessage(), throwable);
//                        throw new CompletionException(new InventoryServiceException("Error calling inventory service", throwable.getCause()));
//                    }
//                });
//    }
//
//    /**
//     * Asynchronous inventory deduction (fire and forget)
//     */
//    private void deductInventoryAsync(List<OrderLIneItemRequest> orderLineItems) {
//        try {
//            // Create deduction request
//            List<InventoryDeductionItem> deductionItems = orderLineItems.stream()
//                    .map(item -> new InventoryDeductionItem(item.skuCode(), item.quantity()))
//                    .toList();
//
//            InventoryDeductionRequest deductionRequest = new InventoryDeductionRequest(deductionItems);
//
//            // Call inventory service asynchronously
//            inventoryClient.deductInventory(deductionRequest)
//                    .whenComplete((result, throwable) -> {
//                        if (throwable != null) {
//                            log.error("Failed to deduct inventory asynchronously: {}", throwable.getMessage());
//                            // You can implement retry logic or compensation logic here
//                            // For now, just log the error since this is fire-and-forget
//                        } else {
//                            log.info("Inventory deduction completed successfully");
//                        }
//                    });
//
//        } catch (Exception e) {
//            log.error("Error preparing inventory deduction request: {}", e.getMessage());
//            // Don't throw exception here as this is fire-and-forget
//        }
//    }
//
//    /**
//     * Alternative synchronous version if you prefer blocking calls
//     */
//    private void deductInventorySync(List<OrderLIneItemRequest> orderLineItems) {
//        try {
//            // Create deduction request
//            List<InventoryDeductionItem> deductionItems = orderLineItems.stream()
//                    .map(item -> new InventoryDeductionItem(item.skuCode(), item.quantity()))
//                    .toList();
//
//            InventoryDeductionRequest deductionRequest = new InventoryDeductionRequest(deductionItems);
//
//            // Call inventory service synchronously (block until complete)
//            inventoryClient.deductInventory(deductionRequest).join();
//            log.info("Inventory deduction completed successfully");
//
//        } catch (Exception e) {
//            log.error("Failed to deduct inventory: {}", e.getMessage());
//            // You can choose to throw exception or handle gracefully
//            throw new RuntimeException("Failed to deduct inventory: " + e.getMessage());
//        }
//    }
//
//    /**
//     * STEP 5: Convert Saved Order Entity to Response DTO
//     */
//    private OrderResponse mapToResponseDto(Order savedOrder) {
//        List<OrderLineItemResponse> orderLineItemResponses = savedOrder.getOrderLineItemsList()
//                .stream()
//                .map(this::mapToOrderLineItemsResponse)
//                .toList();
//
//        return new OrderResponse(
//                savedOrder.getId(),
//                savedOrder.getOrderName(),
//                orderLineItemResponses
//        );
//    }
//
//    /**
//     * HELPER: Convert OrderLineItems ENTITY to OrderLineItemResponse DTO
//     */
//    private OrderLineItemResponse mapToOrderLineItemsResponse(OrderLineItems orderLineItems) {
//        return new OrderLineItemResponse(
//                orderLineItems.getId(),
//                orderLineItems.getSkuCode(),
//                orderLineItems.getPrice(),
//                orderLineItems.getQuantity()
//        );
//    }
//
//    /**
//     * HELPER: Convert OrderLIneItemRequest DTO to OrderLineItems ENTITY
//     */
//    private OrderLineItems mapToOrderLineItem(OrderLIneItemRequest request, Order order) {
//        OrderLineItems orderLineItems = new OrderLineItems();
//        orderLineItems.setSkuCode(request.skuCode());
//        orderLineItems.setQuantity(request.quantity());
//        orderLineItems.setPrice(request.price());
//        orderLineItems.setOrder(order);
//        return orderLineItems;
//    }
//}

package com.JSR.order_service.service.impl;
import com.JSR.order_service.Event.OrderPlacedEvent;
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
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.concurrent.CompletableFuture;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.transaction.annotation.Transactional;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@Service
@Transactional
@Slf4j
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;
    private final InventoryClient inventoryClient;


    private final KafkaTemplate<String , Object> kafkaTemplate;

    @Autowired
    public OrderServiceImpl(OrderRepository orderRepository, InventoryClient inventoryClient, KafkaTemplate<String, Object> kafkaTemplate) {
        this.orderRepository = orderRepository;
        this.inventoryClient = inventoryClient;
        this.kafkaTemplate = kafkaTemplate;
    }

//    @Override
//    public OrderResponse placeOrder(OrderRequest request) {
//        // STEP 1: Extract SKU codes from order request
//        List<String> skuCodes = request.orderLineItems()
//                .stream()
//                .map(OrderLIneItemRequest::skuCode)
//                .toList();
//
//        log.info("Placing order for SKUs: {}", skuCodes);
//
//        // STEP 2: Check inventory stock SYNCHRONOUSLY
//        List<InventoryResponse> inventoryResponses;
//        try {
//            inventoryResponses = inventoryClient.checkStock(skuCodes);
//        } catch (InventoryServiceException e) {
//            log.error("Inventory service unavailable during stock check for SKUs: {}", skuCodes);
//            throw new InventoryServiceException("Cannot place order - inventory service unavailable");
//        }
//
//        // STEP 3: Validate stock availability
//        validateStockAvailability(inventoryResponses, skuCodes);
//
//        // STEP 4: Create and save order
//        Order order = createOrderEntity(request);
//        Order savedOrder = orderRepository.save(order);
//        // Ensure the topic exists before sending
//        kafkaTemplate.send("notificationTopic", new OrderPlacedEvent(order.getOrderName()));
//        log.info("Order created successfully with id: {}", savedOrder.getId());
//
//        // STEP 5: Deduct inventory SYNCHRONOUSLY with compensation
//        try {
//            deductInventorySync(request.orderLineItems());
//            log.info("Inventory deducted successfully for order id: {}", savedOrder.getId());
//        } catch (Exception e) {
//            // COMPENSATION: Delete the order if inventory deduction fails
//            orderRepository.delete(savedOrder);
//            log.error("Failed to deduct inventory, order {} rolled back: {}", savedOrder.getId(), e.getMessage());
//            throw new InventoryServiceException("Order cancelled - failed to deduct inventory");
//        }
//
//        // STEP 6: Convert to response DTO
//        return mapToResponseDto(savedOrder);
//    }


    @Override
    @Transactional
    public OrderResponse placeOrder(OrderRequest request) {
        // STEP 1: Extract SKU codes from order request
        List<String> skuCodes = request.orderLineItems()
                .stream()
                .map(OrderLIneItemRequest::skuCode)
                .toList();

        log.info("Placing order for SKUs: {}", skuCodes);

        // STEP 2: Check inventory stock SYNCHRONOUSLY
        List<InventoryResponse> inventoryResponses;
        try {
            inventoryResponses = inventoryClient.checkStock(skuCodes);
        } catch (InventoryServiceException e) {
            log.error("Inventory service unavailable during stock check for SKUs: {}", skuCodes);
            throw new InventoryServiceException("Cannot place order - inventory service unavailable");
        }

        // STEP 3: Validate stock availability
        validateStockAvailability(inventoryResponses, skuCodes);

        // STEP 4: Create and save order
        Order order = createOrderEntity(request);
        Order savedOrder = orderRepository.save(order);

        // STEP 5: Send Kafka message with proper error handling
        sendOrderNotification(savedOrder);

        // STEP 6: Deduct inventory SYNCHRONOUSLY with compensation
        try {
            deductInventorySync(request.orderLineItems());
            log.info("Inventory deducted successfully for order id: {}", savedOrder.getId());
        } catch (Exception e) {
            // COMPENSATION: Delete the order if inventory deduction fails
            orderRepository.delete(savedOrder);
            log.error("Failed to deduct inventory, order {} rolled back: {}", savedOrder.getId(), e.getMessage());
            throw new InventoryServiceException("Order cancelled - failed to deduct inventory");
        }

        // STEP 7: Convert to response DTO
        return mapToResponseDto(savedOrder);
    }

    /**
     * Send order notification to Kafka with robust error handling
     */
    /**
     /**
     * Send order notification to Kafka - simplified version
     */
    private void sendOrderNotification(Order order) {
        try {
            OrderPlacedEvent event = new OrderPlacedEvent(order.getOrderName());
            log.info("📤 Sending Kafka message for order: {}", order.getOrderName());

            // Simple approach without custom headers
            CompletableFuture<SendResult<String, Object>> future =
                    kafkaTemplate.send("notificationTopic", event);

            future.whenComplete((result, throwable) -> {
                if (throwable != null) {
                    log.error("❌ Failed to send Kafka message for order {}: {}",
                            order.getOrderName(), throwable.getMessage());
                } else {
                    log.info("✅ Kafka message sent successfully for order {}. Offset: {}, Partition: {}",
                            order.getOrderName(),
                            result.getRecordMetadata().offset(),
                            result.getRecordMetadata().partition());
                }
            });

        } catch (Exception e) {
            log.error("❌ Exception while sending Kafka message for order {}: {}",
                    order.getOrderName(), e.getMessage());
        }
    }
//     * Validate stock availability from inventory responses
//     */
    private void validateStockAvailability(List<InventoryResponse> responses, List<String> skuCodes) {
        // Check if we got responses for all SKUs
        if (responses.size() != skuCodes.size()) {
            log.warn("Inventory response incomplete. Expected: {}, Got: {}", skuCodes.size(), responses.size());
            throw new InventoryServiceException("Incomplete inventory response");
        }

        // Find SKUs that are out of stock
        List<String> outOfStockSkus = responses.stream()
                .filter(resp -> !resp.isInStock())
                .map(InventoryResponse::skuCode)
                .toList();

        if (!outOfStockSkus.isEmpty()) {
            log.warn("Order cannot be placed. Out of stock SKUs: {}", outOfStockSkus);
            throw new OutOfStockException(outOfStockSkus);
        }

        log.info("All products are in stock: {}", skuCodes);
    }

    /**
     * Synchronous inventory deduction with compensation
     */
    private void deductInventorySync(List<OrderLIneItemRequest> orderLineItems) {
        try {
            // Create deduction request
            List<InventoryDeductionItem> deductionItems = orderLineItems.stream()
                    .map(item -> new InventoryDeductionItem(item.skuCode(), item.quantity()))
                    .toList();

            InventoryDeductionRequest deductionRequest = new InventoryDeductionRequest(deductionItems);

            // Call inventory service synchronously
            inventoryClient.deductInventory(deductionRequest);

        } catch (InventoryServiceException e) {
            log.error("Inventory service call failed during deduction: {}", e.getMessage());
            throw new InventoryServiceException("Failed to deduct inventory");
        } catch (Exception e) {
            log.error("Unexpected error during inventory deduction: {}", e.getMessage());
            throw new InventoryServiceException("Unexpected error during inventory deduction");
        }
    }

    /**
     * Create order entity from request
     */
    private Order createOrderEntity(OrderRequest request) {
        Order order = new Order();
        order.setOrderName(request.orderName() != null ? request.orderName() : UUID.randomUUID().toString());

        List<OrderLineItems> orderLineItems = request.orderLineItems()
                .stream()
                .map(requestItem -> mapToOrderLineItem(requestItem, order))
                .toList();

        order.setOrderLineItemsList(orderLineItems);
        return order;
    }

    /**
     * Convert Saved Order Entity to Response DTO
     */
    private OrderResponse mapToResponseDto(Order savedOrder) {
        List<OrderLineItemResponse> orderLineItemResponses = savedOrder.getOrderLineItemsList()
                .stream()
                .map(this::mapToOrderLineItemsResponse)
                .toList();

        return new OrderResponse(
                savedOrder.getId(),
                savedOrder.getOrderName(),
                orderLineItemResponses
        );
    }

    /**
     * Convert OrderLineItems ENTITY to OrderLineItemResponse DTO
     */
    private OrderLineItemResponse mapToOrderLineItemsResponse(OrderLineItems orderLineItems) {
        return new OrderLineItemResponse(
                orderLineItems.getId(),
                orderLineItems.getSkuCode(),
                orderLineItems.getPrice(),
                orderLineItems.getQuantity()
        );
    }

    /**
     * Convert OrderLIneItemRequest DTO to OrderLineItems ENTITY
     */
    private OrderLineItems mapToOrderLineItem(OrderLIneItemRequest request, Order order) {
        OrderLineItems orderLineItems = new OrderLineItems();
        orderLineItems.setSkuCode(request.skuCode());
        orderLineItems.setQuantity(request.quantity());
        orderLineItems.setPrice(request.price());
        orderLineItems.setOrder(order);
        return orderLineItems;
    }
}