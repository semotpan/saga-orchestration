package io.example.reservation.messaging;

import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

import java.util.UUID;

import static org.springframework.kafka.support.KafkaHeaders.RECEIVED_KEY;

@Component
@RequiredArgsConstructor
public class KafkaFlightBookingConsumer {

    private final String EVENT_ID = "id";
    private final String EVENT_TYPE = "eventType";

    private final Logger logger = LoggerFactory.getLogger(KafkaFlightBookingConsumer.class);

    private final ReservationPlacementEventHandler reservationPlacementEventHandler;

    @KafkaListener(topics = "${kafka.topic.saga.payment.inbox.events}", containerFactory = "paymentKLCFactory")
    void listen(@Header(RECEIVED_KEY) UUID sagaId,
                @Header(EVENT_ID) String eventId,
                @Header(EVENT_TYPE) String eventType,
                @Payload PaymentEvent payload) {
        logger.info("Kafka message with key = {}, eventType {} arrived, {}", sagaId, eventType, payload);
        reservationPlacementEventHandler.onPaymentEvent(sagaId, UUID.fromString(eventId), payload);
    }

    @KafkaListener(topics = "${kafka.topic.saga.roombooking.inbox.events}", containerFactory = "roomBookingKLCFactory")
    void listen(@Header(RECEIVED_KEY) UUID sagaId,
                @Header(EVENT_ID) String eventId,
                @Header(EVENT_TYPE) String eventType,
                @Payload RoomBookingEvent payload) {
        logger.info("Kafka message with key = {}, eventType {} arrived {}", sagaId, eventType, payload);
        reservationPlacementEventHandler.onSeatBookingEvent(sagaId, UUID.fromString(eventId), payload);
    }
}
