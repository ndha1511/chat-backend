package com.project.chatbackend.controllers;

import com.project.chatbackend.requests.ChangePasswordRequest;
import com.project.chatbackend.responses.UserLoginResponse;
import com.project.chatbackend.services.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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

    @GetMapping("/email/{email}")
    public ResponseEntity<?> findByEmail(@PathVariable String email) {
        try {
            return ResponseEntity.ok(userService.findByEmail(email));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e);
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

    @PostMapping("/changePassword")
    public ResponseEntity<?> changePassword(@Valid @RequestBody ChangePasswordRequest changePasswordRequest,
                                            BindingResult result) {
        if (result.hasErrors()) {
            List<String> errMessages = result.getFieldErrors()
                    .stream()
                    .map(DefaultMessageSourceResolvable::getDefaultMessage)
                    .toList();
            return ResponseEntity.badRequest().body(errMessages);
        }
        if(!changePasswordRequest.getNewPassword().equals(changePasswordRequest.getConfirmNewPassword())) {
            return ResponseEntity.badRequest().body("confirm password incorrect");
        }
        if(changePasswordRequest.getOldPassword().equals(changePasswordRequest.getNewPassword())) {
            return ResponseEntity.badRequest().body("new password like old password");
        }
        try {
            boolean rs = userService.changePassword(changePasswordRequest);
            if(rs) return ResponseEntity.ok("change password successfully pls re login");
            return ResponseEntity.badRequest().body("change password fail");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e);
        }
    }

}
