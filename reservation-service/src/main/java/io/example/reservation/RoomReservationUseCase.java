package io.example.reservation;

import io.example.reservation.saga.SagaManager;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class RoomReservationUseCase {

    private final SagaManager sagaManager;
    private final Reservations reservations;

    public Reservation make(RoomReservationCmd cmd) {
        var reservation = reservations.save(cmd.toReservation());
        sagaManager.begin(reservation);
        return reservation;
    }
}
