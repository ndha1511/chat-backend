package com.project.chatbackend.models;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString
@Document(collection = "request_add_friend")
public class RequestAddFriend {
    @Id
    private String requestId;
    @Indexed
    @Field(name = "sender_id")
    private String senderId;
    @Field(name = "receiver_id")
    @Indexed
    private String receiverId;
    @Field(name = "message")
    private String message;
}
