CREATE TABLE IF NOT EXISTS hotel
(
    id            SERIAL PRIMARY KEY,
    creation_time TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    name          VARCHAR(255) NOT NULL,
    address       VARCHAR(255) NOT NULL,
    location      VARCHAR(255) NOT NULL
);


