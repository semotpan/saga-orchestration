package io.example.reservation.messaging;


import io.example.reservation.framework.SagaStepStatus;

public enum RoomBookingStatus {
    BOOKED, REJECTED, CANCELLED;

    public SagaStepStatus toSagaStepStatus() {
        return switch (this) {
            case BOOKED -> SagaStepStatus.SUCCEEDED;
            case REJECTED -> SagaStepStatus.FAILED;
            case CANCELLED -> SagaStepStatus.COMPENSATED;
        };
    }
}
