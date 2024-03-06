package com.project.chatbackend.services;

import java.util.Optional;

public interface IRoomService {
    Optional<String> getRoomId(String senderId, String receiverId);
    void createRoomForGroup(String userId, String groupId);
}
