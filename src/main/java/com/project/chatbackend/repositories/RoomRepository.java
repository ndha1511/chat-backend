package com.project.chatbackend.repositories;

import com.project.chatbackend.models.Room;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;
import java.util.Optional;

public interface RoomRepository extends MongoRepository<Room, String> {
    List<Room> findAllBySenderId(String senderId);
    Optional<Room> findBySenderIdAndReceiverId(String senderId, String receiverId);
    List<Room> findByRoomId(String roomId);
}
