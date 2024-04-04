package com.project.chatbackend.services;

import com.cloudinary.Cloudinary;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class FileUpload implements IFileUpload{
    private final Cloudinary cloudinary;
    @Override
    public String saveFile(MultipartFile multipartFile) throws IOException {
        System.out.println("đã gửi");
        if(!isImage(multipartFile))
            throw new IOException("file is not image");
        return cloudinary.uploader()
                .upload(multipartFile.getBytes(),
                        Map.of("public_id", UUID.randomUUID().toString()))
                .get("url")
                .toString();
    }
    @Override
    public  String upload(List<MultipartFile> files) throws Exception {
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
    private boolean isImage(MultipartFile file) {
        return file.getContentType().startsWith("image");
    }
}
