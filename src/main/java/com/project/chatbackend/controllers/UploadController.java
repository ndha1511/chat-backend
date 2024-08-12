package com.project.chatbackend.controllers;

import com.project.chatbackend.services.S3UploadService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Objects;


@RestController
@RequestMapping("/api/v1/uploads")
@CrossOrigin(origins = "*", allowedHeaders = "*")
@RequiredArgsConstructor
public class UploadController {
    private final S3UploadService s3UploadService;
    @PostMapping("/upload")
    public ResponseEntity<String> upload(@RequestParam("file") MultipartFile multipartFile) {
        try {
            if(multipartFile.getSize() > 10 * 1024 * 1024) {
                return ResponseEntity.status(412).body("file is too large! Maximum size is 10MB");
            }
            if(!Objects.requireNonNull(multipartFile.getContentType()).startsWith("image/")) {
                return ResponseEntity.status(413).body("file is not an image");
            }
            return ResponseEntity.ok(s3UploadService.uploadFileSync(multipartFile));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("upload fail");
        }
    }
}
