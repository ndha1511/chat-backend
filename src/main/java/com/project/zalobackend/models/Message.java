package com.project.zalobackend.models;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

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
    private String senderId;
    private String receiverId;
    private Object content;
    private LocalDateTime sendDate;
    private LocalDateTime seenDate;
    private MessageType messageType;
    private List<Emoji> emojis;
    private int numberOfEmojis;
}
