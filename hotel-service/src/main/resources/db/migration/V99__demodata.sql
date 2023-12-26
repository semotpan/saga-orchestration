-- DEMO data
INSERT INTO hotel(id, name, address, location)
VALUES (1, 'Bristol Central Park Hotel', 'str. Puskin 32, 2012', 'Chişinău');

INSERT INTO room(id, name, number, floor, hotel_id, available)
VALUES (1, 'Twin with view', 38, 5, 1, true),
       (2, 'Deluxe', 25, 3, 1, false),
       (3, 'Twin Deluxe', 27, 2, 1, true);
