package com.project.chatbackend.controllers;

import com.project.chatbackend.services.S3UploadService;
import jakarta.websocket.server.PathParam;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/files")
public class FileController {
    private final S3UploadService s3UploadService;

    @GetMapping("/download/{fileKey}")
    public ResponseEntity<?> downloadFile(@PathVariable String fileKey) {
        try {
            byte[] data = s3UploadService.downloadFile(fileKey);
            ByteArrayResource resource = new ByteArrayResource(data);
            return ResponseEntity
                    .ok()
                    .contentLength(data.length)
                    .header("Content-type", "application/octet-stream")
                    .header("Content-disposition", "attachment; filename=\"" + fileKey + "\"")
                    .body(resource);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("download file fail");
        }
    }

}
