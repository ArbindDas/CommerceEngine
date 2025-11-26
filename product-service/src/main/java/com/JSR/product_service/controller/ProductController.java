package com.JSR.product_service.controller;


import com.JSR.product_service.dto.ProductRequest;
import com.JSR.product_service.dto.ProductResponse;
import com.JSR.product_service.entites.Product;
import com.JSR.product_service.service.ProductService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/product")
public class ProductController {



    private final ProductService productService;

    @Autowired
    public ProductController(ProductService productService) {
        this.productService = productService;
    }


    @PostMapping()
    public ResponseEntity<ProductResponse>createProduct(@RequestBody ProductRequest request){
        ProductResponse add = productService.add(request);
        return new ResponseEntity<>(add, HttpStatus.CREATED);
    }


    @GetMapping()
    public ResponseEntity<List<ProductResponse>> getAllProduct(){
        List<ProductResponse> allProducts = productService.getAllProducts();
        return new ResponseEntity<>(allProducts, HttpStatus.OK);
    }



    @GetMapping("/{id}")
    public ResponseEntity<?>getProductById(@PathVariable String id){
        ProductResponse productById = productService.getProductById(id);
        return new ResponseEntity<>(productById ,HttpStatus.OK);
    }


    @DeleteMapping("/{id}")
    public ResponseEntity<?>deleteProductById(@PathVariable String id){
        productService.deleteProductById(id);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }


    @PutMapping("/{id}")
    public ResponseEntity<?>updateProductById(@PathVariable String id, @RequestBody ProductRequest request){

        log.info("started ....");
        ProductResponse productResponse = productService.updateProductById(id, request);
        return new ResponseEntity<>(productResponse , HttpStatus.OK);
    }

    @PatchMapping("/{id}")
    public ResponseEntity<?>updateProductByIdPatch(@PathVariable String id , @RequestBody ProductRequest request){
        ProductResponse productResponse = productService.updateProductByIdPatch(id, request);
        return new ResponseEntity<>(productResponse, HttpStatus.OK);
    }

}
