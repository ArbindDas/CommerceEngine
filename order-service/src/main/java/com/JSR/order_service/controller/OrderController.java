package com.JSR.order_service.controller;


import com.JSR.order_service.dto.OrderRequest;
import com.JSR.order_service.dto.OrderResponse;
import com.JSR.order_service.service.OrderService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/order")
@Slf4j
public class OrderController {


    private final OrderService orderService;

    @Autowired
    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }


    @PostMapping()
    public ResponseEntity<?>placeOrder(@RequestBody OrderRequest request){
        OrderResponse orderResponse = orderService.placeOrder(request);
        log.info("successfully the ordered is placed ");
        return new ResponseEntity<>(orderResponse , HttpStatus.CREATED);
    }
}
