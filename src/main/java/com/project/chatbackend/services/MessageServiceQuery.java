package com.project.chatbackend.services;

import com.project.chatbackend.models.Message;
import com.project.chatbackend.repositories.MessageRepositoryQuery;
import com.project.chatbackend.responses.MessageResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.Date;
@Service
@RequiredArgsConstructor
public class MessageServiceQuery implements IMessageServiceQuery {

    private final MessageRepositoryQuery messageRepositoryQuery;
    @Override
    public MessageResponse findByContentContaining(String roomId,
                                                         String content,
                                                         Date startDate,
                                                         Date endDate,
                                                         String senderId, Pageable pageable) {
        Page<Message> messages = messageRepositoryQuery.findByContentContaining(roomId,
                content, startDate, endDate, senderId, pageable);
        return MessageResponse.builder()
                .messages(messages.getContent())
                .totalPage(messages.getTotalPages())
                .build();
    }
}
