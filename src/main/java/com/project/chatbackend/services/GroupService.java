package com.project.chatbackend.services;

import com.project.chatbackend.exceptions.DataExistsException;
import com.project.chatbackend.exceptions.DataNotFoundException;
import com.project.chatbackend.exceptions.PermissionAccessDenied;
import com.project.chatbackend.models.*;
import com.project.chatbackend.repositories.GroupRepository;
import com.project.chatbackend.repositories.MessageRepository;
import com.project.chatbackend.repositories.RoomRepository;
import com.project.chatbackend.repositories.UserRepository;
import com.project.chatbackend.responses.UserNotify;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

@Service
@RequiredArgsConstructor
public class GroupService implements IGroupService {
    private final UserRepository userRepository;
    private final GroupRepository groupRepository;
    private final IRoomService roomService;
    private final S3UploadService s3UploadService;
    private final MessageRepository messageRepository;
    private final SimpMessagingTemplate simpMessagingTemplate;
    private final RoomRepository roomRepository;

    @Override
    public Room createGroup(String groupName,
                            String ownerId,
                            String ownerName,
                            List<String> membersId,
                            MultipartFile file) throws IOException, DataNotFoundException {
        String filePath = "";
        if(file.getSize() > 0) {
            filePath = s3UploadService.uploadFileSync(file);
        }
        Group group = Group.builder()
                .groupName(groupName)
                .avatar(filePath)
                .members(membersId)
                .owner(ownerId)
                .createdAt(LocalDateTime.now())
                .sendMessagePermission(SendMessagePermission.PUBLIC)
                .addMembersPermission(AddMembersPermission.PUBLIC)
                .numberOfMembers(membersId.size())
                .build();
        groupRepository.save(group);
        // add message type system
        Message message = Message.builder()
                .content(ownerName + " đã tạo nhóm")
                .messageType(MessageType.SYSTEM)
                .sendDate(LocalDateTime.now())
                .roomId(group.getId())
                .build();
        messageRepository.save(message);
        Room roomOwner = null;
        // create room for members
        for (String memberId: membersId) {
            if(!userRepository.existsByEmail(memberId)) throw new DataNotFoundException("member not exists");
            Room room = roomService.createRoomForGroup(memberId, group.getId());
            room.setTime(LocalDateTime.now());
            room.setSender(true);
            room.setLatestMessage(message.getContent().toString());
            roomRepository.save(room);
            if(room.getSenderId().equals(ownerId)) roomOwner = room;
            UserNotify userNotify = UserNotify.builder()
                    .message(message)
                    .status("CREAT_GROUP")
                    .room(room)
                    .build();
            // notify
            simpMessagingTemplate.convertAndSendToUser(
                    memberId, "queue/message",
                    userNotify
            );
        }
        return roomOwner;
    }

    @Override
    @Transactional
    public void addMemberToGroup(List<String> membersId, String adderId,
                                 String groupId) throws DataNotFoundException, DataExistsException, PermissionAccessDenied {

        Optional<Group> optionalGroup = groupRepository.findById(groupId);
        Group group = getGroup(adderId, optionalGroup);
        List<String> membersInGroup = group.getMembers();
        // check exists member
        for (String memberId: membersId) {
            if(!userRepository.existsByEmail(memberId))
                throw new DataNotFoundException("Member is not exists");
            if(membersInGroup.contains(memberId))
                throw new DataExistsException("Member is exist in group memberId: " + memberId);

        }
        if(!membersInGroup.contains(adderId))
            throw new DataNotFoundException("adder is not in group");
        List<String> newMembers = Stream.concat(membersInGroup.stream(), membersId.stream()).toList();
        group.setMembers(newMembers);
        group.setNumberOfMembers(newMembers.size());
        group.setUpdatedAt(LocalDateTime.now());
        groupRepository.save(group);

        User adder = userRepository.findByEmail(adderId).orElseThrow();

        Message messageLatest = null;
        // create room for users
        int index = 0;
        for(String memberId: membersId) {
            User newMember = userRepository.findByEmail(memberId).orElseThrow();
            Message message = Message.builder()
                    .content(newMember.getName() + " đã được " + adder.getName() + " thêm vào nhóm")
                    .messageType(MessageType.SYSTEM)
                    .sendDate(LocalDateTime.now())
                    .roomId(group.getId())
                    .build();
            messageRepository.save(message);
            if(index == membersId.size() - 1)
                messageLatest = message;
            Room room = roomService.createRoomForGroup(memberId, group.getId());
            room.setLatestMessage(message.getContent().toString());
            room.setTime(LocalDateTime.now());
            room.setSender(true);
            roomRepository.save(room);

            // notify for users
            UserNotify userNotify = UserNotify.builder()
                    .message(message)
                    .status("ADD_MEMBER")
                    .room(room)
                    .build();
            simpMessagingTemplate.convertAndSendToUser(
                    memberId, "queue/message",
                    userNotify
            );
            ++index;

        }


        for(String memberInGroup : membersInGroup) {
            // cập nhật chat room của các thành viên đó
            Room room = roomRepository
                    .findBySenderIdAndReceiverId(memberInGroup, groupId)
                    .orElseThrow();
            room.setTime(LocalDateTime.now());
            assert messageLatest != null;
            room.setLatestMessage(messageLatest.getContent().toString());
            roomRepository.save(room);

        }
        // thông báo đến các thành viên còn lại trong group
        UserNotify userNotify = UserNotify.builder()
                .status("ADD_MEMBER")
                .build();
        simpMessagingTemplate.convertAndSendToUser(
                groupId, "queue/message",
                userNotify
        );


    }

    private Group getGroup(String adderId, Optional<Group> optionalGroup) throws PermissionAccessDenied {
        Group group = optionalGroup.orElseThrow();
        AddMembersPermission addMembersPermission = group.getAddMembersPermission();
        if(addMembersPermission.equals(AddMembersPermission.ONLY_ADMIN)) {
            List<String> admins = group.getAdmins();
            if(!admins.contains(adderId) && !group.getOwner().equals(adderId))
                throw new PermissionAccessDenied("only admin or owner has the right to add member");
        }
        if(addMembersPermission.equals(AddMembersPermission.ONLY_OWNER)) {
            if(!group.getOwner().equals(adderId))
                throw new PermissionAccessDenied("only owner has the right to add member");

        }
        return group;
    }
}
