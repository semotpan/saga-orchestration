package io.example.hotel.messaging;

import io.example.hotel.Room;
import io.example.hotel.Rooms;
import io.example.hotel.messaging.log.EventLogs;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@Transactional
@RequiredArgsConstructor
class RoomBookingEventHandler {

    private static final Logger logger = LoggerFactory.getLogger(RoomBookingEventHandler.class);

    private final ApplicationEventPublisher eventPublisher;
    private final EventLogs eventLogs;
    private final Rooms rooms;

    void onBookSeatEvent(UUID sagaId, UUID eventId, RoomBookingEventPayload payload) {
        if (eventLogs.alreadyProcessed(eventId)) { // ensure idempotency
            logger.debug("Event with UUID {} was already retrieved, ignoring it", eventId);
            return;
        }

        var possibleRoom = rooms.findById(new Room.RoomIdentifier(payload.roomId()));

        final RoomBookingRequestStatus status;
        if (payload.isRequestType() || possibleRoom.isEmpty()) {
            if (possibleRoom.isEmpty() || possibleRoom.get().isBooked()) {
                status = RoomBookingRequestStatus.REJECTED;
            } else {
                possibleRoom.get().book();
                status = RoomBookingRequestStatus.BOOKED;
            }
        } else {
            possibleRoom.get().release();
            status = RoomBookingRequestStatus.CANCELLED;
        }

        eventPublisher.publishEvent(RoomBookingEvent.of(sagaId, status));
        eventLogs.processed(eventId); // mark as processed
    }
}
