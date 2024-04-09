package com.project.chatbackend.requests;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class ForwardMessageRequest {
    private String messageId;
    private String senderId;
    private List<String> receiversId;
}
