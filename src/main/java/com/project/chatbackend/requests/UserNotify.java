package com.project.chatbackend.requests;

import com.project.chatbackend.models.Message;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class UserNotify {
    private String receiverId;
    private String senderId;
    private String status;
    private Message message;
}
