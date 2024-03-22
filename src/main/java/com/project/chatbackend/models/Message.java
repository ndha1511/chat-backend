package com.project.chatbackend.models;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.time.LocalDateTime;
import java.util.List;

@Document(collection = "messages")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Message {
    @Id
    private String id;
    @Field(name = "room_id")
    private String roomId;
    @Field(name = "sender_id")
    private String senderId;
    @Field(name = "receiver_id")
    private String receiverId;
    private Object content;
    @Field(name = "send_date")
    private LocalDateTime sendDate;
    @Field(name = "seen_date")
    private LocalDateTime seenDate;
    @Field(name = "message_type")
    private MessageType messageType;
    private List<EmojiMessage> emojis;
    @Field(name = "number_of_emojis")
    private int numberOfEmojis;
    @Field(name = "messages_reply_id")
    private List<String> messagesReplyId;
    @Field(name = "message_status")
    private MessageStatus messageStatus;
}
