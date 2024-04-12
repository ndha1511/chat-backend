package com.project.chatbackend.controllers;

import com.project.chatbackend.exceptions.DataExistsException;
import com.project.chatbackend.exceptions.DataNotFoundException;
import com.project.chatbackend.exceptions.PermissionAccessDenied;
import com.project.chatbackend.requests.AddMemberRequest;
import com.project.chatbackend.requests.CreateGroupRequest;
import com.project.chatbackend.services.AuthService;
import com.project.chatbackend.services.IGroupService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/groups")
@RequiredArgsConstructor
public class GroupController {
    private final IGroupService groupService;
    private AuthService authService;

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

    @PostMapping("/addMember")
    public ResponseEntity<?> addMemberToGroup(@RequestBody AddMemberRequest addMemberRequest) {
        try {
            groupService.addMemberToGroup(addMemberRequest.getMembersId(),
                    addMemberRequest.getAdderId(), addMemberRequest.getGroupId());
            return ResponseEntity.ok("add members successfully");
        } catch (DataNotFoundException e) {
            return ResponseEntity.badRequest().body(e);
        } catch (DataExistsException e) {
            return ResponseEntity.status(405).body(e);
        } catch (PermissionAccessDenied e) {
            return ResponseEntity.status(406).body(e);
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> findById(@PathVariable String id) {
        try {
            return ResponseEntity.ok(groupService.findById(id));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e);
        }
    }
}
