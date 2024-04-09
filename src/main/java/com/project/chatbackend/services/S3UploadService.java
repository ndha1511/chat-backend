package com.project.chatbackend.services;


import com.project.chatbackend.models.Message;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.AwsCredentialsProvider;
import software.amazon.awssdk.core.sync.RequestBody;

import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;

import software.amazon.awssdk.services.s3.model.PutObjectRequest;


import java.io.IOException;

import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class S3UploadService {
    @Value("${amazon-properties.access-key}")
    private String accessKey;
    @Value("${amazon-properties.secret-key}")
    private String secretKey;
    @Value("${amazon-properties.bucket-name}")
    private String bucketName;
    @Value("${amazon-properties.region}")
    private String region;
    private final S3UploadAsync s3UploadAsync;


    public void uploadFile(MultipartFile file, Message message) throws IOException {
        log.info("start uploading at " + new Date(System.currentTimeMillis()));
        AwsCredentialsProvider credentialsProvider = () -> AwsBasicCredentials.create(accessKey, secretKey);
        String fileName = file.getOriginalFilename();
        String key = generateUniqueKey(fileName);
        S3Client s3Client = S3Client.builder().credentialsProvider(credentialsProvider).region(Region.AP_SOUTHEAST_1).build();
        PutObjectRequest request = PutObjectRequest.builder()
                        .bucket(bucketName).key(key).build();
        RequestBody requestBody = RequestBody.fromInputStream(file.getInputStream(),
                file.getInputStream().available());

        Map<String, String> fileInfo = new HashMap<>();
        fileInfo.put(key, "https://" + bucketName + ".s3." + region + ".amazonaws.com/" + key);
        assert fileName != null;
        s3UploadAsync.uploadToS3(message, request, requestBody, s3Client, fileInfo, fileName);
    }

    private String generateUniqueKey(String originalFileName) {
        // Tạo key duy nhất dựa trên tên file gốc hoặc sử dụng UUID
        // Ví dụ: return UUID.randomUUID().toString();
        return System.currentTimeMillis() + "_" + originalFileName;
    }



}
