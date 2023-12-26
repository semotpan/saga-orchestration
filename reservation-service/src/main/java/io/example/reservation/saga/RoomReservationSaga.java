package io.example.reservation.saga;

import com.fasterxml.jackson.databind.node.ObjectNode;
import io.example.reservation.Reservation;
import io.example.reservation.Reservations;
import io.example.reservation.framework.Saga;
import io.example.reservation.framework.SagaState;
import io.example.reservation.framework.SagaStepStatus;
import io.example.reservation.messaging.PaymentEvent;
import io.example.reservation.messaging.RoomBookingEvent;
import jakarta.persistence.EntityManager;
import org.springframework.context.ApplicationEventPublisher;

import java.util.UUID;

import static io.example.reservation.framework.Saga.PayloadType.CANCEL;
import static io.example.reservation.framework.Saga.PayloadType.REQUEST;
import static io.example.reservation.framework.SagaStepStatus.COMPENSATING;
import static io.example.reservation.framework.SagaStepStatus.STARTED;
import static io.example.reservation.saga.RoomReservationSaga.SagaStateOrder.PAYMENT;
import static io.example.reservation.saga.RoomReservationSaga.SagaStateOrder.ROOM_BOOKING;

public final class RoomReservationSaga extends Saga {

    private final ApplicationEventPublisher eventPublisher;
    private final Reservations reservations;
    private final SagaState state;

    RoomReservationSaga(ApplicationEventPublisher eventPublisher,
                        EntityManager entityManager,
                        Reservations reservations,
                        SagaState state) {
        super(entityManager);
        this.eventPublisher = eventPublisher;
        this.reservations = reservations;
        this.state = state;
    }

    /**
     * Init Saga
     */
    public void init() {
        advance();
    }

    /**
     * handle Payment Event
     *
     * @param eventId - ensure one-time handling, consumed skip
     * @param payload
     */
    public void onPaymentEvent(UUID eventId, PaymentEvent payload) {
        ensureProcessed(eventId, () -> {
            onStepEvent(PAYMENT.topic, payload.status().toSagaStepStatus());
            updateBookingStatus();
        });
    }

    public void onSeatBookingEvent(UUID eventId, RoomBookingEvent payload) {
        ensureProcessed(eventId, () -> {
            onStepEvent(ROOM_BOOKING.topic, payload.status().toSagaStepStatus());
            updateBookingStatus();
        });
    }

    private void updateBookingStatus() {
        var booking = reservations.findById(new Reservation.ReservationIdentifier(getBookingId()))
                .orElseThrow(RuntimeException::new);

        if (state.sagaStatus().isCompleted()) {
            booking.markSucceed();
        } else if (state.sagaStatus().isAborted()) {
            booking.markFailed();
        }
    }

    private UUID getBookingId() {
        return UUID.fromString(state.payload().get("reservationId").asText());
    }

    private void onStepEvent(String step, SagaStepStatus status) {
        state.updateStepStatus(step, status);

        if (status.isSucceeded()) {
            advance();
        } else if (status.isFailedOrCompensated()) {
            goBack();
        }

        state.advanceSagaStatus();
    }

    private void advance() {
        var next = SagaStateOrder.next(state.currentStep());
        if (next == null) {
            state.currentStep(null);
            return;
        }

        eventPublisher.publishEvent(new SagaEvent(state.id(), next.topic, REQUEST.name(), state.payload()));

        state.updateStepStatus(next.topic, STARTED);
        state.currentStep(next.topic);
    }

    private void goBack() {
        var prev = SagaStateOrder.prev(state.currentStep());
        if (prev == null) {
            state.currentStep(null);
            return;
        }

        var payload = ((ObjectNode) state.payload().deepCopy());
        payload.put("type", CANCEL.name());

        eventPublisher.publishEvent(new SagaEvent(state.id(), prev.topic, CANCEL.name(), payload));

        state.updateStepStatus(prev.topic, COMPENSATING);
        state.currentStep(prev.topic);
    }

    /**
     * FIXME: provide a better solution
     * Saga State Machine
     */
    enum SagaStateOrder {
        ROOM_BOOKING("room-booking") {
            @Override
            public SagaStateOrder next() {
                return PAYMENT;
            }

            @Override
            public SagaStateOrder prev() {
                return null;
            }
        },
        PAYMENT("payment") {
            @Override
            public SagaStateOrder next() {
                return null;
            }

            @Override
            public SagaStateOrder prev() {
                return ROOM_BOOKING;
            }
        };

        public final String topic;

        SagaStateOrder(String topic) {
            this.topic = topic;
        }

        abstract SagaStateOrder next();

        abstract SagaStateOrder prev();

        static SagaStateOrder startStep() {
            return ROOM_BOOKING;
        }

        static SagaStateOrder next(String topic) {
            if (topic == null) {
                return startStep();
            }

            for (var t : values()) {
                if (t.topic.equals(topic))
                    return t.next();
            }

            return null;
        }

        static SagaStateOrder prev(String topic) {
            if (topic == null) {
                return null;
            }

            for (var t : values()) {
                if (t.topic.equals(topic))
                    return t.prev();
            }

            return null;
        }
    }
}
