package com.project.chatbackend.repositories;

import com.project.chatbackend.models.Message;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Date;

public interface IMessageRepositoryQuery {
    Page<Message> findByContentContaining(String roomId, String search, Date startDate, Date endDate, String senderId, Pageable pageable);
}
