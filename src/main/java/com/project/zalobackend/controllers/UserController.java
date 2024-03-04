package com.project.zalobackend.controllers;

import com.project.zalobackend.requests.UseRegisterRequest;
import com.project.zalobackend.services.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;



}
