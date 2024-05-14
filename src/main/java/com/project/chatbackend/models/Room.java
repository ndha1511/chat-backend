package com.project.chatbackend.models;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.time.LocalDateTime;

@Document(collection = "rooms")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Room {
    @Id
    private String id;
    @Field(name = "room_id")
    @Indexed
    private String roomId;
    @Field(name = "sender_id")
    private String senderId;
    @Field(name = "receiver_id")
    private String receiverId;
    @Field(name = "room_type")
    private RoomType roomType;
    @Field(name = "latest_message")
    private String latestMessage;
    @Field(name = "number_of_unread_message")
    private int numberOfUnreadMessage;
    private LocalDateTime time;
    private boolean sender;
    @Field(name = "created_at")
    private LocalDateTime createdAt;
    @Field(name = "deleted_at")
    private LocalDateTime deletedAt;
    private RoomStatus roomStatus;
}
