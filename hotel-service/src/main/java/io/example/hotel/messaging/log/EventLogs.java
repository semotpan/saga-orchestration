package io.example.hotel.messaging.log;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface EventLogs extends CrudRepository<EventLog, UUID> {

    default void processed(UUID eventId) {
        save(new EventLog(eventId));
    }

    @Query("""
            SELECT COUNT (cm.eventId)=1
            FROM EventLog cm
            WHERE cm.eventId = :eventId
            """)
    boolean alreadyProcessed(UUID eventId);
}
