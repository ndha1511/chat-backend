package com.project.chatbackend.controllers;

import com.project.chatbackend.requests.UserNotify;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/notify")
@RequiredArgsConstructor
public class ChanelController {
    private final SimpMessagingTemplate simpMessagingTemplate;

    @PostMapping("/user")
    public ResponseEntity<?> processMessage(@RequestBody UserNotify useNotify) {
            simpMessagingTemplate.convertAndSendToUser(
                useNotify.getReceiverId(), "/queue/messages",
                useNotify
            );
            return ResponseEntity.ok("send notify to user successfully");
    }
}
