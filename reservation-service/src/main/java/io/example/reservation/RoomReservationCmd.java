package io.example.reservation;


import lombok.Builder;

import java.time.LocalDate;

@Builder // FIXME: apply validations
public record RoomReservationCmd(Long hotelId,
                                 Long roomId,
                                 LocalDate startDate,
                                 LocalDate endDate,
                                 Long guestId,
                                 Long paymentDue,
                                 String creditCardNo) {

    Reservation toReservation() {
        return Reservation.builder()
                .hotelId(hotelId)
                .roomId(roomId)
                .startDate(startDate)
                .endDate(endDate)
                .guestId(guestId)
                .paymentDue(paymentDue)
                .creditCardNo(creditCardNo)
                .build();
    }
}