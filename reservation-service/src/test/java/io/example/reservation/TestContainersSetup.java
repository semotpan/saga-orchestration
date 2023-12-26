package io.example.reservation;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.debezium.testing.testcontainers.DebeziumContainer;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.rnorth.ducttape.unreliables.Unreliables;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.kafka.support.serializer.JsonDeserializer;
import org.springframework.kafka.support.serializer.JsonSerializer;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.KafkaContainer;
import org.testcontainers.containers.Network;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.lifecycle.Startables;
import org.testcontainers.utility.DockerImageName;

import java.io.IOException;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;

import static org.springframework.http.MediaType.APPLICATION_JSON;

@DirtiesContext
@Testcontainers
public abstract class TestContainersSetup {

    private static final Logger LOGGER = LoggerFactory.getLogger(TestContainersSetup.class);

    private static final Network network = Network.newNetwork();

    @Container
    protected static final KafkaContainer kafka = new KafkaContainer(DockerImageName.parse("confluentinc/cp-kafka:7.4.1"))
            .withNetwork(network)
            .withNetworkAliases("kafka");

    @Container
    protected static PostgreSQLContainer<?> postgresSQL = new PostgreSQLContainer<>(DockerImageName.parse("debezium/postgres:16")
            .asCompatibleSubstituteFor("postgres"))
            .withNetwork(network)
            .withNetworkAliases("postgres");

    @Container
    protected static DebeziumContainer connector = new DebeziumContainer("debezium/connect:2.4.1.Final")
            .withNetwork(network)
            .withKafka(kafka)
//            .withLogConsumer(new Slf4jLogConsumer(LOGGER))
            .dependsOn(kafka);

    @DynamicPropertySource
    static void configure(DynamicPropertyRegistry registry) {
        Startables.deepStart(Stream.of(postgresSQL, kafka, connector))
                .join();

        registry.add("spring.kafka.bootstrap-servers", kafka::getBootstrapServers);
        registry.add("spring.datasource.url", postgresSQL::getJdbcUrl);
        registry.add("spring.datasource.username", postgresSQL::getUsername);
        registry.add("spring.datasource.password", postgresSQL::getUsername);
    }

    protected KafkaConsumer<String, JsonNode> kafkaConsumer() {
        return new KafkaConsumer<>(Map.of(
                ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, kafka.getBootstrapServers(),
                ConsumerConfig.GROUP_ID_CONFIG, "tc-" + UUID.randomUUID(),
                ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest"
        ), new StringDeserializer(), new JsonDeserializer<>(JsonNode.class));
    }

    protected List<ConsumerRecord<String, JsonNode>> drain(KafkaConsumer<String, JsonNode> consumer, int expectedRecordCount) {
        var allRecords = new ArrayList<ConsumerRecord<String, JsonNode>>();

        Unreliables.retryUntilTrue(20, TimeUnit.SECONDS, () -> {
            consumer.poll(Duration.ofMillis(50))
                    .iterator()
                    .forEachRemaining(allRecords::add);

            return allRecords.size() == expectedRecordCount;
        });

        return allRecords;
    }

    protected <T> KafkaProducer<String, T> kafkaProducer() {
        return new KafkaProducer<>(Map.of(
                ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, kafka.getBootstrapServers()
        ), new StringSerializer(), new JsonSerializer<>());
    }

    protected void registerTestConnector(TestRestTemplate restTemplate) throws IOException {
        var objectMapper = new ObjectMapper();
        var payload = objectMapper
                .readValue(new ClassPathResource("test-outbox-connector.json").getFile(), JsonNode.class);

        var headers = new HttpHeaders();
        headers.setContentType(APPLICATION_JSON);

        restTemplate.exchange(
                connector.getConnectorsUri(),
                HttpMethod.POST,
                new HttpEntity<>(objectMapper.writeValueAsString(payload), headers),
                String.class
        );
    }
}
