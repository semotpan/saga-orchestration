package io.example.hotel.messaging;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.example.outbox.OutboxEvent;

import java.time.Instant;
import java.util.UUID;

public final class RoomBookingEvent implements OutboxEvent<UUID, JsonNode> {

    private static final ObjectMapper mapper = new ObjectMapper();

    private final UUID sagaId;
    private final Instant timestamp;
    private final JsonNode payload;

    private RoomBookingEvent(UUID sagaId, ObjectNode payload) {
        this.sagaId = sagaId;
        this.timestamp = Instant.now();
        this.payload = payload;
    }

    static RoomBookingEvent of(UUID sagaId, RoomBookingRequestStatus status) {
        var payload = mapper.createObjectNode()
                .put("status", status.name());

        return new RoomBookingEvent(sagaId, payload);
    }

    @Override
    public UUID aggregateId() {
        return sagaId;
    }

    @Override
    public String aggregateType() {
        return "room-booking"; // FIXME: use topic prefix
    }

    @Override
    public String type() {
        return "RoomUpdated";
    }

    @Override
    public Instant timestamp() {
        return timestamp;
    }

    @Override
    public JsonNode payload() {
        return payload;
    }
}
