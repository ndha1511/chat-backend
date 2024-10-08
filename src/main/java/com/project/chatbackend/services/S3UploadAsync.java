package com.project.chatbackend.services;

import com.project.chatbackend.models.*;
import com.project.chatbackend.repositories.GroupRepository;
import com.project.chatbackend.repositories.MessageRepository;
import com.project.chatbackend.repositories.UserRepository;
import com.project.chatbackend.requests.ChatRequest;
import com.project.chatbackend.responses.UserNotify;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.transfer.s3.S3TransferManager;
import software.amazon.awssdk.transfer.s3.model.CompletedFileUpload;
import software.amazon.awssdk.transfer.s3.model.FileUpload;
import software.amazon.awssdk.transfer.s3.model.UploadFileRequest;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Service
@RequiredArgsConstructor
@Slf4j
public class S3UploadAsync {
    private final GroupRepository groupRepository;
    private final UserRepository userRepository;
    private final SimpMessagingTemplate simpMessagingTemplate;
    private final RoomService roomService;
    private final MessageRepository messageRepository;


    @Async("asyncExecutor")
    public void uploadToS3(Message message,
                           S3TransferManager transferManager,
                           UploadFileRequest uploadFileRequest,
                           Map<String, String> fileInfo,
                           String filename,
                           long filSize) {
        String fileKey = fileInfo.keySet().stream().findFirst().orElseThrow();
        String filePath = fileInfo.get(fileKey);
        String fileName = Objects.requireNonNull(filename.split("\\."))[0];
        String[] extension = fileKey.split("\\.");
        FileObject fileObject = FileObject.builder()
                .fileKey(fileKey)
                .fileExtension(extension[extension.length - 1])
                .filePath(filePath)
                .filename(fileName)
                .size(filSize)
                .build();
        FileUpload fileUpload = transferManager.uploadFile(uploadFileRequest);
        CompletedFileUpload uploadResult = fileUpload.completionFuture().join();
        log.info("upload successfully: " + uploadResult.response().eTag());
        transferManager.close();
        message.setContent(fileObject);
        message.setMessageStatus(MessageStatus.SENT);
        LocalDateTime time = LocalDateTime.now();
        message.setSendDate(time);
        messageRepository.save(message);
        List<Room> rooms = roomService.findByRoomId(message.getRoomId());
        Group group = null;
        if(isGroupChat(message.getRoomId())) {
            group = groupRepository.findById(message.getRoomId()).orElseThrow();
        }
        for (Room room : rooms) {
            if(group != null){
                List<String> members = group.getMembers();
                if(!members.contains(room.getSenderId())) continue;
            }
            if (Objects.equals(room.getSenderId(), message.getSenderId())) {
                if(message.getContent() instanceof FileObject) {
                    room.setLatestMessage(message.getMessageType().toString());
                } else room.setLatestMessage(message.getContent().toString());
                room.setTime(time);
                room.setSender(true);
                room.setNumberOfUnreadMessage(0);
                Room roomRs = roomService.saveRoom(room);
                UserNotify success = UserNotify.builder()
                        .status("SUCCESS")
                        .senderId(message.getSenderId())
                        .receiverId(message.getReceiverId())
                        .message(message)
                        .room(roomRs)
                        .build();
                simpMessagingTemplate.convertAndSendToUser(
                        message.getSenderId(), "queue/messages",
                        success
                );
            } else {
                User user = userRepository.findByEmail(message.getSenderId()).orElseThrow();
                if (message.getContent() instanceof FileObject) {
                    if(isGroupChat(room.getRoomId())) {
                        room.setLatestMessage(user.getName() + ": " +message.getMessageType().toString());
                    } else {
                        room.setLatestMessage(message.getMessageType().toString());
                    }

                } else {
                    if(isGroupChat(room.getRoomId())) {
                        room.setLatestMessage(user.getName() + ": " + message.getContent().toString());
                    } else
                        room.setLatestMessage(message.getContent().toString());
                }
                room.setNumberOfUnreadMessage(room.getNumberOfUnreadMessage() + 1);
                room.setTime(time);
                room.setSender(false);
                roomService.saveRoom(room);
            }
        }

        UserNotify sent = UserNotify.builder()
                .status("SENT")
                .senderId(message.getSenderId())
                .receiverId(message.getReceiverId())
                .message(message)
                .build();
        simpMessagingTemplate.convertAndSendToUser(
                message.getReceiverId(), "queue/messages",
                sent
        );
    }

    @Async("asyncExecutor")
    public void saveMessageAsync(Message message, ChatRequest chatRequest, Group group) {
        LocalDateTime time = LocalDateTime.now();
        message.setMessageStatus(MessageStatus.SENT);
        message.setSendDate(time);
        messageRepository.save(message);
        List<Room> rooms = roomService.findByRoomId(message.getRoomId());

        for (Room room : rooms) {
            if(group != null) {
                List<String> members = group.getMembers();
                if(!members.contains(room.getSenderId())) continue;
            }
            if (Objects.equals(room.getSenderId(), message.getSenderId())) {
                if (message.getContent() instanceof FileObject) room.setLatestMessage(message.getMessageType().toString());
                else room.setLatestMessage(message.getContent().toString());
                room.setTime(time);
                room.setSender(true);
                room.setNumberOfUnreadMessage(0);
                Room roomRs = roomService.saveRoom(room);
                UserNotify success = UserNotify.builder()
                        .status("SUCCESS")
                        .senderId(message.getSenderId())
                        .receiverId(message.getReceiverId())
                        .message(message)
                        .room(roomRs)
                        .build();
                simpMessagingTemplate.convertAndSendToUser(
                        message.getSenderId(), "queue/messages",
                        success
                );
            } else {
                if (message.getContent() instanceof FileObject) {
                    if(isGroupChat(room.getRoomId())) {
                        room.setLatestMessage(chatRequest.getSenderName() + ": " +message.getMessageType().toString());
                    } else {
                        room.setLatestMessage(message.getMessageType().toString());
                    }

                } else {
                    if(isGroupChat(room.getRoomId())) {
                        room.setLatestMessage(chatRequest.getSenderName() + ": " + message.getContent().toString());
                    } else
                        room.setLatestMessage(message.getContent().toString());
                }
                room.setNumberOfUnreadMessage(room.getNumberOfUnreadMessage() + 1);
                room.setTime(time);
                room.setSender(false);
                roomService.saveRoom(room);
            }
        }

        UserNotify sent = UserNotify.builder()
                .status("SENT")
                .senderId(message.getSenderId())
                .receiverId(message.getReceiverId())
                .message(message)
                .build();

        simpMessagingTemplate.convertAndSendToUser(
                message.getReceiverId(), "queue/messages",
                sent
        );
    }

    private boolean isGroupChat(String roomId) {
        return groupRepository.findById(roomId).isPresent();
    }
}
