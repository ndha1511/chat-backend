package com.project.chatbackend.services;

import com.amazonaws.services.s3.AmazonS3;
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
        File fileObj = null;
        try {
            fileObj = convertMultiPartFileToFile(file);
            String fileName = System.currentTimeMillis() + "_" + Objects.
                    requireNonNull(file.getOriginalFilename())
                    .replace(" ", "-");
            s3Client.putObject(new PutObjectRequest(bucketName, fileName, fileObj));
            fileObj.delete();
            log.info("end upload at" + new Date(System.currentTimeMillis()));
            Map<String, String> fileInfo = new HashMap<>();
            fileInfo.put(fileName, "https://" + bucketName + ".s3." + region + ".amazonaws.com/" + fileName);
            return fileInfo;
        } catch (Exception io) {
            assert fileObj != null;
            fileObj.delete();
            throw io;
        }
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


    private File convertMultiPartFileToFile(MultipartFile file) throws IOException {
        File convertedFile = new File(Objects.requireNonNull(file.getOriginalFilename()));
        try (FileOutputStream fos = new FileOutputStream(convertedFile)) {
            fos.write(file.getBytes());
        } catch (Exception ex) {
            convertedFile.delete();
            throw ex;
        }
        return convertedFile;
    }
}
