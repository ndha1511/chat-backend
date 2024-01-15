package com.project.zalobackend.repositories;

import com.project.zalobackend.models.User;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface UserRepository extends MongoRepository<User, String> {
}
