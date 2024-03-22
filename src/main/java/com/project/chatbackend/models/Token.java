package com.project.chatbackend.models;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.DocumentReference;
import org.springframework.data.mongodb.core.mapping.Field;

import java.time.LocalDateTime;

@Document(collection = "tokens")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Token {
    @Id
    private String id;
    @DocumentReference
    private User user;
    @Field(name = "access_token")
    private String accessToken;
    @Field(name = "access_token_type")
    private String tokenType;
    @Field(name = "expiration_date_access_token")
    private LocalDateTime expirationDateAccessToken;
    private boolean revoked;
    @Field(name = "refresh_token")
    private String refreshToken;
    @Field(name = "expiration_date_refresh_token")
    private LocalDateTime expirationDateRefreshToken;
    @Field(name = "is_mobile")
    private boolean isMobile;

}
