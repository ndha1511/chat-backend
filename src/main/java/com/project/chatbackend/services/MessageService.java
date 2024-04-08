package com.project.chatbackend.services;

import com.project.chatbackend.exceptions.DataNotFoundException;
import com.project.chatbackend.models.*;
import com.project.chatbackend.repositories.MessageRepository;
import com.project.chatbackend.requests.ChatImageGroupRequest;
import com.project.chatbackend.requests.ChatRequest;
import com.project.chatbackend.requests.UserNotify;
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
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
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
    private final FileUpload fileUpload;

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
            UserNotify error = UserNotify.builder()
                    .senderId(message.getSenderId())
                    .receiverId(message.getReceiverId())
                    .status("ERROR")
                    .build();
            message.setMessageStatus(MessageStatus.ERROR);
            message.setSendDate(LocalDateTime.now());
            messageRepository.save(message);
            simpMessagingTemplate.convertAndSendToUser(
                    message.getSenderId(), "queue/messages",
                    error
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

    @Override
    public Message saveMessageForImageGroup(ChatImageGroupRequest chatImageGroupRequest) throws Exception {
        String roomId = getRoomIdConvert(chatImageGroupRequest.getSenderId(), chatImageGroupRequest.getReceiverId());
        Message message = convertImageGroupToMessage(chatImageGroupRequest);
        message.setRoomId(roomId);
        return messageRepository.save(message);
    }

    @Override
    public void revokeMessage(String messageId, String receiverId) {
        Optional<Message> optionalMessage = messageRepository.findById(messageId);
        Message message = optionalMessage.orElseThrow();
        message.setMessageStatus(MessageStatus.REVOKED);
        messageRepository.save(message);
        UserNotify revoke = UserNotify.builder()
                .senderId(message.getSenderId())
                .receiverId(message.getReceiverId())
                .status("REVOKED_MESSAGE")
                .build();
        simpMessagingTemplate.convertAndSendToUser(
                receiverId, "queue/messages",
                revoke
        );
    }

    @Override
    public void forwardMessage(String messageId, String senderId, String receiverId) {

    }

    @Override
    @Transactional
    public void saveImageGroupMessage(ChatImageGroupRequest chatImageGroupRequest, Message messageTmp) throws DataNotFoundException {
        Message message;
        try {
            message = convertImageGroupToMessage(chatImageGroupRequest);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        message.setId(messageTmp.getId());
        message.setRoomId(messageTmp.getRoomId());
        String roomIdConvert = message.getRoomId();
        try {
            if (!chatImageGroupRequest.getFilesContent().isEmpty()) {
                List<FileObject> fileObjectsNew = new ArrayList<>();
                List<MultipartFile> files = chatImageGroupRequest.getFilesContent();
                for (MultipartFile file : files) {
                    FileObject fileObject = uploadFile(file);
                    fileObjectsNew.add(fileObject);
                }
                message.setContent(fileObjectsNew);
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
            UserNotify error = UserNotify.builder()
                    .senderId(message.getSenderId())
                    .receiverId(message.getReceiverId())
                    .status("ERROR")
                    .build();
            message.setMessageStatus(MessageStatus.ERROR);
            message.setSendDate(LocalDateTime.now());
            messageRepository.save(message);
            simpMessagingTemplate.convertAndSendToUser(
                    message.getSenderId(), "queue/messages",
                    error
            );
        }


    }

    public Message convertImageGroupToMessage(ChatImageGroupRequest chatImageGroupRequest) throws Exception {
        List<FileObject> fileObjects = new ArrayList<>();
        if(!chatImageGroupRequest.getFilesContent().isEmpty()) {
            List<MultipartFile> files = chatImageGroupRequest.getFilesContent();
            for(MultipartFile file : files) {
                String fileName = file.getOriginalFilename();
                assert fileName != null;
                String[] fileExtensions = fileName.split("\\.");
                String fileRegex = "(\\S+(\\.(?i)(jpg|png|gif|bmp))$)";
                Pattern pattern = Pattern.compile(fileRegex);
                Matcher matcher = pattern.matcher(fileName);
                if(!matcher.matches()) throw new Exception("all files must be image");
                FileObject fileObject = FileObject.builder()
                        .filename(fileExtensions[0])
                        .fileExtension(fileExtensions[fileExtensions.length - 1])
                        .build();
                fileObjects.add(fileObject);
            }
            return Message.builder()
                    .senderId(chatImageGroupRequest.getSenderId())
                    .receiverId(chatImageGroupRequest.getReceiverId())
                    .messageType(chatImageGroupRequest.getMessageType())
                    .messageStatus(MessageStatus.SENDING)
                    .content(fileObjects)
                    .build();
        }
        return null;
    }

    public Message convertToMessage(ChatRequest chatRequest) {
        if(chatRequest.getFileContent() != null) {
            MultipartFile multipartFile = chatRequest.getFileContent();
            String fileName = multipartFile.getOriginalFilename();
            System.out.println(multipartFile.getSize());
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
        String fileName = Objects.requireNonNull(multipartFile.getOriginalFilename()).split("\\.")[0];
        String[] extension = fileKey.split("\\.");
        return FileObject.builder()
                .fileKey(fileKey)
                .fileExtension(extension[extension.length - 1])
                .filePath(filePath)
                .filename(fileName)
                .build();
    }
}
