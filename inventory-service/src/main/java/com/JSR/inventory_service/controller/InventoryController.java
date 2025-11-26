package com.JSR.inventory_service.controller;


import com.JSR.inventory_service.dto.InventoryDeductionRequest;
import com.JSR.inventory_service.dto.InventoryRequest;
import com.JSR.inventory_service.dto.InventoryResponse;
import com.JSR.inventory_service.service.InventoryService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("api/inventory")
@Slf4j
public class InventoryController {


    private final InventoryService inventoryService;

    @Autowired
    public InventoryController(InventoryService inventoryService) {
        this.inventoryService = inventoryService;
    }

    @GetMapping()
    public List<InventoryResponse> isInStock(@RequestParam List<String>  skuCode){
       return inventoryService.isInStock(skuCode);
    }


    @PostMapping()
    public ResponseEntity<?>createInventory(@RequestBody InventoryRequest request){
        InventoryResponse inventory = inventoryService.createInventory(request);
        return new ResponseEntity<>(inventory, HttpStatus.CREATED);
    }

    @GetMapping("/getAllInventory")
    public List<InventoryResponse>getAllInventory(){
       return inventoryService.getAllInventory();
    }


    // Controller method to handle inventory deduction requests
// NEW: Endpoint to deduct inventory
    @PutMapping(value = "/deduct")  // HTTP PUT endpoint at /api/inventory/deduct (assuming class-level mapping)
    public ResponseEntity<String> deductInventory(@RequestBody InventoryDeductionRequest request) {
        try {
            // Call service to deduct inventory based on request items
            inventoryService.deductInventory(request.items());

            // If successful, return HTTP 200 OK with a success message
            return ResponseEntity.ok("Inventory deducted successfully");
        } catch (Exception e) {
            // If any exception occurs (e.g., product not found or insufficient stock)
            // return HTTP 400 Bad Request with the error message
            return ResponseEntity.badRequest().body("Failed to deduct inventory: " + e.getMessage());
        }
    }

}
