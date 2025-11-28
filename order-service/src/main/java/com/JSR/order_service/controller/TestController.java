package com.JSR.order_service.controller;

import com.JSR.order_service.Event.OrderPlacedEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/test")
public class TestController {

    @Autowired
    private KafkaTemplate<String, Object> kafkaTemplate;

    @PostMapping("/kafka")
    public String testKafka() {
        try {
            OrderPlacedEvent event = new OrderPlacedEvent("test-order-" + System.currentTimeMillis());
            kafkaTemplate.send("notificationTopic", event);
            return "✅ Test message sent to Kafka successfully";
        } catch (Exception e) {
            return "❌ Failed to send test message: " + e.getMessage();
        }
    }
}