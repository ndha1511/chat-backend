package com.project.chatbackend.responses;

import com.project.chatbackend.models.User;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AcceptAddFriendRespone {
    private String message;
    private User user;
}
