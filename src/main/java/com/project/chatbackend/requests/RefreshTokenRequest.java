package com.project.chatbackend.requests;

import lombok.Data;

@Data
public class RefreshTokenRequest {
    private String refreshToken;
}
