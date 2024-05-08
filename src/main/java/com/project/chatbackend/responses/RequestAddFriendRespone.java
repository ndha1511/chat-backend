package com.project.chatbackend.responses;

import com.project.chatbackend.models.Message;
import com.project.chatbackend.models.User;
import lombok.Builder;
import lombok.Data;

@Builder
@Data
public class RequestAddFriendRespone {
    private User user;
    private String message;
}
