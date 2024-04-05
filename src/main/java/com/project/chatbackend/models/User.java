package com.project.chatbackend.models;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Document(collection = "users")
@Getter @Setter
@AllArgsConstructor @NoArgsConstructor
@Builder
public class User {
    private String name;
    private boolean gender;
    @Field(name = "date_of_birth")
    private LocalDate dateOfBirth;
    @Indexed
    @Field(name = "phone_number")
    private String phoneNumber;
    @Id
    private String email;
    private String password;
    private String avatar;
    private List<String> friends;
    @Field(name = "created_at")
    private LocalDateTime createdAt;
    @Field(name = "updated_at")
    private LocalDateTime updatedAt;
    @Field(name = "public_dob")
    private boolean publicDob;
    @Field(name = "public_phone")
    private boolean publicPhone;
    private List<String> images;
    @Field(name = "cover_image")
    private String coverImage;
    @Field(name = "verify")
    private boolean isVerified;



}
