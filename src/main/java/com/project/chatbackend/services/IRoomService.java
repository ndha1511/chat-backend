package com.project.chatbackend.services;

import com.project.chatbackend.models.Room;

import java.util.List;
import java.util.Optional;

public interface IRoomService {
    Optional<String> getRoomId(String senderId, String receiverId);
    void createRoomForGroup(String userId, String groupId);
    List<Room> findAllBySenderId(String senderId);
}
