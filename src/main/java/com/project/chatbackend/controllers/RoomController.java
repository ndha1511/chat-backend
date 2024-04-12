package com.project.chatbackend.controllers;

import com.project.chatbackend.exceptions.DataNotFoundException;
import com.project.chatbackend.exceptions.PermissionAccessDenied;
import com.project.chatbackend.models.Room;
import com.project.chatbackend.responses.PageRoomResponse;
import com.project.chatbackend.responses.RoomResponse;
import com.project.chatbackend.services.AuthService;
import com.project.chatbackend.services.IRoomService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/v1/rooms")
@RequiredArgsConstructor
public class RoomController {
    private final IRoomService roomService;
    private final AuthService authService;
    @GetMapping("/all/{senderId}")
    public ResponseEntity<?> getAllRoomBySenderId(@PathVariable String senderId,
                                                  @RequestParam Optional<Integer> page,
                                                  @RequestParam Optional<Integer> limit,
                                                  HttpServletRequest httpServletRequest) {
        int pageNum = page.orElse(0);
        int limitNum = limit.orElse(20);
        PageRequest pageRequest = PageRequest.of(pageNum, limitNum,
                Sort.by("time").descending());
        try {
            authService.AuthenticationToken(httpServletRequest, senderId);
            Page<Room> rooms = roomService.findAllBySenderId(senderId, pageRequest);
            List<RoomResponse> roomResponses = rooms
                    .getContent()
                    .stream()
                    .map(room -> {
                        try {
                            return roomService.mapRoomToRoomResponse(room);
                        } catch (DataNotFoundException e) {
                            throw new RuntimeException(e);
                        }
                    }).toList();
            PageRoomResponse pageRoomResponse = PageRoomResponse.builder()
                    .roomResponses(roomResponses)
                    .totalPage(rooms.getTotalPages())
                    .build();
            return ResponseEntity.ok(pageRoomResponse);
        } catch (PermissionAccessDenied e) {
            return ResponseEntity.status(406).body(e.getMessage());
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
