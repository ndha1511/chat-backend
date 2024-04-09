package com.project.chatbackend.repositories;

import com.project.chatbackend.models.Message;
import com.project.chatbackend.models.MessageStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface MessageRepository extends MongoRepository<Message, String> {
    Page<Message> getAllByRoomId(String roomId, PageRequest pageRequest);
    List<Message> getAllByRoomIdAndMessageStatus(String roomId, MessageStatus messageStatus);
    Message findTopByOrderBySendDateDesc();
}
