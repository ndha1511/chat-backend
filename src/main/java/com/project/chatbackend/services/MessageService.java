package com.project.chatbackend.services;

import com.project.chatbackend.exceptions.DataNotFoundException;
import com.project.chatbackend.models.FileObject;
import com.project.chatbackend.models.Message;
import com.project.chatbackend.models.MessageStatus;
import com.project.chatbackend.models.Room;
import com.project.chatbackend.repositories.MessageRepository;
import com.project.chatbackend.requests.ChatRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class MessageService implements IMessageService {
    private final MessageRepository messageRepository;
    private final RoomService roomService;
    private final S3UploadService s3UploadService;
    private final SimpMessagingTemplate simpMessagingTemplate;
    @Override
    @Async
    @Transactional
    public void saveMessage(ChatRequest chatRequest)  {
        Message message = convertToMessage(chatRequest);
        try {
            if (chatRequest.getFileContent() != null) {
                FileObject fileObject = uploadFile(chatRequest.getFileContent());
                message.setContent(fileObject);
            }
            String roomIdConvert = getRoomIdConvert(chatRequest.getSenderId(),
                    chatRequest.getReceiverId(), message);

            message.setRoomId(roomIdConvert);
            message.setMessageStatus(MessageStatus.SENT);
            messageRepository.save(message);
            simpMessagingTemplate.convertAndSendToUser(
                    message.getSenderId(), "queue/messages",
                    message
            );
            simpMessagingTemplate.convertAndSendToUser(
                    message.getReceiverId(), "queue/messages",
                    message
            );
        } catch (Exception e) {
            message.setMessageStatus(MessageStatus.ERROR);
            simpMessagingTemplate.convertAndSendToUser(
                    message.getSenderId(), "queue/messages",
                    message
            );
        }

    }

    private String getRoomIdConvert(String senderId, String receiverId, Message message) throws DataNotFoundException {
        var roomId = roomService.getRoomId(senderId, receiverId);
        String roomIdConvert = roomId.orElseThrow(() -> new DataNotFoundException("room not found"));
        List<Room> rooms = roomService.findByRoomId(roomIdConvert);
        for (Room room : rooms) {
            if (Objects.equals(room.getSenderId(), senderId)) {
                room.setLatestMessage(message.getContent().toString());
                room.setTime(LocalDateTime.now());
                room.setSender(true);
                room.setNumberOfUnreadMessage(0);
                roomService.saveRoom(room);
            } else {
                room.setLatestMessage(message.getContent().toString());
                room.setNumberOfUnreadMessage(room.getNumberOfUnreadMessage() + 1);
                room.setTime(LocalDateTime.now());
                room.setSender(false);
                roomService.saveRoom(room);
            }
        }
        return roomIdConvert;
    }

    @Override
    public Page<Message> getAllByRoomId(String roomId, PageRequest pageRequest) {
        return messageRepository.getAllByRoomId(roomId, pageRequest);
    }

    @Override
    public void updateMessage(String id, ChatRequest chatRequest) {
        Message message = convertToMessage(chatRequest);
        Message newMsg = messageRepository.findById(id).orElseThrow();
        newMsg.setMessageStatus(message.getMessageStatus());
        newMsg.setMessageType(message.getMessageType());
        newMsg.setMessageStatus(message.getMessageStatus());
        newMsg.setContent(message.getContent());
        newMsg.setHiddenSenderSide(chatRequest.isHiddenSenderSide());
        messageRepository.save(newMsg);
    }

    private Message convertToMessage(ChatRequest chatRequest) {
        return Message.builder()
                .senderId(chatRequest.getSenderId())
                .receiverId(chatRequest.getReceiverId())
                .messageType(chatRequest.getMessageType())
                .messageStatus(chatRequest.getMessageStatus())
                .content(chatRequest.getTextContent())
                .seenDate(LocalDateTime.now())
                .build();
    }


    public FileObject uploadFile(MultipartFile multipartFile) {
        Map<String, String> fileInfo = s3UploadService.uploadFile(multipartFile);
        String fileKey = fileInfo.keySet().stream().findFirst().orElseThrow();
        String filePath = fileInfo.get(fileKey);
        String[] extension = fileKey.split("\\.");
        return FileObject.builder()
                .fileKey(fileKey)
                .fileExtension(extension[extension.length - 1])
                .filePath(filePath)
                .filename(extension[0])
                .build();
    }
}
