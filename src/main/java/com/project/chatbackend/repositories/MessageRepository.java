package com.project.chatbackend.repositories;

import com.project.chatbackend.models.Message;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface MessageRepository extends MongoRepository<Message, String> {
}
