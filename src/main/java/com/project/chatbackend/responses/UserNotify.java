package com.project.chatbackend.responses;

import com.project.chatbackend.models.Message;
import com.project.chatbackend.models.Room;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class UserNotify {
    private String receiverId;
    private String senderId;
    private String status;
    private Message message;
    private Room room;
}
