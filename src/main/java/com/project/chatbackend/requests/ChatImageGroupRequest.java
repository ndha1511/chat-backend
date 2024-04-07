package com.project.chatbackend.requests;

import com.project.chatbackend.models.MessageStatus;
import com.project.chatbackend.models.MessageType;
import lombok.Builder;
import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Data
@Builder
public class ChatImageGroupRequest {
    private String senderId;
    private String receiverId;
    private MessageType messageType;
    private MessageStatus messageStatus;
    private List<MultipartFile> filesContent;
    private boolean hiddenSenderSide;
}
