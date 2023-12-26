package io.example.reservation.messaging;

import io.example.reservation.saga.SagaManager;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@RequiredArgsConstructor
@Service
public class ReservationPlacementEventHandler {

    private final SagaManager sagaManager;

    @Transactional
    public void onSeatBookingEvent(UUID sagaId, UUID eventId, RoomBookingEvent payload) {
        var saga = sagaManager.find(sagaId);

        if (saga == null) {
            return;
        }

        saga.onSeatBookingEvent(eventId, payload);
    }

    @Transactional
    public void onPaymentEvent(UUID sagaId, UUID eventId, PaymentEvent payload) {
        var saga = sagaManager.find(sagaId);

        if (saga == null) {
            return;
        }

        saga.onPaymentEvent(eventId, payload);
    }
}
