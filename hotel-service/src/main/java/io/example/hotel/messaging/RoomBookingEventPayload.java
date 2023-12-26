package io.example.hotel.messaging;

import java.time.LocalDate;

public record RoomBookingEventPayload(Integer hotelId,
                                      Integer roomId,
                                      LocalDate startDate,
                                      LocalDate endDate,
                                      RoomBookingRequestType type) {

    public boolean isRequestType() {
        return type.isRequestType();
    }
}
