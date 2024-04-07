package com.project.chatbackend.requests;

import lombok.Builder;
import lombok.Data;

@Builder
@Data
public class AcceptAddFriendRequest {
    private String senderId;
    private String receiverId;
}
