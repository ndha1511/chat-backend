package com.project.chatbackend.models;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class FileObject {
    private String filename;
    private String fileKey;
    private String fileExtension;
    private String filePath;


}
