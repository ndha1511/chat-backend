package com.project.chatbackend.repositories;

import com.project.chatbackend.models.Token;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;
import java.util.Optional;

public interface TokenRepository extends MongoRepository<Token, String> {
    Optional<Token> findByToken(String token);
    Optional<Token> findByRefreshToken(String refreshToken);
    List<Token> findAllByUserId(String userId);
}
