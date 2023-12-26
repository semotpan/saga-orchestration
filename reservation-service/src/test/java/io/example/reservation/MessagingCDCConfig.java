package io.example.reservation;


import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;

@TestConfiguration
public class MessagingCDCConfig {


    @Bean
    NewTopic roomBookingInboxTopic() {
        return new NewTopic("room-booking.inbox.events", 1, (short) 1);
    }

    @Bean
    NewTopic paymentInboxTopic() {
        return new NewTopic("payment.inbox.events", 1, (short) 1);
    }

    @Bean
    NewTopic roomBookingOutboxTopic() {
        return new NewTopic("room-booking.outbox.events", 1, (short) 1);
    }

    @Bean
    NewTopic paymentOutboxTopic() {
        return new NewTopic("payment.outbox.events", 1, (short) 1);
    }

}
