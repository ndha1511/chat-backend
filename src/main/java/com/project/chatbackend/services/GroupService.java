package com.project.chatbackend.services;

import com.project.chatbackend.models.Group;
import com.project.chatbackend.repositories.GroupRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class GroupService implements IGroupService {
    private final GroupRepository groupRepository;
    private final IRoomService roomService;
    @Override
    public Group saveGroup(Group group) {
        group.setCreatedAt(LocalDateTime.now());
        group.setUpdatedAt(LocalDateTime.now());
        List<String> membersId = group.getMembers();
        if(!membersId.isEmpty()) {
            for (String memberId : membersId) {
                roomService.createRoomForGroup(memberId, group.getId());
            }
        }
        return groupRepository.save(group);
    }
}
