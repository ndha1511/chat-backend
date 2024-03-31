package com.project.chatbackend.requests;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Field;

import java.time.LocalDate;
@Data
@NoArgsConstructor
@Builder
@AllArgsConstructor
public class UserUpdateRequest {
    private boolean gender;
    private LocalDate dateOfBirth;
    private String phoneNumber;
    private String password;
    private String avatar;
}
