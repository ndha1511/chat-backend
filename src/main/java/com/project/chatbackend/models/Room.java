package com.project.chatbackend.models;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "rooms")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Room {
    @Id
    private String id;
    private String roomId;
    private String senderId;
    private String receiverId;
    private RoomType roomType;
    private String latestMessage;
    private MessageStatus messageStatus;
    private int numberOfUnreadMessage;
    private String avatarReceiver;
}
