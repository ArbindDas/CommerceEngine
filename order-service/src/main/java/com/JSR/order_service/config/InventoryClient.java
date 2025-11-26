//package com.JSR.order_service.config;
//
//
//import com.JSR.order_service.dto.InventoryResponse;
//import org.springframework.cloud.openfeign.FeignClient;
//import org.springframework.web.bind.annotation.GetMapping;
//import org.springframework.web.bind.annotation.PutMapping;
//import org.springframework.web.bind.annotation.RequestBody;
//import org.springframework.web.bind.annotation.RequestParam;
//
//import java.util.List;
//
////@FeignClient(name = "INVENTORY-SERVICE", path = "api/inventory")
//@FeignClient(name = "INVENTORY-SERVICE")
////Spring Cloud Feign will use service discovery (like Eureka) to locate the service by its name.
////Once it finds the service, you can call any endpoint on that service,
//// e.g., /api/inventory, /api/inventory/deduct, /api/orders, etc.
//public interface InventoryClient {
//
//    @GetMapping("api/inventory")
//    List<InventoryResponse> checkStock(@RequestParam("skuCode") List<String> skuCodes);
//
//    // NEW: Method to deduct inventory
//    @PutMapping("api/inventory/deduct")
//    void deductInventory(@RequestBody Object deductionRequest); // Use Object or create proper DTO
//}


package com.JSR.order_service.config;
import com.JSR.order_service.dto.InventoryResponse;
import com.JSR.order_service.dto.InventoryDeductionRequest;
import com.JSR.order_service.exception.InventoryServiceException;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@FeignClient(name = "INVENTORY-SERVICE")
public interface InventoryClient {

    @GetMapping("api/inventory")
    @CircuitBreaker(name = "inventory-service", fallbackMethod = "checkStockFallback")
    @Retry(name = "inventory-service", fallbackMethod = "checkStockFallback")
    List<InventoryResponse> checkStock(@RequestParam("skuCode") List<String> skuCodes);

    @PutMapping("api/inventory/deduct")
    @CircuitBreaker(name = "inventory-service", fallbackMethod = "deductInventoryFallback")
    @Retry(name = "inventory-service", fallbackMethod = "deductInventoryFallback")
    void deductInventory(@RequestBody InventoryDeductionRequest deductionRequest);

    // Fixed Fallback methods - EXACT SAME SIGNATURES
    default List<InventoryResponse> checkStockFallback(List<String> skuCodes, Throwable throwable) {
        System.err.println("Circuit Breaker Fallback: Inventory service unavailable for stock check. SKUs: " + skuCodes);
        throw new InventoryServiceException("Inventory service unavailable - cannot check stock for SKUs: " + skuCodes);
    }

    default void deductInventoryFallback(InventoryDeductionRequest deductionRequest, Throwable throwable) {
        System.err.println("Circuit Breaker Fallback: Inventory service unavailable for deduction");
        throw new InventoryServiceException("Inventory service unavailable - cannot deduct inventory");
    }
}