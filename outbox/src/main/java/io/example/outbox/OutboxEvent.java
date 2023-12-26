package io.example.outbox;

import java.time.Instant;

/**
 * Use {@link org.springframework.context.ApplicationEventPublisher} to send the event
 *
 * @param <ID>
 * @param <P>
 */
public interface OutboxEvent<ID, P> {

    ID aggregateId();

    String aggregateType();

    String type();

    Instant timestamp();

    P payload();
}
