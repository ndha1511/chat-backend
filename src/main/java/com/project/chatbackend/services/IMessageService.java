package com.project.chatbackend.services;

import com.project.chatbackend.exceptions.DataNotFoundException;
import com.project.chatbackend.exceptions.PermissionAccessDenied;
import com.project.chatbackend.models.Message;
import com.project.chatbackend.requests.CallRequest;
import com.project.chatbackend.requests.ChatImageGroupRequest;
import com.project.chatbackend.requests.ChatRequest;
import com.project.chatbackend.responses.MessageResponse;
import org.springframework.data.domain.PageRequest;

import java.util.List;

public interface IMessageService {
    void saveMessage(ChatRequest chatRequest, Message messageTmp) throws DataNotFoundException, PermissionAccessDenied;
    MessageResponse getAllByRoomId(String senderId, String roomId, PageRequest pageRequest);
    void updateMessage(String id, ChatRequest chatRequest);
    Message saveMessage(ChatRequest chatRequest) throws DataNotFoundException, PermissionAccessDenied;
    Message saveMessageForImageGroup(ChatImageGroupRequest chatImageGroupRequest) throws Exception;
    void revokeMessage(String messageId, String senderId, String receiverId) throws PermissionAccessDenied;
    void forwardMessage(String messageId, String senderId, List<String> receiversId) throws DataNotFoundException;
    void saveImageGroupMessage(ChatImageGroupRequest chatImageGroupRequest, Message messageTmp) throws DataNotFoundException;
    void seenMessage(String roomId, String senderId, String receiverId);
    Message saveCall(CallRequest callRequest) throws DataNotFoundException, PermissionAccessDenied;
    void acceptCall(String messageId);
    void rejectCall(String messageId);
    void endCall(String messageId);
}
