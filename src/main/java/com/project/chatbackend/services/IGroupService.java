package com.project.chatbackend.services;

import com.project.chatbackend.exceptions.DataExistsException;
import com.project.chatbackend.exceptions.DataNotFoundException;
import com.project.chatbackend.exceptions.PermissionAccessDenied;
import com.project.chatbackend.models.AddMembersPermission;
import com.project.chatbackend.models.Group;
import com.project.chatbackend.models.Room;
import com.project.chatbackend.models.SendMessagePermission;
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
    void removeGroup(String ownerId, String groupId);
    void addAdmin(String ownerId, String adminId, String groupId);
    void removeAdmin(String ownerId, String adminId, String groupId);
    void updateAddMemberPermission(String ownerId, String groupId, AddMembersPermission addMembersPermission);
    void updateSendMessagePermission(String ownerId, String groupId, SendMessagePermission sendMessagePermission);


}
