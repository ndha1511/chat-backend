package com.project.chatbackend.models;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;


@Builder
@Data
public class CallInfo {
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private long duration;
    private CallStatus callStatus;
}
