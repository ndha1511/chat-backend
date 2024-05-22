package com.project.chatbackend.repositories;

import com.project.chatbackend.models.Emoji;
import org.springframework.data.mongodb.repository.MongoRepository;



public interface EmojiRepository extends MongoRepository<Emoji, String> {
}
