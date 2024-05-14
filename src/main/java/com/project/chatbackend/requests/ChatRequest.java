package com.project.chatbackend.requests;

import com.project.chatbackend.models.MessageStatus;
import com.project.chatbackend.models.MessageType;
import lombok.Builder;
import lombok.Data;
import org.springframework.web.multipart.MultipartFile;


@Data
@Builder
public class ChatRequest {
    private String senderId;
    private String receiverId;
    private String senderName;
    private String senderAvatar;
    private String textContent;
    private MessageType messageType;
    private MessageStatus messageStatus;
    private MultipartFile fileContent;
    private boolean hiddenSenderSide;

}
