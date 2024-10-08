package com.project.chatbackend.controllers;

import com.project.chatbackend.exceptions.*;
import com.project.chatbackend.models.Group;
import com.project.chatbackend.models.Message;
import com.project.chatbackend.requests.*;
import com.project.chatbackend.responses.MessageResponse;
import com.project.chatbackend.services.AuthService;
import com.project.chatbackend.services.IMessageService;
import com.project.chatbackend.services.IMessageServiceQuery;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

@RestController
@RequestMapping("/api/v1/messages")
@RequiredArgsConstructor
public class MessageController {
    private final IMessageService messageService;
    private final AuthService authService;
    private final IMessageServiceQuery messageServiceQuery;


    @GetMapping("/{roomId}")
    public ResponseEntity<?> getAllByRoomId(@PathVariable String roomId,
                                            @RequestParam String senderId,
                                            @RequestParam Optional<Integer> page,
                                            @RequestParam Optional<Integer> limit,
                                            HttpServletRequest request
    ) {
        int pageNum = page.orElse(0);
        int limitNum = limit.orElse(40);
        PageRequest pageRequest = PageRequest.of(pageNum, limitNum,
                Sort.by("sendDate").descending());
        try {
            authService.AuthenticationToken(request, senderId);
            MessageResponse messagePage = messageService.getAllByRoomId(senderId,roomId, pageRequest);
            return ResponseEntity.ok(messagePage);
        } catch (PermissionAccessDenied e) {
            return ResponseEntity.status(406).body(e.getMessage());
        }
    }



    @PostMapping("/chat")
    public ResponseEntity<?> sendMessage(@ModelAttribute ChatRequest chatRequest, HttpServletRequest httpServletRequest) {
        try {
            authService.AuthenticationToken(httpServletRequest, chatRequest.getSenderId());
            Map<String, Object> mapResult = messageService.saveMessage(chatRequest);
            Message messageTmp = (Message) mapResult.get("message");
            Group group = (Group) mapResult.get("group");
            messageService.saveMessage(chatRequest, messageTmp, group);
            return ResponseEntity.ok(messageTmp);
        } catch (DataNotFoundException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (PermissionAccessDenied e) {
            return ResponseEntity.status(406).body(e.getMessage());
        } catch (BlockUserException e) {
            return ResponseEntity.status(410).body(e.getMessage());
        } catch (BlockMessageToStranger e) {
            return ResponseEntity.status(411).body(e.getMessage());
        } catch (MaxFileSizeException e) {
            return ResponseEntity.status(412).body(e.getMessage());
        }
    }

    @PostMapping("/callRequest")
    public ResponseEntity<?> callRequest(@RequestBody CallRequest callRequest,
                                         HttpServletRequest httpServletRequest) {
        try {
            authService.AuthenticationToken(httpServletRequest, callRequest.getSenderId());
            Message ms = messageService.saveCall(callRequest);
            return ResponseEntity.ok(ms);
        } catch (PermissionAccessDenied e) {
            return ResponseEntity.status(406).body(e.getMessage());
        } catch (DataNotFoundException e) {
            return ResponseEntity.status(404).body(e.getMessage());
        } catch (BlockUserException e) {
            return ResponseEntity.status(410).body(e.getMessage());
        } catch (BlockMessageToStranger e) {
            return ResponseEntity.status(411).body(e.getMessage());
        }

    }

    @GetMapping("/acceptCallRequest/{messageId}")
    public ResponseEntity<?> acceptCallRequest(@PathVariable String messageId) {
        messageService.acceptCall(messageId);
        return ResponseEntity.ok("accepted, calling...");
    }

    @GetMapping("/rejectCallRequest/{messageId}")
    public ResponseEntity<?> rejectCallRequest(@PathVariable String messageId) {
        messageService.rejectCall(messageId);
        return ResponseEntity.ok("rejected");
    }

    @GetMapping("/closeCall/{messageId}")
    public ResponseEntity<?> closeCall(@PathVariable String messageId) {
        messageService.endCall(messageId);
        return ResponseEntity.ok("stopped");
    }

    @GetMapping("/cancelCall/{messageId}")
    public ResponseEntity<?> cancelCall(@PathVariable String messageId) {
        messageService.cancelCall(messageId);
        return ResponseEntity.ok("stopped");
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
    public ResponseEntity<?> revokeMessage(@RequestBody RevokeMessageRequest revokeMessageRequest,
                                           HttpServletRequest httpServletRequest) {
        try {
            authService.AuthenticationToken(httpServletRequest, revokeMessageRequest.getSenderId());
            messageService.revokeMessage(revokeMessageRequest.getMessageId(),
                    revokeMessageRequest.getSenderId(),
                    revokeMessageRequest.getReceiverId());
            return ResponseEntity.ok("revoke message successfully");
        } catch (PermissionAccessDenied e) {
            return ResponseEntity.status(406).body(e.getMessage());
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

    @GetMapping("/query")
    public ResponseEntity<?> findMessage(@RequestParam String roomId,
                                         @RequestParam(required = false) String senderId,
                                         @RequestParam String content,
                                         @RequestParam String currentId,
                                         @RequestParam(defaultValue = "") String startDate,
                                         @RequestParam(defaultValue = "") String endDate,
                                         @RequestParam(defaultValue = "0") int page,
                                         @RequestParam(defaultValue = "40") int size,
                                         HttpServletRequest httpServletRequest) {
        try {
            Pageable pageable = PageRequest.of(page, size);
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            Date start = null;
            Date end = null;

            if (!Objects.equals(startDate, "") && !Objects.equals(endDate, "")
            && !startDate.isEmpty() && !endDate.isEmpty()) {
                start = sdf.parse(startDate);
                end = sdf.parse(endDate);
            }
            authService.AuthenticationToken(httpServletRequest, currentId);
            return ResponseEntity.ok(messageServiceQuery.findByContentContaining(
                    roomId,
                    content,
                    start,
                    end,
                    senderId,
                    pageable
            ));
        } catch (PermissionAccessDenied e) {
            return ResponseEntity.badRequest().body(e);
        } catch (ParseException e) {
            return ResponseEntity.badRequest().body("date invalid");
        }
    }

    @PutMapping("/receiveMessage")
    public ResponseEntity<?> receiveMessage(@RequestBody Message message) {
        messageService.receiveMessage(message);
        return ResponseEntity.ok("receive message successfully");
    }






}
