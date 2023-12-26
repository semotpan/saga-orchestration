package io.example.payment.messaging;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.example.outbox.OutboxEvent;
import io.example.payment.PaymentStatus;

import java.time.Instant;
import java.util.UUID;

public final class PaymentEvent implements OutboxEvent<String, JsonNode> {

    private static final ObjectMapper mapper = new ObjectMapper();

    private final UUID sagaId;
    private final JsonNode payload;
    private final Instant timestamp;

    private PaymentEvent(UUID sagaId, JsonNode payload) {
        this.sagaId = sagaId;
        this.payload = payload;
        this.timestamp = Instant.now();
    }

    public static PaymentEvent of(UUID sagaId, PaymentStatus status) {
        ObjectNode asJson = mapper.createObjectNode()
                .put("status", status.name());

        return new PaymentEvent(sagaId, asJson);
    }

    @Override
    public String aggregateId() {
        return String.valueOf(sagaId);
    }

    @Override
    public String aggregateType() {
        return "payment";
    }

    @Override
    public JsonNode payload() {
        return payload;
    }

    @Override
    public String type() {
        return "PaymentUpdated";
    }

    @Override
    public Instant timestamp() {
        return timestamp;
    }
}
