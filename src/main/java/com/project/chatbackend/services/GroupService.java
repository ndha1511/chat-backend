package com.project.chatbackend.services;

import com.project.chatbackend.exceptions.DataNotFoundException;
import com.project.chatbackend.models.Group;
import com.project.chatbackend.models.Message;
import com.project.chatbackend.models.MessageType;
import com.project.chatbackend.models.Room;
import com.project.chatbackend.repositories.GroupRepository;
import com.project.chatbackend.repositories.MessageRepository;
import com.project.chatbackend.repositories.RoomRepository;
import com.project.chatbackend.repositories.UserRepository;
import com.project.chatbackend.requests.UserNotify;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;

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
                .sendMessage(true)
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
                    .build();
            // notify
            simpMessagingTemplate.convertAndSendToUser(
                    memberId, "queue/message",
                    userNotify
            );
        }
        return roomOwner;
    }
}
