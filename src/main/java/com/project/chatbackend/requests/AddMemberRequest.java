package com.project.chatbackend.requests;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class AddMemberRequest {
    private String adderId;
    private List<String> membersId;
    private String groupId;
}
