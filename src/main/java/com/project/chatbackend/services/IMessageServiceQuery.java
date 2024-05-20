package com.project.chatbackend.services;
import com.project.chatbackend.responses.MessageResponse;
import org.springframework.data.domain.Pageable;

import java.util.Date;


public interface IMessageServiceQuery {
    MessageResponse findByContentContaining(String roomId, String content,
                                                  Date startDate, Date endDate, String senderId, Pageable pageable);
}
