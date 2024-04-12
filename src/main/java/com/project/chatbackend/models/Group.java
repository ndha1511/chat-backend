package com.project.chatbackend.models;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

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
    @Field(name = "group_name")
    private String groupName;
    private String avatar;
    private List<String> members;
    private String owner;
    private List<String> admins;
    @Field(name = "created_at")
    private LocalDateTime createdAt;
    @Field(name = "updated_at")
    private LocalDateTime updatedAt;
    private String url;
    @Field(name = "send_message")
    private boolean sendMessage;
    @Field(name = "number_of_members")
    private int numberOfMembers;
    @Field("add_members_permission")
    private AddMembersPermission addMembersPermission;
    @Field("send_message_permission")
    private SendMessagePermission sendMessagePermission;
    @Field("group_status")
    private GroupStatus groupStatus;
}
