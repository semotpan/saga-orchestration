package io.example.hotel;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface Rooms extends JpaRepository<Room, Room.RoomIdentifier> {
}
