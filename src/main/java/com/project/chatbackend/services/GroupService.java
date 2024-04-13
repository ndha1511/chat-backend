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
import java.time.DateTimeException;
import java.time.LocalDateTime;
import java.util.ArrayList;
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
    @Transactional
    public Room createGroup(String groupName,
                            String ownerId,
                            String ownerName,
                            List<String> membersId,
                            MultipartFile file) throws IOException, DataNotFoundException {
        String filePath = "";
        if(file != null) {
            filePath = s3UploadService.uploadFileSync(file);
        }
        membersId.add(ownerId);
        Group group = Group.builder()
                .groupName(groupName)
                .avatar(filePath)
                .members(membersId)
                .owner(ownerId)
                .createdAt(LocalDateTime.now())
                .groupStatus(GroupStatus.ACTIVE)
                .sendMessagePermission(SendMessagePermission.PUBLIC)
                .addMembersPermission(AddMembersPermission.PUBLIC)
                .numberOfMembers(membersId.size())
                .build();
        groupRepository.save(group);
        // add message type system
        Message message = Message.builder()
                .content(ownerName + " đã tạo nhóm")
                .messageType(MessageType.SYSTEM)
                .senderId("system@gmail.com")
                .messageStatus(MessageStatus.SENT)
                .sendDate(LocalDateTime.now())
                .roomId(group.getId())
                .build();
        messageRepository.save(message);
        Room roomOwner = null;
        // create room for members
        LocalDateTime time = LocalDateTime.now();
        for (String memberId: membersId) {
            if(!userRepository.existsByEmail(memberId)) throw new DataNotFoundException("member not exists");
            Room room = roomService.createRoomForGroup(memberId, group.getId());
            room.setTime(time);
            room.setSender(false);
            room.setLatestMessage(message.getContent().toString());
            roomRepository.save(room);
            if(room.getSenderId().equals(ownerId)) roomOwner = room;
            UserNotify userNotify = UserNotify.builder()
                    .message(message)
                    .status("CREATE_GROUP")
                    .room(room)
                    .build();
            // notify
            simpMessagingTemplate.convertAndSendToUser(
                    memberId, "queue/messages",
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
                    .senderId("system@gmail.com")
                    .messageStatus(MessageStatus.SENT)
                    .roomId(group.getId())
                    .build();
            messageRepository.save(message);
            if(index == membersId.size() - 1)
                messageLatest = message;
            Room room = roomService.createRoomForGroup(memberId, group.getId());
            room.setLatestMessage(message.getContent().toString());
            room.setTime(LocalDateTime.now());
            room.setSender(false);
            roomRepository.save(room);

            // notify for users
            UserNotify userNotify = UserNotify.builder()
                    .message(message)
                    .status("ADD_MEMBER")
                    .room(room)
                    .build();
            simpMessagingTemplate.convertAndSendToUser(
                    memberId, "queue/messages",
                    userNotify
            );
            ++index;

        }

        LocalDateTime time = LocalDateTime.now();
        for(String memberInGroup : membersInGroup) {
            // cập nhật chat room của các thành viên đó
            Room room = roomRepository
                    .findBySenderIdAndReceiverId(memberInGroup, groupId)
                    .orElseThrow();
            room.setTime(time);
            assert messageLatest != null;
            room.setLatestMessage(messageLatest.getContent().toString());
            roomRepository.save(room);

        }
        // thông báo đến các thành viên còn lại trong group
        UserNotify userNotify = UserNotify.builder()
                .status("ADD_MEMBER_GROUP")
                .build();
        simpMessagingTemplate.convertAndSendToUser(
                groupId, "queue/messages",
                userNotify
        );


    }

    @Override
    @Transactional
    public void removeMember(String memberId, String adminId, String groupId) throws DataNotFoundException, PermissionAccessDenied {
        // find group
        Optional<Group> optionalGroup = groupRepository.findById(groupId);
        if (optionalGroup.isEmpty()) throw new DataNotFoundException("group not found");
        Group group = optionalGroup.get();
        List<String> admins = group.getAdmins();
        AddMembersPermission addMembersPermission = group.getAddMembersPermission();
        if (addMembersPermission.equals(AddMembersPermission.ONLY_OWNER)) {
            if (!adminId.equals(group.getOwner()))
                throw new PermissionAccessDenied("only owner have to delete member to group");
        }
        if (!admins.contains(adminId) && !group.getOwner().equals(adminId))
            throw new PermissionAccessDenied("only admins or owner have to delete member to group");
        List<String> members = group.getMembers();

        User memberDelete = userRepository.findByEmail(memberId).orElseThrow(() -> new DateTimeException("member not found"));
        User admin = userRepository.findByEmail(adminId).orElseThrow(() -> new DateTimeException("member not found"));
        // save message system
        Message message = Message.builder()
                .content(memberDelete.getName() + " đã bị " + admin.getName() + " xóa khỏi nhóm")
                .messageType(MessageType.SYSTEM)
                .sendDate(LocalDateTime.now())
                .senderId("system@gmail.com")
                .messageStatus(MessageStatus.SENT)
                .roomId(group.getId())
                .build();
        messageRepository.save(message);

        // update room for members
        LocalDateTime time = LocalDateTime.now();
        Room roomRemove = null;
        for (String memberInGroup : members) {
            // cập nhật chat room của các thành có trong nhóm
            Room room = roomRepository
                    .findBySenderIdAndReceiverId(memberInGroup, groupId)
                    .orElseThrow();
            room.setTime(time);
            room.setLatestMessage(message.getContent().toString());
            roomRepository.save(room);
            if(memberDelete.getEmail().equals(memberInGroup)) {
                roomRemove = room;
                roomRemove.setRoomStatus(RoomStatus.INACTIVE);
                roomRepository.save(room);
            }


        }

        // update group
        members.remove(memberId);
        group.setMembers(members);
        group.setNumberOfMembers(members.size());
        group.setUpdatedAt(LocalDateTime.now());
        groupRepository.save(group);

        // notify to user remove
        UserNotify userNotifyUser = UserNotify.builder()
                .status("REMOVE_MEMBER")
                .room(roomRemove)
                .build();
        simpMessagingTemplate.convertAndSendToUser(
                memberId, "/queue/messages", userNotifyUser
        );

        // notify to group
        UserNotify userNotify = UserNotify.builder()
                .status("REMOVE_MEMBER_GROUP")
                .build();
        simpMessagingTemplate.convertAndSendToUser(
                groupId, "/queue/messages", userNotify
        );
    }

    @Override
    public Group findById(String id) throws DataNotFoundException {
        return groupRepository.findById(id).orElseThrow(() -> new DataNotFoundException("not found"));
    }

    @Override
    @Transactional
    public void removeGroup(String ownerId, String groupId) throws DataNotFoundException, PermissionAccessDenied {
        Optional<Group> optionalGroup = groupRepository.findById(groupId);
        if(optionalGroup.isEmpty()) throw new DataNotFoundException("group not found");
        Group group = optionalGroup.get();
        if(!ownerId.equals(group.getOwner())) throw new PermissionAccessDenied("permission access denied");
        group.setGroupStatus(GroupStatus.INACTIVE);
        Message message = Message.builder()
                .content("trưởng nhóm đã giải tán nhóm")
                .messageType(MessageType.SYSTEM)
                .sendDate(LocalDateTime.now())
                .senderId("system@gmail.com")
                .messageStatus(MessageStatus.SENT)
                .roomId(group.getId())
                .build();
        messageRepository.save(message);
        List<String> members = group.getMembers();

        LocalDateTime time = LocalDateTime.now();
        Room roomLatest = null;
        for (String memberId: members) {
            Room room = roomRepository
                    .findBySenderIdAndReceiverId(memberId, groupId)
                    .orElseThrow();
            room.setTime(time);
            room.setLatestMessage(message.getContent().toString());
            room.setNumberOfUnreadMessage(0);
            room.setRoomStatus(RoomStatus.INACTIVE);
            roomRepository.save(room);
            roomLatest = room;
        }

        // update group
        group.setUpdatedAt(time);
        groupRepository.save(group);

        // notify to group
        UserNotify userNotify = UserNotify.builder()
                .status("REMOVE_GROUP")
                .room(roomLatest)
                .build();
        simpMessagingTemplate.convertAndSendToUser(
                groupId, "/queue/messages", userNotify
        );

    }

    @Override
    @Transactional
    public void addAdmin(String ownerId, String adminId, String groupId) throws DataNotFoundException, PermissionAccessDenied, DataExistsException {
        Optional<Group> optionalGroup = groupRepository.findById(groupId);
        if(optionalGroup.isEmpty()) throw new DataNotFoundException("group not found");
        // kiểm tra quyền owner
        Group group = optionalGroup.get();
        if(!ownerId.equals(group.getOwner())) throw new PermissionAccessDenied("only owner have to add admin");
        List<String> admins = group.getAdmins();
        List<String> members = group.getMembers();
        if(!members.contains(adminId)) throw new PermissionAccessDenied("this user is not member in group");
        if(admins.contains(adminId)) throw new DataExistsException("admin is exist");
        admins.add(adminId);
        group.setAdmins(admins);
        LocalDateTime time = LocalDateTime.now();
        group.setUpdatedAt(time);

        User owner = userRepository.findByEmail(ownerId).orElseThrow(() -> new DataNotFoundException("owner not found"));
        User admin = userRepository.findByEmail(adminId).orElseThrow(() -> new DataNotFoundException("admin not found"));

        Message message = Message.builder()
                .content(admin.getName() + " đã được " + owner.getName() + " phong làm phó nhóm")
                .messageType(MessageType.SYSTEM)
                .sendDate(LocalDateTime.now())
                .senderId("system@gmail.com")
                .messageStatus(MessageStatus.SENT)
                .roomId(group.getId())
                .build();
        messageRepository.save(message);

        // update room for members
        for (String memberId: members) {
            Room room = roomRepository
                    .findBySenderIdAndReceiverId(memberId, groupId)
                    .orElseThrow();
            room.setTime(time);
            room.setLatestMessage(message.getContent().toString());
            roomRepository.save(room);
        }

        groupRepository.save(group);

        // notify to group
        UserNotify userNotify = UserNotify.builder()
                .status("ADD_ADMIN")
                .build();
        simpMessagingTemplate.convertAndSendToUser(
                groupId, "/queue/messages", userNotify
        );


    }

    @Override
    @Transactional
    public void removeAdmin(String ownerId, String adminId, String groupId) throws DataNotFoundException, PermissionAccessDenied {
        Optional<Group> optionalGroup = groupRepository.findById(groupId);
        if(optionalGroup.isEmpty()) throw new DataNotFoundException("group not found");
        Group group = optionalGroup.get();
        if(!ownerId.equals(group.getOwner())) throw new PermissionAccessDenied("only owner have to delete admin");
        List<String> admins = group.getAdmins();
        List<String> members = group.getMembers();

        admins.remove(adminId);
        group.setAdmins(admins);
        LocalDateTime time = LocalDateTime.now();
        group.setUpdatedAt(time);

        User owner = userRepository.findByEmail(ownerId).orElseThrow(() -> new DataNotFoundException("owner not found"));
        User admin = userRepository.findByEmail(adminId).orElseThrow(() -> new DataNotFoundException("admin not found"));

        Message message = Message.builder()
                .content(admin.getName() + " đã được " + owner.getName() + " giáng chức làm thành viên")
                .messageType(MessageType.SYSTEM)
                .sendDate(LocalDateTime.now())
                .senderId("system@gmail.com")
                .messageStatus(MessageStatus.SENT)
                .roomId(group.getId())
                .build();
        messageRepository.save(message);

        // update room for members
        for (String memberId: members) {
            Room room = roomRepository
                    .findBySenderIdAndReceiverId(memberId, groupId)
                    .orElseThrow();
            room.setTime(time);
            room.setLatestMessage(message.getContent().toString());
            roomRepository.save(room);
        }

        groupRepository.save(group);

        // notify to group
        UserNotify userNotify = UserNotify.builder()
                .status("REMOVE_ADMIN")
                .build();
        simpMessagingTemplate.convertAndSendToUser(
                groupId, "/queue/messages", userNotify
        );
    }

    @Override
    public void updateAddMemberPermission(String ownerId, String groupId, AddMembersPermission addMembersPermission)
            throws DataNotFoundException, PermissionAccessDenied {
        Optional<Group> optionalGroup = groupRepository.findById(groupId);
        if(optionalGroup.isEmpty()) throw new DataNotFoundException("group not found");
        Group group = optionalGroup.get();
        if(!ownerId.equals(group.getOwner())) throw new PermissionAccessDenied("only owner have to update permission");
        group.setAddMembersPermission(addMembersPermission);
        group.setUpdatedAt(LocalDateTime.now());
        groupRepository.save(group);

        // notify to group
        UserNotify userNotify = UserNotify.builder()
                .status("UPDATE_ADD_MEMBER_PERMISSION")
                .build();
        simpMessagingTemplate.convertAndSendToUser(
                groupId, "/queue/messages", userNotify
        );
    }

    @Override
    public void updateSendMessagePermission(String ownerId, String groupId, SendMessagePermission sendMessagePermission) throws DataNotFoundException, PermissionAccessDenied {
        Optional<Group> optionalGroup = groupRepository.findById(groupId);
        if(optionalGroup.isEmpty()) throw new DataNotFoundException("group not found");
        Group group = optionalGroup.get();
        if(!ownerId.equals(group.getOwner())) throw new PermissionAccessDenied("only owner have to update permission");
        group.setSendMessagePermission(sendMessagePermission);
        group.setUpdatedAt(LocalDateTime.now());
        groupRepository.save(group);

        // notify to group
        UserNotify userNotify = UserNotify.builder()
                .status("UPDATE_ADD_SEND_MESSAGE_PERMISSION")
                .build();
        simpMessagingTemplate.convertAndSendToUser(
                groupId, "/queue/messages", userNotify
        );
    }

    @Override
    @Transactional
    public void leaveGroup(String memberId, String groupId) throws DataNotFoundException, PermissionAccessDenied {
        Optional<Group> optionalGroup = groupRepository.findById(groupId);
        if(optionalGroup.isEmpty()) throw new DataNotFoundException("group not found");
        Group group = optionalGroup.get();
        if(group.getOwner().equals(memberId)) throw new PermissionAccessDenied("owner can't leave group");
        List<String> members = group.getMembers();
        if(!members.contains(memberId)) throw new PermissionAccessDenied("you are not member in group");
        members.remove(memberId);
        group.setMembers(members);
        List<String> admins = group.getAdmins();
        admins.remove(memberId);
        group.setAdmins(admins);
        group.setNumberOfMembers(members.size());
        LocalDateTime time = LocalDateTime.now();
        group.setUpdatedAt(time);
        groupRepository.save(group);
        User member = userRepository.findByEmail(memberId).orElseThrow(() -> new DataNotFoundException("user not found"));
        Message message = Message.builder()
                .content(member.getName() + " đã rời nhóm")
                .messageType(MessageType.SYSTEM)
                .sendDate(LocalDateTime.now())
                .senderId("system@gmail.com")
                .messageStatus(MessageStatus.SENT)
                .roomId(group.getId())
                .build();
        messageRepository.save(message);
        Room roomLeave = null;
        // update room for members
        for (String memberGroupId: members) {
            Room room = roomRepository
                    .findBySenderIdAndReceiverId(memberGroupId, groupId)
                    .orElseThrow();
            if(room.getSenderId().equals(memberId)) {
                room.setRoomStatus(RoomStatus.INACTIVE);
                room.setLatestMessage("Bạn đã rời nhóm");
                room.setNumberOfUnreadMessage(0);
                room.setSender(false);

            } else {
                room.setLatestMessage(message.getContent().toString());
            }
            room.setTime(time);
            roomRepository.save(room);
            roomLeave = room;


        }

        groupRepository.save(group);

        // notify cho chính mình
        UserNotify userNotifyMain = UserNotify.builder()
                .status("LEAVE")
                .room(roomLeave)
                .build();
        simpMessagingTemplate.convertAndSendToUser(
                memberId, "/queue/messages", userNotifyMain
        );

        // notify to group
        UserNotify userNotify = UserNotify.builder()
                .status("MEMBER_LEAVE")
                .build();
        simpMessagingTemplate.convertAndSendToUser(
                groupId, "/queue/messages", userNotify
        );



    }

    @Override
    public List<Group> findAllBySenderId(String senderId) {
        List<Room> rooms = roomRepository.findAllBySenderId(senderId);
        List<Group> groups = new ArrayList<>();
        for(Room room: rooms) {
            if(room.getRoomStatus().equals(RoomStatus.ACTIVE)) {
                Optional<Group> optionalGroup = groupRepository.findById(room.getRoomId());
                if(optionalGroup.isEmpty()) continue;
                Group group = optionalGroup.get();
                if(!group.getMembers().contains(senderId)) continue;
                groups.add(group);
            }
        }
        return groups;
    }

    @Override
    public List<User> getMemberInGroup(String groupId) throws DataNotFoundException {
        Group group = groupRepository.findById(groupId).orElseThrow(() -> new DataNotFoundException("group not found"));
        List<String> members = group.getMembers();
        List<User> users = new ArrayList<>();
        for (String memberId: members) {
            Optional<User> optionalUser = userRepository.findByEmail(memberId);
            if(optionalUser.isEmpty()) continue;
            users.add(optionalUser.get());
        }
        return users;
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
