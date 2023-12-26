package io.example.reservation.saga;

import io.example.reservation.Reservation;
import io.example.reservation.Reservations;
import io.example.reservation.framework.Saga;
import io.example.reservation.framework.SagaState;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@RequiredArgsConstructor
public class SagaManager {

    private final ApplicationEventPublisher eventPublisher;
    private final EntityManager entityManager;
    private final Reservations reservations;

    public void begin(Reservation reservation) {
        var payload = reservation.toSagaPayload()
                .put("type", Saga.PayloadType.REQUEST.name());

        var sagaState = new SagaState("room-reservation", payload);
        entityManager.persist(sagaState);

        var saga = new RoomReservationSaga(eventPublisher, entityManager, reservations, sagaState);
        saga.init();
    }

    public RoomReservationSaga find(UUID sagaId) {
        var state = entityManager.find(SagaState.class, sagaId);

        if (state == null) {
            return null;
        }

        return new RoomReservationSaga(eventPublisher, entityManager, reservations, state);
    }
}
