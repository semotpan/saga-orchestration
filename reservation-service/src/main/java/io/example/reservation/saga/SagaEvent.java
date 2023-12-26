package io.example.reservation.saga;

import com.fasterxml.jackson.databind.JsonNode;
import io.example.outbox.OutboxEvent;

import java.time.Instant;
import java.util.UUID;

public final class SagaEvent implements OutboxEvent<UUID, JsonNode> {

    private final UUID sagaId;
    private final String aggregateType;
    private final String eventType;
    private final Instant timestamp;
    private final JsonNode payload;

    public SagaEvent(UUID sagaId, String aggregateType, String eventType, JsonNode payload) {
        this.sagaId = sagaId;
        this.aggregateType = aggregateType;
        this.eventType = eventType;
        this.payload = payload;
        this.timestamp = Instant.now();
    }

    @Override
    public UUID aggregateId() {
        return sagaId;
    }

    @Override
    public String aggregateType() {
        return aggregateType;
    }

    @Override
    public String type() {
        return eventType;
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
