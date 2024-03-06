package com.project.chatbackend.controllers;

import com.project.chatbackend.exceptions.DataNotFoundException;
import com.project.chatbackend.models.Message;
import com.project.chatbackend.services.IMessageService;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class ChatController {
    private final IMessageService messageService;
    private final SimpMessagingTemplate simpMessagingTemplate;

    @MessageMapping("/chat")
    public void processMessage(@Payload Message message) {
        try {
            Message msg = messageService.saveMessage(message);
            simpMessagingTemplate.convertAndSendToUser(
                message.getReceiverId(), "/queue/messages",
                    msg
            );
        } catch (DataNotFoundException e) {
            throw new RuntimeException(e);
        }
    }
}
