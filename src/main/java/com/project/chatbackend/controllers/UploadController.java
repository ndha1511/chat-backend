package com.project.chatbackend.controllers;

import com.project.chatbackend.utils.UploadFile;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/v1/uploads")
public class UploadController {

    @PostMapping("/upload")
    public String upload(@RequestBody MultipartFile multipartFile) {
        try {
            return UploadFile.upload(List.of(multipartFile));
        } catch (Exception e) {
            return "upload fail";
        }
    }
}
