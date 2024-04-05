package com.project.chatbackend.controllers;

import com.project.chatbackend.services.FileUpload;
import com.project.chatbackend.utils.UploadFile;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/v1/uploads")
@CrossOrigin(origins = "*", allowedHeaders = "*")
@RequiredArgsConstructor
public class UploadController {
    private final FileUpload fileUpload;
    @PostMapping("/upload")
    public String upload(@RequestBody MultipartFile multipartFile) {
        try {
            System.out.println("đã gưửi");
            return fileUpload.upload(List.of(multipartFile));
        } catch (Exception e) {
            System.out.println(e.getMessage());
            return "upload fail";
        }
    }
}
