package com.project.chatbackend.controllers;

import com.project.chatbackend.requests.*;
import com.project.chatbackend.responses.LoginResponse;
import com.project.chatbackend.services.IOtpService;
import com.project.chatbackend.services.IUserService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@CrossOrigin(origins = "*", allowedHeaders = "*")
public class AuthController {
    private final IUserService userService;
    private final IOtpService otpService;

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
    @PostMapping("/sendOtp")
    public ResponseEntity<?> sendOtp(@RequestBody OtpRequest otpRequest) {
        try {
            if(otpService.sendOTP(otpRequest)){
                return ResponseEntity.ok("send otp successfully");
            }else{
                return ResponseEntity.badRequest().body("send otp fail");
            }
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/verifyOtp")
    public ResponseEntity<?> verifyOtp(@RequestBody OtpValidRequest otpValidRequest) {
        try {
            String result = otpService.verifyOTP(otpValidRequest);
            if(result.equals("expired")){
                return ResponseEntity.badRequest().body("expired");
            }else if(result.equals("not exist")){
                return ResponseEntity.badRequest().body("not exist");
            }else if(result.equals("invalid")){
                return ResponseEntity.badRequest().body("invalid");
            }else{
                return ResponseEntity.ok("valid");
            }
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
