package com.project.chatbackend.controllers;

import com.project.chatbackend.requests.RefreshTokenRequest;
import com.project.chatbackend.requests.UseRegisterRequest;
import com.project.chatbackend.requests.UserLoginRequest;
import com.project.chatbackend.responses.LoginResponse;
import com.project.chatbackend.services.IUserService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {
    private final IUserService userService;

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody UserLoginRequest userLoginRequest, HttpServletRequest httpServletRequest) {
        try {
            String userAgent = httpServletRequest.getHeader("User-Agent");
            userLoginRequest.setMobile(userAgent.equals("mobile"));
            return ResponseEntity.ok(userService.login(userLoginRequest));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/register")
    public ResponseEntity<?> createUser(@RequestBody UseRegisterRequest useRegisterRequest) {
        try {
            userService.createUser(useRegisterRequest);
            return ResponseEntity.ok("create user successfully");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/refreshToken")
    public ResponseEntity<?> refreshToken(@RequestBody RefreshTokenRequest refreshTokenRequest) {
        try {
            LoginResponse loginResponse = userService.refreshToken(refreshTokenRequest.getRefreshToken());
            return ResponseEntity.ok(loginResponse);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("refresh token fail");
        }
    }

}
