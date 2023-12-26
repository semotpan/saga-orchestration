package io.example.reservation.web;

import io.example.reservation.MessagingCDCConfig;
import io.example.reservation.Reservation;
import io.example.reservation.TestContainersSetup;
import io.example.reservation.messaging.PaymentEvent;
import io.example.reservation.messaging.PaymentStatus;
import io.example.reservation.messaging.RoomBookingEvent;
import io.example.reservation.messaging.RoomBookingStatus;
import io.example.reservation.web.ReservationController.StatusResource;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.header.Headers;
import org.apache.kafka.common.header.internals.RecordHeader;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;

import java.io.IOException;
import java.util.List;

import static java.util.UUID.randomUUID;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;
import static org.springframework.http.MediaType.APPLICATION_JSON;

@Tag("system")
@SpringBootTest(webEnvironment = RANDOM_PORT)
@Import(MessagingCDCConfig.class)
public class BookRoomE2ETest extends TestContainersSetup {

    @BeforeEach
    void setUp(@Autowired TestRestTemplate restTemplate) throws IOException {
        registerTestConnector(restTemplate); // FIXME: think of a better way
    }

    @Test
    @DisplayName("Place a hotel room reservation")
    void makeAReservation(@Autowired TestRestTemplate restTemplate) {
        var headers = new HttpHeaders();
        headers.setContentType(APPLICATION_JSON);
        // 1. start the process
        var resp = restTemplate.postForEntity("/v1/reservations", new HttpEntity<>(requestJSON(), headers), String.class);

        // 2. ensure mocking of hotel service
        String sagaId = null; // message key for saga
        try (var consumer = kafkaConsumer()) {
            consumer.subscribe(List.of("room-booking.inbox.events"));
            var changeEvent = drain(consumer, 1).get(0);

            sagaId = changeEvent.key();
            consumer.unsubscribe();
        }

        try (KafkaProducer<String, RoomBookingEvent> roomBookingProducer = kafkaProducer()) {
            var record = new ProducerRecord<>("room-booking.outbox.events", sagaId, new RoomBookingEvent(RoomBookingStatus.BOOKED));
            enrichHeaders(record.headers(), "RoomUpdated", "room-booking");

            roomBookingProducer.send(record);
            roomBookingProducer.flush();
        }

        // 3. ensure mocking of payment service
        try (var consumer = kafkaConsumer()) {
            consumer.subscribe(List.of("payment.inbox.events"));
            drain(consumer, 1);
            consumer.unsubscribe();
        }

        try (KafkaProducer<String, PaymentEvent> roomBookingProducer = kafkaProducer()) {
            var record = new ProducerRecord<>("payment.outbox.events", sagaId, new PaymentEvent(PaymentStatus.REQUESTED));
            enrichHeaders(record.headers(), "PaymentUpdated", "payment");

            roomBookingProducer.send(record);
            roomBookingProducer.flush();
        }

        //4. check the reservation
        var reservationStatus = restTemplate.getForEntity(resp.getHeaders().getLocation(), StatusResource.class);
        assertThat(reservationStatus.getBody())
                .isEqualTo(new StatusResource(Reservation.Status.SUCCEED));
    }

    private void enrichHeaders(Headers record, String eventType, String aggregateType) {
        record.add(new RecordHeader("id", randomUUID().toString().getBytes()))
                .add(new RecordHeader("eventType", eventType.getBytes()))
                .add(new RecordHeader("aggregateType", aggregateType.getBytes()));
    }

    @NotNull
    private String requestJSON() {
        return """
                {
                  "hotelId": 1,
                  "roomId": 1,
                  "startDate": "2023-12-16",
                  "endDate": "2023-12-17",
                  "guestId": 10000001,
                  "paymentDue": 1702632793441,
                  "creditCardNo": "************7999"
                }
                """;
    }
}
