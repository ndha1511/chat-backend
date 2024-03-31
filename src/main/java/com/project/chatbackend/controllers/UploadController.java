package com.project.chatbackend.controllers;

import com.project.chatbackend.utils.UploadFile;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/v1/uploads")
@CrossOrigin(origins = "*", allowedHeaders = "*")
public class UploadController {

    @PostMapping("/upload")
    public String upload(@RequestBody MultipartFile multipartFile) {
        try {
            System.out.println("đã gưửi");
            return UploadFile.upload(List.of(multipartFile));
        } catch (Exception e) {
            return "upload fail";
        }
    }
}
