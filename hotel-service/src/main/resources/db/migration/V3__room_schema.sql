CREATE TABLE IF NOT EXISTS room
(
    id            SERIAL PRIMARY KEY,
    creation_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    name          VARCHAR(255),
    number        INT       NOT NULL,
    floor         INT       NOT NULL,
    available     BOOL               DEFAULT TRUE,
    hotel_id      INT       NOT NULL,
    FOREIGN KEY (hotel_id) REFERENCES hotel (id) ON UPDATE CASCADE ON DELETE CASCADE
);
