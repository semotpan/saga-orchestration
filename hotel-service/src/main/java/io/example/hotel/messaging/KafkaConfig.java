package io.example.hotel.messaging;

import lombok.Setter;
import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.boot.autoconfigure.kafka.KafkaProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.support.serializer.JsonDeserializer;

@Configuration
@EnableKafka
class KafkaConfig {

    @Bean
    @ConfigurationProperties(prefix = "kafka.topic.roombooking.inbox.events")
    KafkaTopic roomBookingInboxTopicProps() {
        return new KafkaTopic();
    }

    @Bean
    @ConfigurationProperties(prefix = "kafka.topic.roombooking.outbox.events")
    KafkaTopic roomBookingOutboxTopicProps() {
        return new KafkaTopic();
    }

    /**
     * room-booking inbox topic creation
     */
    @Bean
    NewTopic roomBookingInboxTopic() {
        var props = roomBookingInboxTopicProps();
        return new NewTopic(props.name, props.partitions, props.replicas);
    }

    /**
     * room-booking outbox topic creation
     */
    @Bean
    NewTopic roomBookingOutboxTopic() {
        var props = roomBookingOutboxTopicProps();
        return new NewTopic(props.name, props.partitions, props.replicas);
    }

    @Bean
    ConcurrentKafkaListenerContainerFactory<String, RoomBookingEventPayload> roomBookingKLCFactory(KafkaProperties props) {
        var factory = new ConcurrentKafkaListenerContainerFactory<String, RoomBookingEventPayload>();
        factory.setConsumerFactory(consumerFactory(props));
        factory.setConcurrency(props.getListener().getConcurrency());
        return factory;
    }

    @Bean
    ConsumerFactory<String, RoomBookingEventPayload> consumerFactory(KafkaProperties props) {
        return new DefaultKafkaConsumerFactory<>(
                props.buildConsumerProperties(null),
                new StringDeserializer(),
                new JsonDeserializer<>(RoomBookingEventPayload.class)
        );
    }

    @Setter
    private static class KafkaTopic {
        private String name;
        private int partitions;
        private short replicas;
    }
}
