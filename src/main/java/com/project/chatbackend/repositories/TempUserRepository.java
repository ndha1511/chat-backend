package com.project.chatbackend.repositories;

import com.project.chatbackend.models.TempUser;
import com.project.chatbackend.models.Token;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

public interface TempUserRepository  extends MongoRepository<TempUser, String> {
    Optional<TempUser> findByEmail(String email);
}
