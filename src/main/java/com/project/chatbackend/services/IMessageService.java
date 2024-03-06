package com.project.chatbackend.services;

import com.project.chatbackend.exceptions.DataNotFoundException;
import com.project.chatbackend.models.Message;

public interface IMessageService {
    Message saveMessage(Message message) throws DataNotFoundException;
}
