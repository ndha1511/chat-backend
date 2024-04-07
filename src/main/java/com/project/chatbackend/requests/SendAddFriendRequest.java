package com.project.chatbackend.requests;

import lombok.Builder;
import lombok.Data;

@Builder
@Data
public class SendAddFriendRequest {
    private String senderId;
    private String receiverId;
    private String message;
}
