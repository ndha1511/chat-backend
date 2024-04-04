package com.project.chatbackend.utils;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

public class UploadFile {

    public static String upload(List<MultipartFile> files) throws Exception {
        if(files != null) {
            for(MultipartFile file: files) {
                if(file.getSize() == 0)
                    continue;
                if(file.getSize() > 50 * 1024 * 1024) {
                    throw new Exception("file is too large! Maximum size is 50MB");
                }
                return saveFile(file);
            }
        }
        throw new Exception("upload file unsuccessfully");
    }

    private static String saveFile(MultipartFile file) throws Exception {
        String filename = StringUtils
                .cleanPath(Objects.requireNonNull(file.getOriginalFilename()));
        String uniqueFilename = UUID.randomUUID() + "_" + filename;
        Path uploadDir = Paths.get("uploads");
        if(!Files.exists(uploadDir)) {
            Files.createDirectories(uploadDir);
        }
        Path destination = Paths.get(uploadDir.toString(), uniqueFilename);
        Files.copy(file.getInputStream(), destination, StandardCopyOption.REPLACE_EXISTING);
        return uniqueFilename;
    }


}
