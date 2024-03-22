package com.project.chatbackend.services;

import com.project.chatbackend.exceptions.DataNotFoundException;
import com.project.chatbackend.models.Message;
import com.project.chatbackend.models.Room;
import com.project.chatbackend.repositories.MessageRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class MessageService implements IMessageService {
    private final MessageRepository messageRepository;
    private final RoomService roomService;
    @Override
    public Message saveMessage(Message message) throws DataNotFoundException {
        message.setSendDate(LocalDateTime.now());
        var roomId = roomService.getRoomId(message.getSenderId(), message.getReceiverId());
        String roomIdConvert = roomId.orElseThrow(() -> new DataNotFoundException("room not found"));
        List<Room> rooms = roomService.findByRoomId(roomIdConvert);
        for(Room room : rooms) {
            if(Objects.equals(room.getSenderId(), message.getSenderId())) {
                room.setLatestMessage(message.getContent().toString());
                room.setTime(LocalDateTime.now());
                room.setSender(true);
                roomService.saveRoom(room);
            } else {
                room.setLatestMessage(message.getContent().toString());
                room.setNumberOfUnreadMessage(room.getNumberOfUnreadMessage() + 1);
                room.setTime(LocalDateTime.now());
                room.setSender(false);
                roomService.saveRoom(room);
            }
        }
        message.setRoomId(roomIdConvert);
        return messageRepository.save(message);
    }

    @Override
    public Page<Message> getAllByRoomId(String roomId, PageRequest pageRequest) {
        return messageRepository.getAllByRoomId(roomId, pageRequest);
    }
}
