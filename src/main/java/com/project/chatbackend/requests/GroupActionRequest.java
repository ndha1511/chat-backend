package com.project.chatbackend.requests;

import com.project.chatbackend.models.AddMembersPermission;
import com.project.chatbackend.models.SendMessagePermission;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class GroupActionRequest {
    String ownerId;
    String groupId;
    String adminId;
    String memberId;
    AddMembersPermission addMembersPermission;
    SendMessagePermission sendMessagePermission;
}
