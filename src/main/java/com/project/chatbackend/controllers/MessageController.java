package com.project.chatbackend.controllers;

import com.project.chatbackend.models.Message;
import com.project.chatbackend.responses.MessageResponse;
import com.project.chatbackend.services.IMessageService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequestMapping("/api/v1/messages")
@RequiredArgsConstructor
public class MessageController {
    private final IMessageService messageService;

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

}
