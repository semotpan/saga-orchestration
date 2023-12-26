package io.example.reservation.framework;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.hypersistence.utils.hibernate.type.json.JsonNodeBinaryType;
import jakarta.persistence.*;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Type;

import java.util.EnumSet;
import java.util.UUID;

import static io.example.reservation.framework.SagaStepStatus.*;
import static lombok.AccessLevel.PRIVATE;

@Entity
@Table(name = "sagastate")
@NoArgsConstructor(access = PRIVATE, force = true) // JPA compliant
public class SagaState {

    @Id
    private UUID id;

    @Version
    private int version;

    private String type;

    @Type(JsonNodeBinaryType.class)
    private JsonNode payload;

    private String currentStep;

    @Type(JsonNodeBinaryType.class)
    private ObjectNode stepStatus;

    @Enumerated(EnumType.STRING)
    private SagaStatus sagaStatus;

    public SagaState(String sagaType, JsonNode payload) {
        this.id = UUID.randomUUID();
        this.type = sagaType;
        this.payload = payload;
        this.sagaStatus = SagaStatus.STARTED;
        this.stepStatus = JsonNodeFactory.instance.objectNode();
    }

    public UUID id() {
        return id;
    }

    public JsonNode payload() {
        return payload;
    }

    public String currentStep() {
        return currentStep;
    }

    public void currentStep(String currentStep) {
        this.currentStep = currentStep;
    }

    public void updateStepStatus(String step, SagaStepStatus sagaStepStatus) {
        this.stepStatus.put(step, sagaStepStatus.name());
    }

    /**
     * Following SagaSteps To SagaStatus mapping:
     * 1. SUCCEEDED -> COMPLETED
     * 2. STARTED, SUCCEEDED -> STARTED
     * 3. FAILED, COMPENSATED -> ABORTED
     * 4. COMPENSATING, other -> ABORTING
     */
    public void advanceSagaStatus() {
        var bitmask = stepStatusToSet().stream()
                .mapToInt(status -> 1 << status.ordinal())
                .reduce(0, (a, b) -> a | b);

        if ((bitmask & (1 << SUCCEEDED.ordinal())) == bitmask) {
            sagaStatus = SagaStatus.COMPLETED;
        } else if ((bitmask & ((1 << STARTED.ordinal()) | (1 << SUCCEEDED.ordinal()))) == bitmask) {
            sagaStatus = SagaStatus.STARTED;
        } else if ((bitmask & ((1 << FAILED.ordinal()) | (1 << COMPENSATED.ordinal()))) == bitmask) {
            sagaStatus = SagaStatus.ABORTED;
        } else {
            sagaStatus = SagaStatus.ABORTING;
        }
    }

    private EnumSet<SagaStepStatus> stepStatusToSet() {
        EnumSet<SagaStepStatus> allStatus = EnumSet.noneOf(SagaStepStatus.class);
        stepStatus.fields()
                .forEachRemaining(entry -> allStatus.add(SagaStepStatus.valueOf(entry.getValue().asText())));

        return allStatus;
    }

    public SagaStatus sagaStatus() {
        return sagaStatus;
    }
}
