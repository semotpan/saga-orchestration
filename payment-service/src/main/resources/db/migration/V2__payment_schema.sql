CREATE TABLE IF NOT EXISTS payment
(
    reservation_id UUID PRIMARY KEY,
    timestamp      TIMESTAMP   NOT NULL DEFAULT CURRENT_TIMESTAMP,
    guest_id       INT         NOT NULL,
    payment_due    BIGINT      NOT NULL,
    credit_card_no VARCHAR(16) NOT NULL,
    type           VARCHAR(20) NOT NULL
);
