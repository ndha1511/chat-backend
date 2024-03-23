package com.project.chatbackend.controllers;

import com.project.chatbackend.exceptions.DataNotFoundException;
import com.project.chatbackend.models.Message;
import com.project.chatbackend.responses.MessageResponse;
import com.project.chatbackend.services.IMessageService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequestMapping("/api/v1/messages")
@RequiredArgsConstructor
public class MessageController {
    private final IMessageService messageService;
    private final SimpMessagingTemplate simpMessagingTemplate;

    @GetMapping("/{roomId}")
    public ResponseEntity<?> getAllByRoomId(@PathVariable String roomId,
                                            @RequestParam Optional<Integer> page,
                                            @RequestParam Optional<Integer> limit
                                            ) {
        int pageNum = page.orElse(0);
        int limitNum = limit.orElse(40);
        PageRequest pageRequest = PageRequest.of(pageNum, limitNum,
                Sort.by("sendDate").descending());
        try {
            Page<Message> messagePage = messageService.getAllByRoomId(roomId, pageRequest);
            MessageResponse messageResponse = MessageResponse.builder()
                    .messages(messagePage.getContent())
                    .totalPage(messagePage.getTotalPages())
                    .build();
            return ResponseEntity.ok(messageResponse);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("bad request");
        }
    }

    @PostMapping("/chat")
    public ResponseEntity<?> sendMessage(@RequestBody Message message) {
        try {
            Message msg = messageService.saveMessage(message);
            simpMessagingTemplate.convertAndSendToUser(
                    message.getReceiverId(), "/queue/messages",
                    msg.getId()
            );
            return ResponseEntity.ok(msg);
        } catch (DataNotFoundException e) {
            return ResponseEntity.badRequest().body("send message fail");
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateStatusMessage(@PathVariable String id, @RequestBody Message message) {
        try {
            messageService.updateStatusMessage(id, message);
            return ResponseEntity.ok("updated");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("update message fail");
        }
    }

}
