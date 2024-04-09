package com.project.chatbackend.requests;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class SeenMessageRequest {
    private String roomId;
    private String senderId;
    private String receiverId;
}
