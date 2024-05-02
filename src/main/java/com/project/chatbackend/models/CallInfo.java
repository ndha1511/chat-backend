package com.project.chatbackend.models;

import lombok.Builder;
import lombok.Data;


@Builder
@Data
public class CallInfo {
    private long duration;
    private CallStatus callStatus;
}
