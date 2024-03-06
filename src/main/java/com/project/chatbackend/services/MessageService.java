package com.project.chatbackend.services;

import com.project.chatbackend.exceptions.DataNotFoundException;
import com.project.chatbackend.models.Message;
import com.project.chatbackend.repositories.MessageRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class MessageService implements IMessageService {
    private final MessageRepository messageRepository;
    private final IRoomService roomService;
    @Override
    public Message saveMessage(Message message) throws DataNotFoundException {
        var roomId = roomService.getRoomId(message.getSenderId(), message.getReceiverId());
        message.setRoomId(roomId.orElseThrow(() -> new DataNotFoundException("room not found")));
        return messageRepository.save(message);
    }
}
