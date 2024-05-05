package com.project.chatbackend.requests;

import com.project.chatbackend.models.Message;
import com.project.chatbackend.models.MessageType;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CallRequest {
    private String senderId;
    private String receiverId;
    private MessageType messageType;
}
