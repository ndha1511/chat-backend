package com.project.chatbackend.requests;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class UserLoginRequest {
    private String email;
    private String password;
    private boolean isMobile;
}
