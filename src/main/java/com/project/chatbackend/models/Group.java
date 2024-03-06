package com.project.chatbackend.models;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.List;

@Document(collection = "groups")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Group {
    @Id
    private String id;
    private String name;
    private String avatar;
    private List<String> members;
    private String owner;
    private List<String> admins;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String url;
    private boolean sendMessage;
    private int numberOfMembers;
}
