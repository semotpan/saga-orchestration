package io.example.reservation.framework;

import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;

import java.util.UUID;

@RequiredArgsConstructor
public abstract class Saga {

    private final EntityManager entityManager;

    protected void ensureProcessed(UUID eventId, Runnable callback) {
        if (entityManager.find(EventLog.class, eventId) != null) {
            return;
        }

        callback.run();

        entityManager.persist(new EventLog(eventId));
    }

    public enum PayloadType {
        REQUEST, CANCEL
    }
}
