package com.project.chatbackend.controllers;

import com.project.chatbackend.services.IRoomService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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

    @GetMapping("/findBySenderIdAndReceiverId")
    public ResponseEntity<?> findBySenderIdAndReceiverId(@RequestParam("sender") String senderId, @RequestParam("receiver") String receiverId) {
        try {
            return ResponseEntity.ok(roomService.findBySenderIdAndReceiverId(
                   senderId, receiverId));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}
