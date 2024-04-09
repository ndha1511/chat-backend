package com.project.chatbackend.controllers;

import com.project.chatbackend.requests.CreateGroupRequest;
import com.project.chatbackend.services.IGroupService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/groups")
@RequiredArgsConstructor
public class GroupController {
    private final IGroupService groupService;

    @PostMapping()
    public ResponseEntity<?> createGroup(@ModelAttribute CreateGroupRequest createGroupRequest) {
        try {
            return ResponseEntity.ok(groupService.createGroup(
                    createGroupRequest.getGroupName(),
                    createGroupRequest.getOwnerId(),
                    createGroupRequest.getOwnerName(),
                    createGroupRequest.getMembersId(),
                    createGroupRequest.getAvatar()
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e);
        }

    }
}
