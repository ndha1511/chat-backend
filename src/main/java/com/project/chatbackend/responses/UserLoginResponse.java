package com.project.chatbackend.responses;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class UserLoginResponse {
    private String id;
    private String name;
    private boolean gender;
    private String avatar;
    private String phoneNumber;
    private String coverImage;
    private String email;
    private List<String> images;
}
