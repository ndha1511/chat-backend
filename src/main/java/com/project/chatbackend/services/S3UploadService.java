package com.project.chatbackend.services;


import com.project.chatbackend.exceptions.MaxFileSizeException;
import com.project.chatbackend.models.Message;
import com.project.chatbackend.responses.ProgressNotify;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.AwsCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3AsyncClient;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.transfer.s3.S3TransferManager;
import software.amazon.awssdk.transfer.s3.SizeConstant;
import software.amazon.awssdk.transfer.s3.model.CompletedFileUpload;
import software.amazon.awssdk.transfer.s3.model.FileUpload;
import software.amazon.awssdk.transfer.s3.model.UploadFileRequest;
import software.amazon.awssdk.transfer.s3.progress.LoggingTransferListener;
import software.amazon.awssdk.transfer.s3.progress.TransferListener;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
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
    private final SimpMessagingTemplate messagingTemplate;



    public void uploadFile(MultipartFile file, Message message) throws IOException, MaxFileSizeException {
        if(file.getSize() > 500 * 1024 * 1024) {
            throw new MaxFileSizeException("file is too large! Maximum size is 500MB");
        }
        AwsCredentialsProvider credentialsProvider = () -> AwsBasicCredentials.create(accessKey, secretKey);
        String fileName = file.getOriginalFilename();
        String key = generateUniqueKey(fileName);
        S3AsyncClient s3AsyncClient = S3AsyncClient.crtBuilder()
                .credentialsProvider(credentialsProvider)
                .region(Region.AP_SOUTHEAST_1)
                .targetThroughputInGbps(20.0)
                .minimumPartSizeInBytes(10 * SizeConstant.MB)
                .build();
        S3TransferManager transferManager = S3TransferManager.builder()
                .s3Client(s3AsyncClient)
                .build();
        TransferListener transferListener = new TransferListener() {
            @Override
            public void bytesTransferred(Context.BytesTransferred context) {
                log.info("bytes uploaded: {}", context.progressSnapshot().transferredBytes());
                ProgressNotify progressNotify = ProgressNotify.builder()
                        .id(message.getId())
                        .bytesTransferred(context.progressSnapshot().transferredBytes())
                        .build();
                messagingTemplate.convertAndSendToUser(message.getSenderId(), "/queue/progress", progressNotify);
            }

        };
        Path tempFile = Files.createTempFile("temp", file.getOriginalFilename());
        Files.copy(file.getInputStream(), tempFile, StandardCopyOption.REPLACE_EXISTING);
        PutObjectRequest request = PutObjectRequest.builder()
                        .bucket(bucketName).key(key).build();
        UploadFileRequest uploadFileRequest = UploadFileRequest.builder()
                .putObjectRequest(request)
                .addTransferListener(transferListener)
                .source(tempFile)
                .build();
        Map<String, String> fileInfo = new HashMap<>();
        fileInfo.put(key, "https://" + bucketName + ".s3." + region + ".amazonaws.com/" + key);
        assert fileName != null;
        s3UploadAsync.uploadToS3(message, transferManager, uploadFileRequest, fileInfo, fileName, file.getSize());
    }

    private String generateUniqueKey(String originalFileName) {
        return System.currentTimeMillis() + "_" + originalFileName;
    }

    public String uploadFileSync(MultipartFile file) throws IOException {
        AwsCredentialsProvider credentialsProvider = () -> AwsBasicCredentials.create(accessKey, secretKey);
        String fileName = file.getOriginalFilename();
        String key = generateUniqueKey(fileName);
        S3AsyncClient s3AsyncClient = S3AsyncClient.crtBuilder()
                .credentialsProvider(credentialsProvider)
                .region(Region.AP_SOUTHEAST_1)
                .targetThroughputInGbps(20.0)
                .minimumPartSizeInBytes(10 * SizeConstant.MB)
                .build();
        S3TransferManager transferManager = S3TransferManager.builder()
                .s3Client(s3AsyncClient)
                .build();
        Path tempFile = Files.createTempFile("temp", file.getOriginalFilename());
        Files.copy(file.getInputStream(), tempFile, StandardCopyOption.REPLACE_EXISTING);
        PutObjectRequest request = PutObjectRequest.builder()
                .bucket(bucketName).key(key).build();
        UploadFileRequest uploadFileRequest = UploadFileRequest.builder()
                .putObjectRequest(request)
                .addTransferListener(LoggingTransferListener.create())
                .source(tempFile)
                .build();
        FileUpload fileUpload = transferManager.uploadFile(uploadFileRequest);
        CompletedFileUpload uploadResult = fileUpload.completionFuture().join();
        log.info(uploadResult.response().eTag());
        return "https://" + bucketName + ".s3." + region + ".amazonaws.com/" + key;
    }



}
