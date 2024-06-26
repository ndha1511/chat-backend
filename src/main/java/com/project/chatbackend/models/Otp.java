package com.project.chatbackend.models;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Field;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class Otp implements java.io.Serializable{
    @Field(name = "otp")
    private String otp;
    @Id
    private String email;
    @Field(name = "created_at")
    private long createdAt;
    @Field(name = "expired_date")
    private LocalDateTime expiredDate;
}
