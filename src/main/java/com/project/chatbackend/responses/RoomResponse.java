package com.project.chatbackend.responses;

import com.project.chatbackend.models.Group;
import com.project.chatbackend.models.Room;
import com.project.chatbackend.models.RoomType;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class RoomResponse {
    private String objectId;
    private String roomId;
    private int numberOfUnreadMessage;
    private String name;
    private String receiverId;
    private RoomType roomType;
    private boolean sender;
    private String senderId;
    private LocalDateTime time;
    private String latestMessage;
    private String avatar;
}
