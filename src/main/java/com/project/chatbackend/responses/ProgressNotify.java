package com.project.chatbackend.responses;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ProgressNotify {
    private String id;
    private Long bytesTransferred;
}
