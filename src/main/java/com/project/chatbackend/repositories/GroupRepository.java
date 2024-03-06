package com.project.chatbackend.repositories;

import com.project.chatbackend.models.Group;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface GroupRepository extends MongoRepository<Group, String> {
}
