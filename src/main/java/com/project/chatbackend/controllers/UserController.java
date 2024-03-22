package com.project.chatbackend.controllers;

import com.project.chatbackend.responses.UserLoginResponse;
import com.project.chatbackend.services.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;

    @GetMapping("/{phoneNumber}")
    public ResponseEntity<?> findByPhoneNumber(@PathVariable String phoneNumber) {
        try {
            UserLoginResponse userLoginResponse = userService.findByPhoneNumber(phoneNumber);
            return ResponseEntity.ok(userLoginResponse);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/id/{id}")
    public ResponseEntity<?> findById(@PathVariable String id) {
        try {
            UserLoginResponse userLoginResponse = userService.findById(id);
            return ResponseEntity.ok(userLoginResponse);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

}
