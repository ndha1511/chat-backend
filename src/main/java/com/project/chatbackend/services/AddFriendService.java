package com.project.chatbackend.services;

import com.project.chatbackend.exceptions.DataNotFoundException;
import com.project.chatbackend.models.RequestAddFriend;
import com.project.chatbackend.models.User;
import com.project.chatbackend.repositories.RequestAddFriendRepository;
import com.project.chatbackend.repositories.UserRepository;
import com.project.chatbackend.requests.AcceptAddFriendRequest;
import com.project.chatbackend.requests.RejectAddFriendRequest;
import com.project.chatbackend.requests.SendAddFriendRequest;
import com.project.chatbackend.requests.UnfriendRequest;
import com.project.chatbackend.responses.AcceptAddFriendRespone;
import com.project.chatbackend.responses.RequestAddFriendRespone;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AddFriendService implements IAddFriendService {
    private final RequestAddFriendRepository requestAddFriendRepository;
    private final SimpMessagingTemplate simpMessagingTemplate;
    private final UserService userService;
    private final UserRepository userRepository;

    @Override
    public void sendAddFriendRequest(SendAddFriendRequest sendAddFriendRequest) throws DataNotFoundException {
        User sender = userService.findUserByEmail(sendAddFriendRequest.getSenderId());
        User receiver = userService.findUserByEmail(sendAddFriendRequest.getReceiverId());
        if (sender == null || receiver == null) {
            throw new DataNotFoundException("User not found");
        }
        if (isFriend(sender, receiver)) {
            throw new RuntimeException("User is already friend");
        }
        RequestAddFriend find = requestAddFriendRepository.findBySenderIdAndReceiverId(sendAddFriendRequest.getSenderId(), sendAddFriendRequest.getReceiverId());
        if (find != null) {
            throw new DataNotFoundException("Request already sent");
        }
        RequestAddFriend requestAddFriend = RequestAddFriend.builder().senderId(sendAddFriendRequest.getSenderId())
                .receiverId(sendAddFriendRequest.getReceiverId()).message(sendAddFriendRequest.getMessage()).build();
        requestAddFriendRepository.save(requestAddFriend);
        RequestAddFriendRespone requestAddFriendRespone = RequestAddFriendRespone.builder().user(sender).message(requestAddFriend.getMessage()).build();
        simpMessagingTemplate.convertAndSendToUser(receiver.getEmail(), "/queue/friend-request", requestAddFriendRespone);


    }

    @Override
    public void acceptAddFriendRequest(AcceptAddFriendRequest acceptAddFriendRequest) throws DataNotFoundException {
        User sender = userService.findUserByEmail(acceptAddFriendRequest.getSenderId());
        User receiver = userService.findUserByEmail(acceptAddFriendRequest.getReceiverId());
        if (sender == null || receiver == null) {
            throw new DataNotFoundException("User not found");
        }
        if (isFriend(sender, receiver)) {
            throw new RuntimeException("User is already friend");
        }
        sender.getFriends().add(receiver.getEmail());
        receiver.getFriends().add(sender.getEmail());
        userRepository.save(sender);
        userRepository.save(receiver);
        requestAddFriendRepository.deleteBySenderIdAndReceiverId(acceptAddFriendRequest.getSenderId(), acceptAddFriendRequest.getReceiverId());
        requestAddFriendRepository.deleteBySenderIdAndReceiverId(acceptAddFriendRequest.getReceiverId(), acceptAddFriendRequest.getSenderId());
        simpMessagingTemplate.convertAndSendToUser(receiver.getEmail(), "/queue/response-friend-request", AcceptAddFriendRespone.builder().user(sender).message("Request accepted").build());
    }

    @Override
    @Transactional
    public void rejectAddFriendRequest(RejectAddFriendRequest rejectAddFriendRequest) throws DataNotFoundException {
        User sender = userService.findUserByEmail((rejectAddFriendRequest.getSenderId()));
        User receiver = userService.findUserByEmail(rejectAddFriendRequest.getReceiverId());
        if (sender == null || receiver == null) {
            throw new DataNotFoundException("User not found");
        }
        requestAddFriendRepository.deleteBySenderIdAndReceiverId(rejectAddFriendRequest.getSenderId(), rejectAddFriendRequest.getReceiverId());
        requestAddFriendRepository.deleteBySenderIdAndReceiverId(rejectAddFriendRequest.getReceiverId(), rejectAddFriendRequest.getSenderId());
    }

    @Override
    public void unfriend(UnfriendRequest unfriendRequest) throws DataNotFoundException {
        User sender = userService.findUserByEmail(unfriendRequest.getSenderId());
        User receiver = userService.findUserByEmail(unfriendRequest.getReceiverId());
        if (sender == null || receiver == null) {
            throw new DataNotFoundException("User not found");
        }
        if (!isFriend(sender, receiver) || !isFriend(receiver, sender)) {
            throw new RuntimeException("User is not friend");
        }
        sender.getFriends().remove(receiver.getEmail());
        receiver.getFriends().remove(sender.getEmail());
        userRepository.save(sender);
        userRepository.save(receiver);
    }

    @Override
    public List<User> getFriendList(String userId) {
        return null;
    }

    @Override
    public List<RequestAddFriendRespone> getRequestAddFriendByReceiverId(String receiverId) throws DataNotFoundException {
        List<RequestAddFriend> requestAddFriends = requestAddFriendRepository.findByReceiverId(receiverId);
        List<RequestAddFriendRespone> requestAddFriendRespones = new ArrayList<>();
        for (RequestAddFriend requestAddFriend :
                requestAddFriends) {
            User user = userService.findUserByEmail(requestAddFriend.getSenderId());
            if (user == null) {
                throw new DataNotFoundException("User not found");
            }
            RequestAddFriendRespone requestAddFriendRespone = RequestAddFriendRespone.builder().user(user).message(requestAddFriend.getMessage()).build();
            requestAddFriendRespones.add(requestAddFriendRespone);
        }
        return requestAddFriendRespones;
    }

    @Override
    public List<RequestAddFriend> getRequestAddFriendBySenderId(String senderId) {
        return requestAddFriendRepository.findBySenderId(senderId);
    }

    private boolean isFriend(User sender, User receiver) {
        return sender.getFriends().contains(receiver.getEmail());
    }
}
