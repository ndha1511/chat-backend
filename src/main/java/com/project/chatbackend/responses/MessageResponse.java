package com.project.chatbackend.responses;

import com.project.chatbackend.models.Message;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class MessageResponse {
    private List<Message> messages;
    private int totalPage;
}
