package com.project.chatbackend.services;

import com.project.chatbackend.exceptions.DataNotFoundException;
import com.project.chatbackend.models.RequestAddFriend;
import com.project.chatbackend.models.User;
import com.project.chatbackend.requests.AcceptAddFriendRequest;
import com.project.chatbackend.requests.RejectAddFriendRequest;
import com.project.chatbackend.requests.SendAddFriendRequest;
import com.project.chatbackend.requests.UnfriendRequest;
import com.project.chatbackend.responses.RequestAddFriendRespone;

import java.util.List;

public interface IAddFriendService {
    void sendAddFriendRequest(SendAddFriendRequest sendAddFriendRequest) throws DataNotFoundException;
    void acceptAddFriendRequest(AcceptAddFriendRequest acceptAddFriendRequest) throws DataNotFoundException;
    void rejectAddFriendRequest(RejectAddFriendRequest rejectAddFriendRequest) throws DataNotFoundException;
    void unfriend(UnfriendRequest unfriendRequest) throws DataNotFoundException;
    List<User> getFriendList(String userId);
    List<RequestAddFriendRespone> getRequestAddFriendByReceiverId(String receiverId) throws DataNotFoundException;
    List<RequestAddFriend> getRequestAddFriendBySenderId(String senderId);
}
