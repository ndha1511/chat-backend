package com.project.chatbackend.requests;

import lombok.Builder;
import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Data
@Builder
public class CreateGroupRequest {
    private String groupName;
    private String ownerId;
    private String ownerName;
    private MultipartFile avatar;
    private List<String> membersId;
}
