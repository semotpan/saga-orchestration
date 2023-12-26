package io.example.payment.messaging;

import io.example.payment.Payment;
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
    @ConfigurationProperties(prefix = "kafka.topic.inbox.events")
    KafkaTopic paymentInboxTopicProps() {
        return new KafkaTopic();
    }

    @Bean
    @ConfigurationProperties(prefix = "kafka.topic.outbox.events")
    KafkaTopic paymentOutboxTopicProps() {
        return new KafkaTopic();
    }

    /**
     * payment inbox topic creation
     */
    @Bean
    NewTopic paymentInboxTopic() {
        var props = paymentInboxTopicProps();
        return new NewTopic(props.name, props.partitions, props.replicas);
    }

    /**
     * payment outbox topic creation
     */
    @Bean
    NewTopic paymentOutboxTopic() {
        var props = paymentOutboxTopicProps();
        return new NewTopic(props.name, props.partitions, props.replicas);
    }

    @Bean
    ConcurrentKafkaListenerContainerFactory<String, Payment> kafkaListenerContainerFactory(KafkaProperties props) {
        var factory = new ConcurrentKafkaListenerContainerFactory<String, Payment>();
        factory.setConsumerFactory(consumerFactory(props));
        factory.setConcurrency(props.getListener().getConcurrency());
        return factory;
    }

    @Bean
    ConsumerFactory<String, Payment> consumerFactory(KafkaProperties props) {
        return new DefaultKafkaConsumerFactory<>(
                props.buildConsumerProperties(null),
                new StringDeserializer(),
                new JsonDeserializer<>(Payment.class)
        );
    }

    @Setter
    private static class KafkaTopic {
        private String name;
        private int partitions;
        private short replicas;
    }
}
