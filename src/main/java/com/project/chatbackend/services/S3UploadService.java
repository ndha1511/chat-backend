package com.project.chatbackend.services;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.amazonaws.services.s3.model.S3Object;
import com.amazonaws.services.s3.model.S3ObjectInputStream;
import com.amazonaws.util.IOUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

@Service
@RequiredArgsConstructor
@Slf4j
public class S3UploadService {
    @Value("${amazon-properties.bucket-name}")
    private String bucketName;
    @Value("${amazon-properties.region}")
    private String region;
    private final AmazonS3 s3Client;

    public Map<String, String> uploadFile(MultipartFile file) throws IOException {
        log.info("start uploading at " + new Date(System.currentTimeMillis()));
//        File fileObj = null;
//        try {
//            fileObj = convertMultiPartFileToFile(file);
//            String fileName = System.currentTimeMillis() + "_" + Objects.
//                    requireNonNull(file.getOriginalFilename())
//                    .replace(" ", "-");
//            s3Client.putObject(new PutObjectRequest(bucketName, fileName, fileObj));
//            fileObj.delete();
//            log.info("end upload at" + new Date(System.currentTimeMillis()));
//            Map<String, String> fileInfo = new HashMap<>();
//            fileInfo.put(fileName, "https://" + bucketName + ".s3." + region + ".amazonaws.com/" + fileName);
//            return fileInfo;
//        } catch (Exception io) {
//            assert fileObj != null;
//            fileObj.delete();
//            throw io;
//        }
        String fileName = file.getOriginalFilename();
        String key = generateUniqueKey(fileName);
        ObjectMetadata metadata = new ObjectMetadata();
        metadata.setContentLength(file.getSize());
        metadata.setContentType(file.getContentType());
        metadata.setLastModified(new Date());
        PutObjectRequest request = new PutObjectRequest(bucketName, key, file.getInputStream(), metadata);
        s3Client.putObject(request);

        log.info("File uploaded to S3 - Key: {}, Bucket: {}", key, bucketName);
        Map<String, String> fileInfo = new HashMap<>();
        fileInfo.put(key, "https://" + bucketName + ".s3." + region + ".amazonaws.com/" + key);
        return fileInfo;
    }

    private String generateUniqueKey(String originalFileName) {
        // Tạo key duy nhất dựa trên tên file gốc hoặc sử dụng UUID
        // Ví dụ: return UUID.randomUUID().toString();
        return System.currentTimeMillis() + "_" + originalFileName;
    }


    public byte[] downloadFile(String fileName) throws IOException {
        S3Object s3Object = s3Client.getObject(bucketName, fileName);
        S3ObjectInputStream inputStream = s3Object.getObjectContent();
        return IOUtils.toByteArray(inputStream);

    }


    public String deleteFile(String fileName) {
        s3Client.deleteObject(bucketName, fileName);
        return fileName + " removed ...";
    }


//    private File convertMultiPartFileToFile(MultipartFile file) throws IOException {
//        String tempDir = System.getProperty("java.io.tmpdir");
//        File convertedFile = new File(tempDir, Objects.requireNonNull(file.getOriginalFilename()));
//        try (FileOutputStream fos = new FileOutputStream(convertedFile)) {
//            fos.write(file.getBytes());
//        }
//        return convertedFile;
//    }
}
