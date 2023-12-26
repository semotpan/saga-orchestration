package io.example.reservation;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface Reservations extends CrudRepository<Reservation, Reservation.ReservationIdentifier> {

    @Query("""
            SELECT r.status
            FROM Reservation r
            WHERE r.id = :id
            """)
    Optional<Reservation.Status> queryReservationStatus(Reservation.ReservationIdentifier id);
}
