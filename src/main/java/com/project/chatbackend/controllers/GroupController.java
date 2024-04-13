package com.project.chatbackend.controllers;

import com.project.chatbackend.exceptions.DataExistsException;
import com.project.chatbackend.exceptions.DataNotFoundException;
import com.project.chatbackend.exceptions.PermissionAccessDenied;
import com.project.chatbackend.requests.AddMemberRequest;
import com.project.chatbackend.requests.CreateGroupRequest;
import com.project.chatbackend.requests.GroupActionRequest;
import com.project.chatbackend.services.AuthService;
import com.project.chatbackend.services.IGroupService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/groups")
@RequiredArgsConstructor
public class GroupController {
    private final IGroupService groupService;
    private final AuthService authService;

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
    public ResponseEntity<?> addMemberToGroup(@RequestBody AddMemberRequest addMemberRequest,
                                              HttpServletRequest httpServletRequest) {
        try {
            authService.AuthenticationToken(httpServletRequest, addMemberRequest.getAdderId());
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

    @PostMapping("/removeMember")
    public ResponseEntity<?> removeMember(@RequestBody GroupActionRequest groupActionRequest,
                                          HttpServletRequest httpServletRequest) {
        try {
            authService.AuthenticationToken(httpServletRequest, groupActionRequest.getAdminId());
            groupService.removeMember(groupActionRequest.getMemberId(),
                    groupActionRequest.getAdminId(),
                    groupActionRequest.getGroupId());
            return ResponseEntity.ok("remove member successfully");
        } catch (PermissionAccessDenied e) {
            return ResponseEntity.status(406).body(e.getMessage());
        } catch (DataNotFoundException e) {
            return ResponseEntity.status(404).body(e.getMessage());
        }

    }

    @GetMapping()
    public ResponseEntity<?> getAllBySenderId(@RequestParam String senderId,
                                              HttpServletRequest httpServletRequest) {
        try {
            authService.AuthenticationToken(httpServletRequest, senderId);
            return ResponseEntity.ok(groupService.findAllBySenderId(senderId));
        } catch (PermissionAccessDenied e) {
            return ResponseEntity.status(406).body(e.getMessage());
        }
    }


    @DeleteMapping("/remove")
    public ResponseEntity<?> removeGroup(@RequestBody GroupActionRequest groupActionRequest,
                                         HttpServletRequest httpServletRequest) {
        try {
            authService.AuthenticationToken(httpServletRequest, groupActionRequest.getOwnerId());
            groupService.removeGroup(groupActionRequest.getOwnerId(), groupActionRequest.getGroupId());
            return ResponseEntity.ok("group removed");
        } catch (PermissionAccessDenied e) {
            return ResponseEntity.status(406).body(e.getMessage());
        } catch (DataNotFoundException e) {
            return ResponseEntity.status(404).body(e.getMessage());
        }
    }

    @PutMapping("/addAdmin")
    public ResponseEntity<?> addAdmin(@RequestBody GroupActionRequest groupActionRequest,
                                      HttpServletRequest httpServletRequest) {
        try {
            authService.AuthenticationToken(httpServletRequest, groupActionRequest.getOwnerId());
            groupService.addAdmin(groupActionRequest.getOwnerId(),
                    groupActionRequest.getAdminId(),
                    groupActionRequest.getGroupId()
            );
            return ResponseEntity.ok("add admin successfully");
        } catch (PermissionAccessDenied e) {
            return ResponseEntity.status(406).body(e.getMessage());
        } catch (DataNotFoundException e) {
            return ResponseEntity.status(404).body(e.getMessage());
        } catch (DataExistsException e) {
            return ResponseEntity.status(405).body(e.getMessage());
        }

    }

    @PutMapping("/removeAdmin")
    public ResponseEntity<?> removeAdmin(@RequestBody GroupActionRequest groupActionRequest,
                                      HttpServletRequest httpServletRequest) {
        try {
            authService.AuthenticationToken(httpServletRequest, groupActionRequest.getOwnerId());
            groupService.removeAdmin(groupActionRequest.getOwnerId(),
                    groupActionRequest.getAdminId(),
                    groupActionRequest.getGroupId()
            );
            return ResponseEntity.ok("remove admin successfully");
        } catch (PermissionAccessDenied e) {
            return ResponseEntity.status(406).body(e.getMessage());
        } catch (DataNotFoundException e) {
            return ResponseEntity.status(404).body(e.getMessage());
        }

    }

    @PutMapping("/updateSendMessagePermission")
    public ResponseEntity<?> updateSendPermission(@RequestBody GroupActionRequest groupActionRequest,
                                         HttpServletRequest httpServletRequest) {
        try {
            authService.AuthenticationToken(httpServletRequest, groupActionRequest.getOwnerId());
            groupService.updateSendMessagePermission(groupActionRequest.getOwnerId(),
                    groupActionRequest.getGroupId(),
                    groupActionRequest.getSendMessagePermission()
            );
            return ResponseEntity.ok("update successfully");
        } catch (PermissionAccessDenied e) {
            return ResponseEntity.status(406).body(e.getMessage());
        } catch (DataNotFoundException e) {
            return ResponseEntity.status(404).body(e.getMessage());
        }

    }

    @PutMapping("/updateAddMemberPermission")
    public ResponseEntity<?> updateAddPermission(@RequestBody GroupActionRequest groupActionRequest,
                                                  HttpServletRequest httpServletRequest) {
        try {
            authService.AuthenticationToken(httpServletRequest, groupActionRequest.getOwnerId());
            groupService.updateAddMemberPermission(groupActionRequest.getOwnerId(),
                    groupActionRequest.getGroupId(),
                    groupActionRequest.getAddMembersPermission()
            );
            return ResponseEntity.ok("update successfully");
        } catch (PermissionAccessDenied e) {
            return ResponseEntity.status(406).body(e.getMessage());
        } catch (DataNotFoundException e) {
            return ResponseEntity.status(404).body(e.getMessage());
        }

    }

    @PutMapping("/leaveGroup")
    public ResponseEntity<?> leaveGroup(@RequestBody GroupActionRequest groupActionRequest,
                                        HttpServletRequest httpServletRequest) {
        try {
            authService.AuthenticationToken(httpServletRequest, groupActionRequest.getMemberId());
            groupService.leaveGroup(groupActionRequest.getMemberId(), groupActionRequest.getGroupId());
            return ResponseEntity.ok("leave group successfully");

        } catch (PermissionAccessDenied e) {
            return ResponseEntity.status(406).body(e.getMessage());
        } catch (DataNotFoundException e) {
            return ResponseEntity.status(404).body(e.getMessage());
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
