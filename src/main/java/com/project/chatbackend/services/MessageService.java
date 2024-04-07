package com.project.chatbackend.services;

import com.project.chatbackend.exceptions.DataNotFoundException;
import com.project.chatbackend.models.*;
import com.project.chatbackend.repositories.MessageRepository;
import com.project.chatbackend.requests.ChatRequest;
import com.project.chatbackend.responses.MessageResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
@RequiredArgsConstructor
@Slf4j
public class MessageService implements IMessageService {
    private final MessageRepository messageRepository;
    private final RoomService roomService;
    private final S3UploadService s3UploadService;
    private final SimpMessagingTemplate simpMessagingTemplate;

    @Override
    @Async
    @Transactional
    public void saveMessage(ChatRequest chatRequest, Message messageTmp) {
        Message message = convertToMessage(chatRequest);
        message.setId(messageTmp.getId());
        message.setRoomId(messageTmp.getRoomId());
        String roomIdConvert = message.getRoomId();
        try {
            if (chatRequest.getFileContent() != null) {
                FileObject fileObject = uploadFile(chatRequest.getFileContent());
                message.setContent(fileObject);
            }
            message.setRoomId(roomIdConvert);
            message.setMessageStatus(MessageStatus.SENT);
            message.setSendDate(LocalDateTime.now());
            messageRepository.save(message);
            List<Room> rooms = roomService.findByRoomId(roomIdConvert);
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
            simpMessagingTemplate.convertAndSendToUser(
                    message.getSenderId(), "queue/messages",
                    message
            );
            simpMessagingTemplate.convertAndSendToUser(
                    message.getReceiverId(), "queue/messages",
                    message
            );
        } catch (Exception e) {
            log.error("error line 59:  " + e);
            List<Room> rooms = roomService.findByRoomId(roomIdConvert);
            for (Room room : rooms) {
                if (Objects.equals(room.getSenderId(), message.getSenderId())) {
                    if (message.getContent() instanceof FileObject fileObject) {
                        room.setLatestMessage(fileObject.getFilename());
                    } else room.setLatestMessage(message.getContent().toString());
                    room.setTime(LocalDateTime.now());
                    room.setSender(true);
                    room.setNumberOfUnreadMessage(0);
                    roomService.saveRoom(room);
                    break;
                }
            }
            message.setMessageStatus(MessageStatus.ERROR);
            messageRepository.save(message);
            simpMessagingTemplate.convertAndSendToUser(
                    message.getSenderId(), "queue/messages",
                    message
            );
        }

    }

    private String getRoomIdConvert(String senderId, String receiverId) throws DataNotFoundException {
        var roomId = roomService.getRoomId(senderId, receiverId);
        return roomId.orElseThrow(() -> new DataNotFoundException("room not found"));
    }



    @Override
    public MessageResponse getAllByRoomId(String senderId, String roomId, PageRequest pageRequest) {
        Page<Message> messagePage = messageRepository.getAllByRoomId(roomId, pageRequest);


            List<Message> messagesSend = messagePage.getContent().stream().filter(msg ->
                    msg.getSenderId().equals(senderId)
            ).toList();
            List<Message> messagesReceive = messagePage.getContent().stream().filter(msg ->
                    !msg.getSenderId().equals(senderId)
            ).toList();
            List<Message> messagesFilter = messagesReceive.stream().filter(msg ->
                    !msg.getMessageStatus().equals(MessageStatus.SENDING) &&
                            !msg.getMessageStatus().equals(MessageStatus.ERROR)
            ).toList();
            List<Message> results = Stream
                    .concat(messagesSend.stream(), messagesFilter.stream())
                    .sorted(Comparator.comparing(Message::getSendDate))
                    .toList();
            return MessageResponse.builder()
                    .messages(results)
                    .totalPage(messagePage.getTotalPages())
                    .build();

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

    @Override
    public Message saveMessage(ChatRequest chatRequest) throws DataNotFoundException {
        String roomId = getRoomIdConvert(chatRequest.getSenderId(), chatRequest.getReceiverId());
        Message message = convertToMessage(chatRequest);
        message.setSendDate(LocalDateTime.now());
        message.setRoomId(roomId);
        return messageRepository.save(message);
    }


    public Message convertToMessage(ChatRequest chatRequest) {
        if(chatRequest.getFileContent() != null) {
            MultipartFile multipartFile = chatRequest.getFileContent();
            String fileName = multipartFile.getOriginalFilename();
            assert fileName != null;
            String[] fileExtensions = fileName.split("\\.");
            FileObject fileObject = FileObject.builder()
                    .filename(fileExtensions[0])
                    .fileExtension(fileExtensions[fileExtensions.length - 1])
                    .build();
            return Message.builder()
                    .senderId(chatRequest.getSenderId())
                    .receiverId(chatRequest.getReceiverId())
                    .messageType(chatRequest.getMessageType())
                    .messageStatus(MessageStatus.SENDING)
                    .content(fileObject)
                    .build();
        }
        return Message.builder()
                .senderId(chatRequest.getSenderId())
                .receiverId(chatRequest.getReceiverId())
                .messageType(chatRequest.getMessageType())
                .messageStatus(MessageStatus.SENDING)
                .content(chatRequest.getTextContent())
                .build();
    }


    public FileObject uploadFile(MultipartFile multipartFile) throws IOException {
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
