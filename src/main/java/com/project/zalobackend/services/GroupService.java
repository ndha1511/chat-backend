package com.project.zalobackend.services;

import com.project.zalobackend.models.Group;
import com.project.zalobackend.repositories.GroupRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class GroupService implements IGroupService {
    private final GroupRepository groupRepository;
    @Override
    public Group createGroup(Group group) {
        return groupRepository.save(group);
    }
}
