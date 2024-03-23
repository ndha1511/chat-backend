package com.project.chatbackend.repositories;

import com.project.chatbackend.models.Room;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;
import java.util.Optional;

public interface RoomRepository extends MongoRepository<Room, String> {
    Page<Room> findAllBySenderId(String senderId, PageRequest pageRequest);
    Optional<Room> findBySenderIdAndReceiverId(String senderId, String receiverId);
    List<Room> findByRoomId(String roomId);
}
