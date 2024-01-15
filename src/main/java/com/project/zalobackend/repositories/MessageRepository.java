package com.project.zalobackend.repositories;

import com.project.zalobackend.models.Message;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface MessageRepository extends MongoRepository<Message, String> {
}
