package com.project.chatbackend.services;

import com.project.chatbackend.exceptions.*;
import com.project.chatbackend.models.Group;
import com.project.chatbackend.models.Message;
import com.project.chatbackend.requests.CallRequest;
import com.project.chatbackend.requests.ChatImageGroupRequest;
import com.project.chatbackend.requests.ChatRequest;
import com.project.chatbackend.responses.MessageResponse;
import org.springframework.data.domain.PageRequest;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

public interface IMessageService {
    @Transactional
    void saveMessage(ChatRequest chatRequest, Message messageTmp, Group group) throws MaxFileSizeException;

    MessageResponse getAllByRoomId(String senderId, String roomId, PageRequest pageRequest);
    void updateMessage(String id, ChatRequest chatRequest);
    Map<String, Object> saveMessage(ChatRequest chatRequest) throws DataNotFoundException, PermissionAccessDenied, BlockUserException, BlockMessageToStranger;
    Message saveMessageForImageGroup(ChatImageGroupRequest chatImageGroupRequest) throws Exception;
    void revokeMessage(String messageId, String senderId, String receiverId) throws PermissionAccessDenied;
    void forwardMessage(String messageId, String senderId, List<String> receiversId) throws DataNotFoundException;
    void saveImageGroupMessage(ChatImageGroupRequest chatImageGroupRequest, Message messageTmp) throws DataNotFoundException;
    void seenMessage(String roomId, String senderId, String receiverId);
    Message saveCall(CallRequest callRequest) throws DataNotFoundException, PermissionAccessDenied, BlockUserException, BlockMessageToStranger;
    void acceptCall(String messageId);
    void rejectCall(String messageId);
    void endCall(String messageId);
}
