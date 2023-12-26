package io.example.hotel;

import jakarta.persistence.*;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.io.Serializable;
import java.time.Instant;

import static lombok.AccessLevel.PRIVATE;

@Entity
@Table(name = "room")
@NoArgsConstructor(access = PRIVATE, force = true) // JPA compliant
@ToString
public class Room {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private RoomIdentifier id;

    private final Instant creationTime;

    private String name;
    private Integer number;
    private Integer floor;
    private Boolean available;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "hotel_id")
    private Hotel hotel;

    public Room(RoomIdentifier id, String name, Integer number, Integer floor, Hotel hotel) {
        this.id = id; // TODO exposed only for demo purpose
        this.name = name;
        this.number = number;
        this.floor = floor;
        this.available = true;
        this.creationTime = Instant.now();
        this.hotel = hotel;
    }

    public boolean isBooked() {
        return !available;
    }

    public void book() {
        this.available = false;
    }

    public void release() {
        this.available = true;
    }

    @Embeddable
    public record RoomIdentifier(Integer id) implements Serializable {

        public String toString() {
            return id.toString();
        }
    }
}
