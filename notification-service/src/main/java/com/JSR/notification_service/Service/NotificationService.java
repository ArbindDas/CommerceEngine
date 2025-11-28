package com.JSR.notification_service.Service;

import com.JSR.notification_service.dto.OrderPlacedEvent;
import io.micrometer.observation.Observation;
import io.micrometer.tracing.Tracer;
import io.micrometer.observation.ObservationRegistry;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class NotificationService {
    private final ObservationRegistry observationRegistry;
    private final Tracer tracer;

    @Autowired
    public NotificationService( ObservationRegistry observationRegistry, Tracer tracer ) {
        this.observationRegistry = observationRegistry;
        this.tracer = tracer;
    }


    public void handleOrderPlacedEvent( OrderPlacedEvent orderPlacedEvent ) {
        Observation.createNotStarted("on-message", this.observationRegistry).observe(() -> {
            log.info("Got Message<{}>", orderPlacedEvent);
            log.info("TraceId- {} Received Notification for Order - {}",
                    this.tracer.currentSpan().context().traceId(),
                    orderPlacedEvent.getOrderNumber());

            // send out an email notification
            sendEmailNotification(orderPlacedEvent);

        });
    }

    private void sendEmailNotification( OrderPlacedEvent orderPlacedEvent ) {
        // Implement email notification logic here
        log.info("Sending email notification for order: {}", orderPlacedEvent.getOrderNumber());
    }
}
