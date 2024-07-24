package com.project.chatbackend.responses;

import lombok.Builder;
import lombok.Data;

import java.util.List;
import java.util.Set;

@Data
@Builder
public class UserLoginResponse {
    private String name;
    private boolean gender;
    private String avatar;
    private String phoneNumber;
    private String coverImage;
    private String email;
    private String dob;
    private List<String> friends;
    private List<String> images;
    private Set<String> blockUsers;
    private boolean notReceiveMessageToStranger;
}
