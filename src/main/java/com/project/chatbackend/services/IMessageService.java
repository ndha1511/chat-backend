package com.project.chatbackend.services;

import com.project.chatbackend.exceptions.DataNotFoundException;
import com.project.chatbackend.models.Message;
import com.project.chatbackend.requests.ChatRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.scheduling.annotation.Async;

public interface IMessageService {
    @Async
    void saveMessage(ChatRequest chatRequest, Message messageTmp) throws DataNotFoundException;
    Page<Message> getAllByRoomId(String roomId, PageRequest pageRequest);

    void updateMessage(String id, ChatRequest chatRequest);
    Message saveMessage(ChatRequest chatRequest) throws DataNotFoundException;
}
