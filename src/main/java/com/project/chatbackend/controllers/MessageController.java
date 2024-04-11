package com.project.chatbackend.controllers;

import com.project.chatbackend.exceptions.DataNotFoundException;
import com.project.chatbackend.exceptions.PermissionAccessDenied;
import com.project.chatbackend.models.Message;
import com.project.chatbackend.repositories.MessageRepository;
import com.project.chatbackend.requests.*;
import com.project.chatbackend.responses.MessageResponse;
import com.project.chatbackend.services.IMessageService;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.Optional;

@RestController
@RequestMapping("/api/v1/messages")
@RequiredArgsConstructor
public class MessageController {
    private final IMessageService messageService;


    @GetMapping("/{roomId}")
    public ResponseEntity<?> getAllByRoomId(@PathVariable String roomId,
                                            @RequestParam String senderId,
                                            @RequestParam Optional<Integer> page,
                                            @RequestParam Optional<Integer> limit
    ) {
        int pageNum = page.orElse(0);
        int limitNum = limit.orElse(40);
        PageRequest pageRequest = PageRequest.of(pageNum, limitNum,
                Sort.by("sendDate").descending());
        try {
            MessageResponse messagePage = messageService.getAllByRoomId(senderId,roomId, pageRequest);

            return ResponseEntity.ok(messagePage);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("bad request");
        }
    }

    @PostMapping("/chat")
    public ResponseEntity<?> sendMessage(@ModelAttribute ChatRequest chatRequest) {
        try {
            Message messageTmp = messageService.saveMessage(chatRequest);
            messageService.saveMessage(chatRequest, messageTmp);
            return ResponseEntity.ok(messageTmp);
        } catch (DataNotFoundException e) {
            return ResponseEntity.badRequest().body("send message fail");
        } catch (PermissionAccessDenied e) {
            return ResponseEntity.status(406).body(e);
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateMessage(@PathVariable String id, @RequestBody ChatRequest chatRequest) {
        try {
            messageService.updateMessage(id, chatRequest);
            return ResponseEntity.ok("updated");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("update message fail");
        }
    }

    @PostMapping("/revokeMessage")
    public ResponseEntity<?> revokeMessage(@RequestBody RevokeMessageRequest revokeMessageRequest) {
        try {
            messageService.revokeMessage(revokeMessageRequest.getMessageId(), revokeMessageRequest.getReceiverId());
            return ResponseEntity.ok("revoke message successfully");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("cannot revoke message");
        }
    }

    @PostMapping("/seenMessage")
    public ResponseEntity<?> seenMessage(@RequestBody SeenMessageRequest seenMessageRequest) {
        try {
            messageService.seenMessage(
                    seenMessageRequest.getRoomId(),
                    seenMessageRequest.getSenderId(),
                    seenMessageRequest.getReceiverId()
            );
            return ResponseEntity.ok("seen message successfully");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e);
        }

    }

    @PostMapping("/saveImageGroup")
    public ResponseEntity<?> saveImageGroup(@ModelAttribute ChatImageGroupRequest chatImageGroupRequest) {
        try {
            Message message = messageService.saveMessageForImageGroup(chatImageGroupRequest);
            messageService.saveImageGroupMessage(chatImageGroupRequest, message);
            return ResponseEntity.ok(message);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e);
        }

    }

    @PostMapping("/forwardMessage")
    public ResponseEntity<?> forwardMessage(@RequestBody ForwardMessageRequest forwardMessageRequest) {
        try {
            messageService.forwardMessage(forwardMessageRequest.getMessageId(),
                    forwardMessageRequest.getSenderId(),
                    forwardMessageRequest.getReceiversId());
            return ResponseEntity.ok("forward message successfully");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e);
        }
    }


}
