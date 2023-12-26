package io.example.reservation.web;

import io.example.reservation.Reservation;
import io.example.reservation.Reservations;
import io.example.reservation.RoomReservationCmd;
import io.example.reservation.RoomReservationUseCase;
import lombok.Builder;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.web.servlet.support.ServletUriComponentsBuilder.fromCurrentRequest;

@RestController
@RequestMapping(path = "/v1/reservations")
@RequiredArgsConstructor
final class ReservationController {

    private final RoomReservationUseCase roomReservationUseCase;
    private final Reservations reservations;

    @PostMapping(consumes = APPLICATION_JSON_VALUE, produces = APPLICATION_JSON_VALUE)
    ResponseEntity<?> recordReservation(@RequestBody RoomReservationCmd cmd) {
        var reservation = roomReservationUseCase.make(cmd);
        return ResponseEntity.accepted()
                .location(fromCurrentRequest().path("/{id}").build(reservation.id().toString()))
                .header(HttpHeaders.RETRY_AFTER, "0.5") // seconds
                .build();
    }

    @GetMapping(path = "/{reservationId}", produces = APPLICATION_JSON_VALUE)
    ResponseEntity<?> status(@PathVariable UUID reservationId) {
        var reservation = reservations.findById(new Reservation.ReservationIdentifier(reservationId)); // FIXME: handle not found

        if (reservation.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ApiError("Reservation not found"));
        }

        return ResponseEntity.ok()
                .body(ReservationResource.builder()
                        .reservationId(reservation.get().id().id())
                        .hotelId(reservation.get().hotelId())
                        .roomId(reservation.get().roomId())
                        .guestId(reservation.get().guestId())
                        .status(reservation.get().status())
                        .build());
    }

    @Builder
    record ReservationResource(UUID reservationId,
                               Long hotelId,
                               Long roomId,
                               Long guestId,
                               Reservation.Status status) {}

    record ApiError(String message){}
}
