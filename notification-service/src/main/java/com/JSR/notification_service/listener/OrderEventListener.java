package com.JSR.notification_service.listener;

import com.JSR.notification_service.Service.NotificationService;
import com.JSR.notification_service.dto.OrderPlacedEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class OrderEventListener {

    private  final NotificationService notificationService;

    @Autowired
    public OrderEventListener(NotificationService notificationService) {
        this.notificationService = notificationService;
    }


    @KafkaListener(topics = "notificationTopic", groupId = "notificationGroup")
    public void handleNotification(OrderPlacedEvent orderPlacedEvent) {
        log.info("✅ Received notification for order: {}", orderPlacedEvent.getOrderNumber());
        // Add your notification logic here (email, SMS, etc.)
    }

}
