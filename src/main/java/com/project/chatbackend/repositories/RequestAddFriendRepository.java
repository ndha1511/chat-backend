package com.project.chatbackend.repositories;

import com.project.chatbackend.models.RequestAddFriend;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface RequestAddFriendRepository extends MongoRepository<RequestAddFriend, String> {
    RequestAddFriend findBySenderIdAndReceiverId(String senderId, String receiverId);
    List<RequestAddFriend> findByReceiverId(String receiverId);
    List<RequestAddFriend> findBySenderId(String senderId);
    void deleteBySenderIdAndReceiverId(String senderId, String receiverId);
}
