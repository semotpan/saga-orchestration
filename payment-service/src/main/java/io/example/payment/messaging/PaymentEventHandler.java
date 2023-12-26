package io.example.payment.messaging;

import io.example.payment.Payment;
import io.example.payment.Payments;
import io.example.payment.messaging.log.EventLogs;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Component
@Transactional
@RequiredArgsConstructor
public class PaymentEventHandler {

    private final Logger logger = LoggerFactory.getLogger(PaymentEventHandler.class);

    private final ApplicationEventPublisher eventPublisher;
    private final EventLogs eventLogs;
    private final Payments payments;

    public void onPaymentEvent(UUID eventId, UUID sagaId, Payment event) {
        if (eventLogs.alreadyProcessed(eventId)) {
            logger.info("Event with UUID {} was already retrieved, ignoring it", eventId);
            return;
        }

        payments.save(event);
        eventPublisher.publishEvent(PaymentEvent.of(sagaId, event.paymentStatus()));
        eventLogs.processed(eventId);
    }
}
