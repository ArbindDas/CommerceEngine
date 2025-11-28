package com.JSR.order_service.controller;


import com.JSR.order_service.dto.OrderRequest;
import com.JSR.order_service.dto.OrderResponse;
import com.JSR.order_service.service.OrderService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

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


    @Autowired
    private KafkaTemplate<String, Object> kafkaTemplate;

    @GetMapping("/test-kafka")
    public String testKafkaDetailed() {
        try {
            log.info("🔍 Testing Kafka connection to: localhost:9093");

            // Test 1: Check if we can get producer config
            Map<String, Object> config = kafkaTemplate.getProducerFactory().getConfigurationProperties();
            log.info("Kafka config: {}", config);

            // Test 2: Send a simple string message (no serialization issues)
            String testMessage = "Simple test message " + System.currentTimeMillis();

            CompletableFuture<SendResult<String, Object>> future = kafkaTemplate.send(
                    "test-topic",
                    testMessage
            );

            // Wait for result with timeout
            SendResult<String, Object> result = future.get(10, TimeUnit.SECONDS);

            log.info("✅ Kafka test SUCCESSFUL! Topic: {}, Partition: {}, Offset: {}",
                    result.getRecordMetadata().topic(),
                    result.getRecordMetadata().partition(),
                    result.getRecordMetadata().offset());

            return "Kafka test SUCCESSFUL! Check logs for details.";

        } catch (TimeoutException e) {
            log.error("⏰ Kafka test TIMEOUT: {}", e.getMessage());
            return "Kafka test FAILED: Timeout - " + e.getMessage();
        } catch (ExecutionException e) {
            log.error("🚨 Kafka test EXECUTION ERROR: {}", e.getCause().getMessage());
            return "Kafka test FAILED: Execution - " + e.getCause().getMessage();
        } catch (InterruptedException e) {
            log.error("🚨 Kafka test INTERRUPTED: {}", e.getMessage());
            return "Kafka test FAILED: Interrupted - " + e.getMessage();
        } catch (Exception e) {
            log.error("🚨 Kafka test UNKNOWN ERROR: {}", e.getMessage(), e);
            return "Kafka test FAILED: Unknown - " + e.getMessage();
        }
    }
}
