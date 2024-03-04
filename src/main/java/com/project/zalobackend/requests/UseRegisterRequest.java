package com.project.zalobackend.requests;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;

@Data
@Builder
public class UseRegisterRequest {
    private String phoneNumber;
    private String name;
    private boolean gender;
    private LocalDate dateOfBirth;
    private String password;
    private String avatar;
}
