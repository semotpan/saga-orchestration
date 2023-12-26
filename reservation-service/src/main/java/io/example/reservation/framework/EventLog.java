package io.example.reservation.framework;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

import static lombok.AccessLevel.PRIVATE;

/**
 *
 */
@Entity
@Table(name = "eventlog")
@NoArgsConstructor(access = PRIVATE, force = true) // JPA compliant
class EventLog {

    @Id
    private final UUID eventId;

    private final Instant issuedOn;

    EventLog(UUID eventId) {
        this.eventId = eventId;
        this.issuedOn = Instant.now();
    }
}
