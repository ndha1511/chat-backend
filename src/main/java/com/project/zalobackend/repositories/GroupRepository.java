package com.project.zalobackend.repositories;

import com.project.zalobackend.models.Group;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface GroupRepository extends MongoRepository<Group, String> {
}
