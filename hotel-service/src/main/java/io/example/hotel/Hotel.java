package io.example.hotel;

import jakarta.persistence.*;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.io.Serializable;
import java.time.Instant;

import static lombok.AccessLevel.PACKAGE;
import static lombok.AccessLevel.PRIVATE;

@Entity
@Table(name = "hotel")
@NoArgsConstructor(access = PACKAGE, force = true) // JPA compliant
@ToString
public class Hotel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private HotelIdentifier id;

    private final Instant creationTime;

    private String name;
    private String address;
    private String location;

    public Hotel(HotelIdentifier id, String name, String address, String location) {
        this.id = id;
        this.name = name;
        this.address = address;
        this.location = location;
        this.creationTime = Instant.now();
    }

    @Embeddable
    public record HotelIdentifier(Integer id) implements Serializable {

        public String toString() {
            return id.toString();
        }
    }
}
