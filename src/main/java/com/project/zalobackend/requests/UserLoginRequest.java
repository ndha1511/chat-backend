package com.project.zalobackend.requests;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class UserLoginRequest {
    private String phoneNumber;
    private String password;
}
