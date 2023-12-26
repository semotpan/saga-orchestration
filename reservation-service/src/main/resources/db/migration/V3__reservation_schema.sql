CREATE TABLE IF NOT EXISTS reservation
(
    id             UUID PRIMARY KEY     DEFAULT gen_random_uuid(),
    timestamp      TIMESTAMP   NOT NULL DEFAULT CURRENT_TIMESTAMP,
    hotel_id       BIGINT      NOT NULL,
    room_id        INT         NOT NULL,
    start_date     DATE        NOT NULL,
    end_date       DATE        NOT NULL,
    status         VARCHAR(20) NOT NULL,
    guest_id       BIGINT      NOT NULL,
    payment_due    BIGINT      NOT NULL,
    credit_card_no VARCHAR(16) NOT NULL
);
