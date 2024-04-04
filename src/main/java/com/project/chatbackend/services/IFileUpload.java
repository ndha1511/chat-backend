package com.project.chatbackend.services;

import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

public interface IFileUpload {
    String saveFile(MultipartFile multipartFile) throws IOException;
    String upload(List<MultipartFile> files) throws Exception;
}
