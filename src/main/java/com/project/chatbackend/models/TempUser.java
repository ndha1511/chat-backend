package com.project.chatbackend.models;

import lombok.*;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.time.LocalDate;

@Document(collection = "temp_users")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class TempUser {
    private String id;
    private String name;
    private boolean gender;
    private LocalDate dateOfBirth;
    private String phoneNumber;
    private String email;
    private String password;
    private String avatar;
}
