package com.project.chatbackend.models;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Document(collection = "users")
@Getter @Setter
@AllArgsConstructor @NoArgsConstructor
@Builder
public class User {
    @Id
    private String id;
    private String name;
    private boolean gender;
    private LocalDate dateOfBirth;
    @Indexed
    private String phoneNumber;
    private String password;
    private String avatar;
    private List<String> friends;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

}
