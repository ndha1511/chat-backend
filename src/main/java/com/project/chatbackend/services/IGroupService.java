package com.project.chatbackend.services;

import com.project.chatbackend.exceptions.DataExistsException;
import com.project.chatbackend.exceptions.DataNotFoundException;
import com.project.chatbackend.exceptions.PermissionAccessDenied;
import com.project.chatbackend.models.Group;
import com.project.chatbackend.models.Room;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

public interface IGroupService {

    Room createGroup(String groupName, String ownerId, String ownerName, List<String> membersId, MultipartFile file)
            throws IOException, DataNotFoundException;
    void addMemberToGroup(List<String> membersId, String adderId, String groupId)
            throws DataNotFoundException, DataExistsException, PermissionAccessDenied;
    Group findById(String id) throws DataNotFoundException;

}
