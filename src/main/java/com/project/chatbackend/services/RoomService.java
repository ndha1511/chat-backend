package com.project.chatbackend.services;

import com.project.chatbackend.models.Room;
import com.project.chatbackend.models.RoomType;
import com.project.chatbackend.repositories.RoomRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class RoomService implements IRoomService{
    private final RoomRepository roomRepository;

    @Override
    public Optional<String> getRoomId(String senderId, String receiverId) {
        Optional<Room> room = roomRepository.findBySenderIdAndReceiverId(senderId, receiverId);
        return room.map(Room::getRoomId).or(() -> Optional.of(createRoomId(senderId, receiverId)));

    }

    @Override
    public void createRoomForGroup(String userId, String groupId) {
        Room room = Room.builder()
                .roomId(groupId)
                .senderId(userId)
                .receiverId(groupId)
                .roomType(RoomType.GROUP_CHAT)
                .build();
        roomRepository.save(room);
    }

    @Override
    public List<Room> findAllBySenderId(String senderId) {
        return roomRepository.findAllBySenderId(senderId);
    }

    private String createRoomId(String senderId, String receiverId) {
        var roomId = String.format("%s_%s", senderId, receiverId);
        Room senderRoom = Room.builder()
                .roomType(RoomType.USER_CHAT)
                .senderId(senderId)
                .receiverId(receiverId)
                .roomId(roomId)
                .build();
        Room receiverRoom = Room.builder()
                .roomType(RoomType.USER_CHAT)
                .senderId(receiverId)
                .receiverId(senderId)
                .roomId(roomId)
                .build();
        roomRepository.save(senderRoom);
        roomRepository.save(receiverRoom);
        return roomId;

    }
}
