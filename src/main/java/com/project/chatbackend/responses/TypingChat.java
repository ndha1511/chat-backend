package com.project.chatbackend.responses;

import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class TypingChat {
    private String status;
    private String senderId;
    private String receiverId;
    private String senderName;
    private String roomId;
    private String senderAvatar;
}
