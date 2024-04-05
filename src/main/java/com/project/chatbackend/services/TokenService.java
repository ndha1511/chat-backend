package com.project.chatbackend.services;

import com.project.chatbackend.models.Token;
import com.project.chatbackend.repositories.TokenRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class TokenService implements ITokenService {
    private final TokenRepository tokenRepository;
    @Override
    public void save(Token token) {
        tokenRepository.save(token);
    }

    @Override
    public void delete(String id) {
        tokenRepository.deleteById(id);
    }

    @Override
    public void update(String id, Token token) {

    }

    @Override
    public List<Token> findAllByUserId(String userId) {
        return tokenRepository.findAllByUserEmail(userId);
    }
}
