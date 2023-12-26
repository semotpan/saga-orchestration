package io.example.hotel.messaging;

import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaHandler;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

import java.util.UUID;

import static org.springframework.kafka.support.KafkaHeaders.RECEIVED_KEY;

@KafkaListener(
        topics = "${kafka.topic.roombooking.inbox.events.name}",
        groupId = "${spring.kafka.consumer.group-id}",
        containerFactory = "roomBookingKLCFactory"
)
@Component
@RequiredArgsConstructor
class RoomBookingInboxEventsConsumer {

    private final Logger logger = LoggerFactory.getLogger(RoomBookingInboxEventsConsumer.class);

    private final RoomBookingEventHandler roomBookingEventHandler;

    @KafkaHandler
    public void handle(@Header(RECEIVED_KEY) UUID sagaId,
                       @Header("id") String eventId,
                       @Header("eventType") String eventType,
                       @Payload RoomBookingEventPayload payload) {
        logger.debug("Kafka message with key = {}, eventType {} arrived", sagaId, eventType);
        roomBookingEventHandler.onBookSeatEvent(sagaId, UUID.fromString(eventId), payload);
    }
}
