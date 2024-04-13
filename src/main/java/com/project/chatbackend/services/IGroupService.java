package com.project.chatbackend.services;

import com.project.chatbackend.exceptions.DataExistsException;
import com.project.chatbackend.exceptions.DataNotFoundException;
import com.project.chatbackend.exceptions.PermissionAccessDenied;
import com.project.chatbackend.models.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

public interface IGroupService {
    Room createGroup(String groupName, String ownerId, String ownerName, List<String> membersId, MultipartFile file)
            throws IOException, DataNotFoundException;
    void addMemberToGroup(List<String> membersId, String adderId, String groupId)
            throws DataNotFoundException, DataExistsException, PermissionAccessDenied;
    void removeMember(String memberId, String adminId, String groupId) throws DataNotFoundException, PermissionAccessDenied;
    Group findById(String id) throws DataNotFoundException;
    void removeGroup(String ownerId, String groupId) throws DataNotFoundException, PermissionAccessDenied;
    void addAdmin(String ownerId, String adminId, String groupId) throws DataNotFoundException, PermissionAccessDenied, DataExistsException;
    void removeAdmin(String ownerId, String adminId, String groupId) throws DataNotFoundException, PermissionAccessDenied;
    void updateAddMemberPermission(String ownerId, String groupId, AddMembersPermission addMembersPermission) throws DataNotFoundException, PermissionAccessDenied;
    void updateSendMessagePermission(String ownerId, String groupId, SendMessagePermission sendMessagePermission) throws DataNotFoundException, PermissionAccessDenied;
    void leaveGroup(String memberId, String groupId) throws DataNotFoundException, PermissionAccessDenied;
    List<Group> findAllBySenderId(String senderId);
    List<User> getMemberInGroup(String groupId) throws DataNotFoundException;



}
