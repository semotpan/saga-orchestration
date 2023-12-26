package io.example.reservation.messaging;

import io.example.reservation.framework.SagaStepStatus;

public enum PaymentStatus {
    REQUESTED, CANCELLED, FAILED, COMPLETED;

    public SagaStepStatus toSagaStepStatus() {
        return switch (this) {
            case CANCELLED -> SagaStepStatus.COMPENSATED;
            case COMPLETED, REQUESTED -> SagaStepStatus.SUCCEEDED;
            case FAILED -> SagaStepStatus.FAILED;
        };
    }
}
