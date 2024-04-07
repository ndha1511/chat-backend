package com.project.chatbackend.requests;

import lombok.Builder;
import lombok.Data;
import lombok.Getter;

@Data
@Builder
public class UnfriendRequest {
    private String senderId;
    private String receiverId;
}
