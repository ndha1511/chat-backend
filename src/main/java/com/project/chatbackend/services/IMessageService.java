package com.project.chatbackend.services;

import com.project.chatbackend.exceptions.DataNotFoundException;
import com.project.chatbackend.models.Message;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

public interface IMessageService {
    Message saveMessage(Message message) throws DataNotFoundException;
    Page<Message> getAllByRoomId(String roomId, PageRequest pageRequest);
}
