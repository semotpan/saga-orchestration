package io.example.hotel.messaging;

public enum RoomBookingRequestType {
    REQUEST, CANCEL;

    public boolean isRequestType() {
        return this == REQUEST;
    }

}
