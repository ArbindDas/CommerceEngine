package com.JSR.order_service.config;


import com.JSR.order_service.dto.InventoryResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

//@FeignClient(name = "INVENTORY-SERVICE", path = "api/inventory")
@FeignClient(name = "INVENTORY-SERVICE")
//Spring Cloud Feign will use service discovery (like Eureka) to locate the service by its name.
//Once it finds the service, you can call any endpoint on that service,
// e.g., /api/inventory, /api/inventory/deduct, /api/orders, etc.
public interface InventoryClient {

    @GetMapping("api/inventory")
    List<InventoryResponse> checkStock(@RequestParam("skuCode") List<String> skuCodes);

    // NEW: Method to deduct inventory
    @PutMapping("api/inventory/deduct")
    void deductInventory(@RequestBody Object deductionRequest); // Use Object or create proper DTO
}
