package io.example.reservation.messaging;

import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.boot.autoconfigure.kafka.KafkaProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.support.serializer.JsonDeserializer;

@Configuration
class KafkaConfig {

    @Bean
    ConcurrentKafkaListenerContainerFactory<String, RoomBookingEvent> roomBookingKLCFactory(KafkaProperties props) {
        ConcurrentKafkaListenerContainerFactory<String, RoomBookingEvent> factory = new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(roomBookingConsumerFactory(props));
        factory.setConcurrency(props.getListener().getConcurrency());
        return factory;
    }

    @Bean
    ConsumerFactory<String, RoomBookingEvent> roomBookingConsumerFactory(KafkaProperties props) {
        return new DefaultKafkaConsumerFactory<>(
                props.buildConsumerProperties(null),
                new StringDeserializer(),
                new JsonDeserializer<>(RoomBookingEvent.class)
        );
    }

    @Bean
    ConcurrentKafkaListenerContainerFactory<String, PaymentEvent> paymentKLCFactory(KafkaProperties props) {
        ConcurrentKafkaListenerContainerFactory<String, PaymentEvent> factory = new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(paymentConsumerFactory(props));
        factory.setConcurrency(props.getListener().getConcurrency());
        return factory;
    }

    @Bean
    ConsumerFactory<String, PaymentEvent> paymentConsumerFactory(KafkaProperties props) {
        return new DefaultKafkaConsumerFactory<>(
                props.buildConsumerProperties(null),
                new StringDeserializer(),
                new JsonDeserializer<>(PaymentEvent.class)
        );
    }
}
