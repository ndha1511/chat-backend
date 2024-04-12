package com.project.chatbackend.requests;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class RevokeMessageRequest {
    private String messageId;
    private String senderId;
    private String receiverId;
}
