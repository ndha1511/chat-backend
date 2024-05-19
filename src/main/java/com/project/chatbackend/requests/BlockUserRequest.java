package com.project.chatbackend.requests;

import lombok.Data;

@Data
public class BlockUserRequest {
    private String senderId;
    private String blockId;
}
