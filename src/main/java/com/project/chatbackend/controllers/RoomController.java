package com.project.chatbackend.controllers;

import com.project.chatbackend.services.IRoomService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/rooms")
@RequiredArgsConstructor
public class RoomController {
    private final IRoomService roomService;
    @GetMapping("/all/{senderId}")
    public ResponseEntity<?> getAllRoomBySenderId(@PathVariable String senderId) {
        try {
            return ResponseEntity.ok(roomService.findAllBySenderId(senderId));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}
