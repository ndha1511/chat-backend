package com.project.chatbackend.models;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Document(collection = "users")
@Getter @Setter
@AllArgsConstructor @NoArgsConstructor
@Builder
public class User {
    private String name;
    private boolean gender;
    @Field(name = "date_of_birth")
    private LocalDate dateOfBirth;
    @Field(name = "phone_number")
    private String phoneNumber;
    @Id
    @Indexed
    private String email;
    private String password;
    private String avatar;
    private List<String> friends = new ArrayList<>();
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
    @Field(name = "block_ids")
    private Set<String> blockIds = new HashSet<>();
    @Field(name = "not_receive_message_to_stranger")
    private boolean notReceiveMessageToStranger;


}
