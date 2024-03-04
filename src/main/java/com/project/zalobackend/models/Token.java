package com.project.zalobackend.models;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

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
    private String userId;
    private String token;
    private String tokenType;
    private LocalDateTime expirationDate;
    private boolean revoked;
    private String refreshToken;
    private LocalDateTime expirationDateRefreshToken;
    private boolean mobile;

}
