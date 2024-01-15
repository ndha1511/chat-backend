package com.project.zalobackend.services;

import com.project.zalobackend.models.Message;
import com.project.zalobackend.repositories.MessageRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class MessageService implements IMessageService {
    private final MessageRepository messageRepository;
    @Override
    public Message createMessage(Message message) {
        return messageRepository.save(message);
    }
}
