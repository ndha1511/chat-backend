package com.project.chatbackend.services;

import com.project.chatbackend.exceptions.DataNotFoundException;
import com.project.chatbackend.models.*;
import com.project.chatbackend.repositories.GroupRepository;
import com.project.chatbackend.repositories.MessageRepository;
import com.project.chatbackend.repositories.RoomRepository;
import com.project.chatbackend.repositories.UserRepository;
import com.project.chatbackend.responses.RoomResponse;
import com.project.chatbackend.responses.UserLoginResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;


@Service
@RequiredArgsConstructor
public class RoomService implements IRoomService{
    private final RoomRepository roomRepository;
    private final UserRepository userRepository;
    private final GroupRepository groupRepository;
    private final MessageRepository messageRepository;
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
    public Page<Room> findAllBySenderId(String senderId, PageRequest pageRequest) {
        return roomRepository.findAllBySenderId(senderId, pageRequest);
    }

    @Override
    public RoomResponse mapRoomToRoomResponse(Room room) throws DataNotFoundException {
        UserLoginResponse userLoginResponse = null;
        Group group = null;
        if (room.getRoomType() == RoomType.SINGLE_CHAT) {
            userLoginResponse = userRepository
                    .findById(room.getReceiverId())
                    .map(user -> UserLoginResponse.builder()
                            .email(user.getEmail())
                            .name(user.getName())
                            .avatar(user.getAvatar())
                            .gender(user.isGender())
                            .phoneNumber(user.getPhoneNumber())
                            .images(user.getImages())
                            .coverImage(user.getCoverImage())
                            .build())
                    .orElseThrow(() -> new DataNotFoundException("user not found"));
            return RoomResponse.builder()
                    .objectId(room.getId())
                    .roomId(room.getRoomId())
                    .receiverId(userLoginResponse.getEmail())
                    .senderId(room.getSenderId())
                    .time(room.getTime())
                    .latestMessage(room.getLatestMessage())
                    .numberOfUnreadMessage(room.getNumberOfUnreadMessage())
                    .avatar(userLoginResponse.getAvatar())
                    .sender(room.isSender())
                    .roomType(room.getRoomType())
                    .name(userLoginResponse.getName())
                    .build();
        } else {
            group = groupRepository.findById(room.getReceiverId())
                    .orElseThrow(() -> new DataNotFoundException("group not found"));
            return RoomResponse.builder()
                    .objectId(room.getId())
                    .roomId(room.getRoomId())
                    .receiverId(group.getId())
                    .senderId(room.getSenderId())
                    .time(room.getTime())
                    .latestMessage(room.getLatestMessage())
                    .numberOfUnreadMessage(room.getNumberOfUnreadMessage())
                    .avatar(group.getAvatar())
                    .sender(room.isSender())
                    .roomType(room.getRoomType())
                    .name(group.getGroupName())
                    .build();
        }

    }

    @Override
    public void updateStatusRoom(String id) {
        Room room = roomRepository.findById(id).orElseThrow();
        List<Message> messages = messageRepository.
                getAllByRoomIdAndMessageStatus(room.getRoomId(), MessageStatus.RECEIVED);
        List<Message> messages1 = messageRepository
                .getAllByRoomIdAndMessageStatus(room.getRoomId(), MessageStatus.SEEN);
        for(Message msg : messages) {
            msg.setMessageStatus(MessageStatus.SEEN);
            messageRepository.save(msg);
        }
        for (Message msg: messages1) {
            msg.setMessageStatus(MessageStatus.SEEN);
            messageRepository.save(msg);
        }
        room.setNumberOfUnreadMessage(0);
        roomRepository.save(room);

    }

    @Override
    public RoomResponse findBySenderIdAndReceiverId(String senderId, String receiverId) throws Exception {
        return roomRepository.findBySenderIdAndReceiverId(senderId, receiverId)
                .map(room -> {
                    try {
                        return mapRoomToRoomResponse(room);
                    } catch (DataNotFoundException e) {
                        throw new RuntimeException(e);
                    }
                })
                .orElseThrow(() -> new DataNotFoundException("room not found"));
    }

    @Override
    public List<Room> findByRoomId(String roomId) {
        return roomRepository.findByRoomId(roomId);
    }

    private String createRoomId(String senderId, String receiverId) {
        var roomId = String.format("%s_%s", senderId, receiverId);
        Room senderRoom = Room.builder()
                .roomType(RoomType.SINGLE_CHAT)
                .senderId(senderId)
                .receiverId(receiverId)
                .roomId(roomId)
                .build();
        Room receiverRoom = Room.builder()
                .roomType(RoomType.SINGLE_CHAT)
                .senderId(receiverId)
                .receiverId(senderId)
                .roomId(roomId)
                .build();
        roomRepository.save(senderRoom);
        roomRepository.save(receiverRoom);
        return roomId;

    }

    public void saveRoom(Room room) {
        roomRepository.save(room);
    }
}
