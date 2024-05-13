package com.project.chatbackend.requests;

import lombok.Data;

import java.util.Date;

@Data
public class FindMessageRequest {
    private String roomId;
    private String content;
    private Date startDate; // option
    private Date endDate; // option
    private String senderId; // option
    private String currentId; // option
}
