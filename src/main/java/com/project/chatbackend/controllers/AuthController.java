package com.project.chatbackend.controllers;

import com.project.chatbackend.requests.*;
import com.project.chatbackend.responses.LoginResponse;
import com.project.chatbackend.services.IOtpService;
import com.project.chatbackend.services.IUserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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
            return switch (result) {
                case "expired" -> ResponseEntity.badRequest().body("expired");
                case "not exist" -> ResponseEntity.badRequest().body("not exist");
                case "invalid" -> ResponseEntity.badRequest().body("invalid");
                default -> ResponseEntity.ok("valid");
            };
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/register")
    public ResponseEntity<?> createUser(@Valid @RequestBody UseRegisterRequest useRegisterRequest,
                                        BindingResult result) {
        try {
            if (result.hasErrors()) {
                List<String> errMessages = result.getFieldErrors()
                        .stream()
                        .map(DefaultMessageSourceResolvable::getDefaultMessage)
                        .toList();
                return ResponseEntity.badRequest().body(errMessages);
            }
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

    @PostMapping("/sendOtpResetPassword")
    public ResponseEntity<?> sendOtpResetPassword(@RequestBody OtpForResetPwsRequest otp) {
        try {
           boolean rs = otpService.sendOTPForResetPassword(otp);
           if(rs) return ResponseEntity.ok("send otp successfully");
           return ResponseEntity.badRequest().body("send otp fail");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e);
        }
    }

    @PostMapping("/validOtp")
    public ResponseEntity<?> validOtp(@RequestBody OtpValidRequest otpValidRequest) {
        try {
            boolean rs = userService.isValidOTP(otpValidRequest);
            if(rs) return ResponseEntity.ok("otp is valid");
            return ResponseEntity.badRequest().body("otp is invalid");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e);
        }
    }

    @PostMapping("/resetPassword")
    public ResponseEntity<?> resetPassword(@RequestBody ResetPasswordRequest resetPasswordRequest) {
        try {
            boolean rs = userService.resetPassword(resetPasswordRequest);
            if(rs) return ResponseEntity.ok("reset password successfully pls re login");
            return ResponseEntity.badRequest().body("reset password fail");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e);
        }
    }



}
