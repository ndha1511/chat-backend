package com.project.chatbackend.services;

import com.project.chatbackend.models.FileObject;
import com.project.chatbackend.models.Message;
import com.project.chatbackend.models.MessageStatus;
import com.project.chatbackend.models.Room;
import com.project.chatbackend.repositories.MessageRepository;
import com.project.chatbackend.requests.UserNotify;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.core.waiters.WaiterResponse;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.HeadObjectRequest;
import software.amazon.awssdk.services.s3.model.HeadObjectResponse;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.waiters.S3Waiter;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Service
@RequiredArgsConstructor
@Slf4j
public class S3UploadAsync {
    @Value("${amazon-properties.bucket-name}")
    private String bucketName;
    private final SimpMessagingTemplate simpMessagingTemplate;
    private final RoomService roomService;
    private final MessageRepository messageRepository;

    @Async
    public void uploadToS3(Message message,
                           PutObjectRequest request,
                           RequestBody requestBody,
                           S3Client s3Client, Map<String, String> fileInfo,
                           String filename) {
        String fileKey = fileInfo.keySet().stream().findFirst().orElseThrow();
        String filePath = fileInfo.get(fileKey);
        String fileName = Objects.requireNonNull(filename.split("\\."))[0];
        String[] extension = fileKey.split("\\.");
        FileObject fileObject = FileObject.builder()
                .fileKey(fileKey)
                .fileExtension(extension[extension.length - 1])
                .filePath(filePath)
                .filename(fileName)
                .build();
        s3Client.putObject(request, requestBody);
        S3Waiter s3Waiter = s3Client.waiter();
        HeadObjectRequest waitRequest = HeadObjectRequest.builder()
                .bucket(bucketName).key(fileKey).build();
        WaiterResponse<HeadObjectResponse> waiterResponse = s3Waiter.waitUntilObjectExists(waitRequest);
        waiterResponse.matched().response().ifPresent(x -> log.info("File uploaded to S3 - Key: {}, Bucket: {}", fileKey, bucketName));
        message.setContent(fileObject);
        message.setMessageStatus(MessageStatus.SENT);
        message.setSendDate(LocalDateTime.now());
        messageRepository.save(message);
        List<Room> rooms = roomService.findByRoomId(message.getRoomId());
        for (Room room : rooms) {
            if (Objects.equals(room.getSenderId(), message.getSenderId())) {
                if(message.getContent() instanceof FileObject) {
                    room.setLatestMessage(message.getMessageType().toString());
                } else room.setLatestMessage(message.getContent().toString());
                room.setTime(LocalDateTime.now());
                room.setSender(true);
                room.setNumberOfUnreadMessage(0);
                roomService.saveRoom(room);
            } else {
                if(message.getContent() instanceof FileObject) {
                    room.setLatestMessage(message.getMessageType().toString());
                } else room.setLatestMessage(message.getContent().toString());
                room.setNumberOfUnreadMessage(room.getNumberOfUnreadMessage() + 1);
                room.setTime(LocalDateTime.now());
                room.setSender(false);
                roomService.saveRoom(room);
            }
        }
        UserNotify success = UserNotify.builder()
                .status("SUCCESS")
                .senderId(message.getSenderId())
                .receiverId(message.getReceiverId())
                .message(message)
                .build();
        UserNotify sent = UserNotify.builder()
                .status("SENT")
                .senderId(message.getSenderId())
                .receiverId(message.getReceiverId())
                .message(message)
                .build();
        simpMessagingTemplate.convertAndSendToUser(
                message.getSenderId(), "queue/messages",
                success
        );
        simpMessagingTemplate.convertAndSendToUser(
                message.getReceiverId(), "queue/messages",
                sent
        );
    }
}
