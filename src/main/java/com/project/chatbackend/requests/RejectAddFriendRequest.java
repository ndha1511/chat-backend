package com.project.chatbackend.requests;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class RejectAddFriendRequest {
    private String senderId;
    private String receiverId;

}
