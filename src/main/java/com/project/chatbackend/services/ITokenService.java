package com.project.chatbackend.services;

import com.project.chatbackend.models.Token;

import java.util.List;

public interface ITokenService {
    void save(Token token);
    void delete(String id);
    void update(String id, Token token);
    List<Token> findAllByUserId(String userId);
}
