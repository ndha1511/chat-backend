package com.project.chatbackend.services;

import com.project.chatbackend.exceptions.DataNotFoundException;
import com.project.chatbackend.models.Room;
import com.project.chatbackend.responses.RoomResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

import java.util.List;
import java.util.Optional;

public interface IRoomService {
    Optional<String> getRoomId(String senderId, String receiverId);
    Room createRoomForGroup(String userId, String groupId);
    Page<Room> findAllBySenderId(String senderId, PageRequest pageRequest);
    RoomResponse findBySenderIdAndReceiverId(String senderId, String receiverId) throws Exception;
    List<Room> findByRoomId(String roomId);
    RoomResponse mapRoomToRoomResponse(Room room) throws DataNotFoundException;
    void updateStatusRoom(String id);

}
