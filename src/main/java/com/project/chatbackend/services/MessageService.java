package com.project.chatbackend.services;

import com.project.chatbackend.exceptions.DataNotFoundException;
import com.project.chatbackend.exceptions.PermissionAccessDenied;
import com.project.chatbackend.models.*;
import com.project.chatbackend.repositories.GroupRepository;
import com.project.chatbackend.repositories.MessageRepository;
import com.project.chatbackend.repositories.RoomRepository;
import com.project.chatbackend.repositories.UserRepository;
import com.project.chatbackend.requests.ChatImageGroupRequest;
import com.project.chatbackend.requests.ChatRequest;
import com.project.chatbackend.responses.UserNotify;
import com.project.chatbackend.responses.MessageResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

@Service
@RequiredArgsConstructor
@Slf4j
public class MessageService implements IMessageService {
    private final UserRepository userRepository;
    private final GroupRepository groupRepository;
    private final RoomRepository roomRepository;
    private final MessageRepository messageRepository;
    private final RoomService roomService;
    private final S3UploadService s3UploadService;
    private final SimpMessagingTemplate simpMessagingTemplate;

    @Override
    @Transactional
    public void saveMessage(ChatRequest chatRequest, Message messageTmp) throws PermissionAccessDenied {
        checkPermissionInChatGroup(chatRequest);
        Message message = convertToMessage(chatRequest);
        message.setId(messageTmp.getId());
        message.setRoomId(messageTmp.getRoomId());
        String roomIdConvert = message.getRoomId();
        message.setRoomId(roomIdConvert);
        try {
            if (chatRequest.getFileContent() != null) {
                s3UploadService.uploadFile(chatRequest.getFileContent(), message);
            } else {
                LocalDateTime time = LocalDateTime.now();
                message.setMessageStatus(MessageStatus.SENT);
                message.setSendDate(time);
                messageRepository.save(message);
                List<Room> rooms = roomService.findByRoomId(message.getRoomId());

                for (Room room : rooms) {
                    if (Objects.equals(room.getSenderId(), message.getSenderId())) {
                        if (message.getContent() instanceof FileObject) room.setLatestMessage(message.getMessageType().toString());
                        else room.setLatestMessage(message.getContent().toString());
                        room.setTime(time);
                        room.setSender(true);
                        room.setNumberOfUnreadMessage(0);
                        roomService.saveRoom(room);
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


        } catch (Exception e) {
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

        }

    }
    public boolean isGroupChat(String roomId) {
        return groupRepository.findById(roomId).isPresent();
    }

    private String getRoomIdConvert(String senderId, String receiverId) throws DataNotFoundException {
        var roomId = roomService.getRoomId(senderId, receiverId);
        return roomId.orElseThrow(() -> new DataNotFoundException("room not found"));
    }


    @Override
    public MessageResponse getAllByRoomId(String senderId, String roomId, PageRequest pageRequest) {
        Optional<Group> group = groupRepository.findById(roomId);
        Page<Message> messagePage = messageRepository.getAllByRoomId(roomId, pageRequest);
        // kiểm tra trong trường hợp room này là group_chat
        if (group.isPresent()) {
            List<String> members = group.get().getMembers();
            // nếu user không có trong group hoặc group inactive thì chỉ trả về các tin nhắn hệ thống
            if (!members.contains(senderId)) {
                List<Message> messagesSystem = messagePage.getContent()
                        .stream()
                        .filter(msg -> msg.getMessageType().equals(MessageType.SYSTEM))
                        .toList();
                return MessageResponse.builder()
                        .messages(messagesSystem)
                        .totalPage(0)
                        .build();
            }
        }

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
    public Message saveMessage(ChatRequest chatRequest) throws DataNotFoundException, PermissionAccessDenied {
        String roomId = getRoomIdConvert(chatRequest.getSenderId(), chatRequest.getReceiverId());
        Room room = roomRepository.findBySenderIdAndReceiverId(chatRequest.getSenderId(),
                chatRequest.getReceiverId()).orElseThrow(() -> new DataNotFoundException("room not found"));
        if (room.getRoomType().equals(RoomType.GROUP_CHAT)) {
            checkPermissionInChatGroup(chatRequest);
        }
        Message message = convertToMessage(chatRequest);
        message.setSendDate(LocalDateTime.now());
        message.setRoomId(roomId);
        return messageRepository.save(message);
    }

    private void checkPermissionInChatGroup(ChatRequest chatRequest) throws PermissionAccessDenied {
        // kiểm tra xem có phải cuộc trò chuyện nhóm hay không
        Optional<Group> optionalGroup = groupRepository.findById(chatRequest.getReceiverId());
        if (optionalGroup.isPresent()) {

            Group group = optionalGroup.get();
            // kiểm tra user có trong group hay không
            List<String> members = group.getMembers();
            if (!members.contains(chatRequest.getSenderId()))
                throw new PermissionAccessDenied("user is not in group");

            // kiểm tra quyền của senderId trong nhóm nếu permission là only_admin hoặc only_owner
            SendMessagePermission sendMessagePermission = group.getSendMessagePermission();
            if (sendMessagePermission.equals(SendMessagePermission.ONLY_ADMIN)) {
                List<String> admins = group.getAdmins();
                if (!admins.contains(chatRequest.getSenderId()) &&
                        !group.getOwner().equals(chatRequest.getSenderId()))
                    throw new PermissionAccessDenied("only admins or owner can send message");
            }
            if (sendMessagePermission.equals(SendMessagePermission.ONLY_OWNER)) {
                if (!group.getOwner().equals(chatRequest.getSenderId()))
                    throw new PermissionAccessDenied("only owner can send message");
            }
        }
    }

    @Override
    public Message saveMessageForImageGroup(ChatImageGroupRequest chatImageGroupRequest) throws Exception {
        String roomId = getRoomIdConvert(chatImageGroupRequest.getSenderId(), chatImageGroupRequest.getReceiverId());
        Message message = convertImageGroupToMessage(chatImageGroupRequest);
        message.setRoomId(roomId);
        return messageRepository.save(message);
    }

    @Override
    public void revokeMessage(String messageId, String senderId, String receiverId) throws PermissionAccessDenied {
        Optional<Message> optionalMessage = messageRepository.findById(messageId);
        // kiểm tra user có đúng là người gửi hay không
        Message message = optionalMessage.orElseThrow();
        if (!message.getSenderId().equals(senderId))
            throw new PermissionAccessDenied("permission access denied");
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
    public void forwardMessage(String messageId, String senderId, List<String> receiversId) throws DataNotFoundException {
        Optional<Message> optionalMessage = messageRepository.findById(messageId);
        Message message = optionalMessage.orElseThrow();
        message.setSenderId(senderId);
        for (String receiverId : receiversId) {
            String roomId = getRoomIdConvert(senderId, receiverId);
            message.setMessageStatus(MessageStatus.SENT);
            message.setReceiverId(receiverId);
            message.setSendDate(LocalDateTime.now());
            message.setRoomId(roomId);

            // update rooms
            List<Room> rooms = roomRepository.findByRoomId(roomId);
            for (Room room : rooms) {

                if (!room.getSenderId().equals(senderId)) {
                    if (message.getContent() instanceof FileObject) {
                        room.setLatestMessage(message.getMessageType().toString());
                    } else room.setLatestMessage(message.getContent().toString());
                    room.setTime(LocalDateTime.now());
                    room.setSender(true);
                    room.setNumberOfUnreadMessage(room.getNumberOfUnreadMessage() + 1);
                    roomService.saveRoom(room);
                } else {
                    if (message.getContent() instanceof FileObject) {
                        room.setLatestMessage(message.getMessageType().toString());
                    } else room.setLatestMessage(message.getContent().toString());
                    room.setLatestMessage(message.getContent().toString());
                    room.setTime(LocalDateTime.now());
                    room.setSender(true);
                    room.setNumberOfUnreadMessage(0);
                    roomService.saveRoom(room);
                }
            }
            UserNotify success = UserNotify.builder()
                    .senderId(message.getSenderId())
                    .receiverId(message.getReceiverId())
                    .status("SENT")
                    .build();
            simpMessagingTemplate.convertAndSendToUser(
                    message.getReceiverId(), "queue/messages",
                    success
            );

        }
        UserNotify success = UserNotify.builder()
                .senderId(message.getSenderId())
                .receiverId(message.getReceiverId())
                .status("SUCCESS")
                .build();
        simpMessagingTemplate.convertAndSendToUser(
                senderId, "queue/messages",
                success
        );
    }

    @Override
    public void saveImageGroupMessage(ChatImageGroupRequest chatImageGroupRequest, Message messageTmp) throws DataNotFoundException {

    }

    @Override
    public void seenMessage(String roomId, String senderId, String receiverId) {
        List<Message> messagesSent = messageRepository.getAllByRoomIdAndMessageStatus(roomId, MessageStatus.SENT);
        messagesSent = messagesSent.stream().filter(msg -> !msg.getSenderId().equals(senderId)).toList();
        List<Message> messagesReceiver = messageRepository.getAllByRoomIdAndMessageStatus(roomId, MessageStatus.RECEIVED);
        messagesReceiver = messagesReceiver.stream().filter(msg -> !msg.getSenderId().equals(senderId)).toList();
        for (Message msgSent : messagesSent) {
            msgSent.setSeenDate(LocalDateTime.now());
            msgSent.setMessageStatus(MessageStatus.SEEN);
            messageRepository.save(msgSent);
        }
        for (Message msgReceive : messagesReceiver) {
            msgReceive.setSeenDate(LocalDateTime.now());
            msgReceive.setMessageStatus(MessageStatus.SEEN);
            messageRepository.save(msgReceive);
        }
        Optional<Room> optionalRoom = roomRepository.findBySenderIdAndReceiverId(senderId, receiverId);
        Room room = optionalRoom.orElseThrow();
        room.setNumberOfUnreadMessage(0);
        roomRepository.save(room);
        Message latestMessage = messageRepository.findTopByOrderBySendDateDesc();
        UserNotify seen = UserNotify.builder()
                .senderId(latestMessage.getSenderId())
                .receiverId(latestMessage.getReceiverId())
                .status("SEEN")
                .build();
        simpMessagingTemplate.convertAndSendToUser(
                receiverId, "queue/messages",
                seen
        );
    }


//    @Override
//    @Transactional
//    public void saveImageGroupMessage(ChatImageGroupRequest chatImageGroupRequest, Message messageTmp) throws DataNotFoundException {
//        Message message;
//        try {
//            message = convertImageGroupToMessage(chatImageGroupRequest);
//        } catch (Exception e) {
//            throw new RuntimeException(e);
//        }
//        message.setId(messageTmp.getId());
//        message.setRoomId(messageTmp.getRoomId());
//        String roomIdConvert = message.getRoomId();
//        try {
//            if (!chatImageGroupRequest.getFilesContent().isEmpty()) {
//                List<FileObject> fileObjectsNew = new ArrayList<>();
//                List<MultipartFile> files = chatImageGroupRequest.getFilesContent();
//                for (MultipartFile file : files) {
//                    FileObject fileObject = uploadFile(file);
//                    fileObjectsNew.add(fileObject);
//                }
//                message.setContent(fileObjectsNew);
//            }
//            message.setRoomId(roomIdConvert);
//            message.setMessageStatus(MessageStatus.SENT);
//            message.setSendDate(LocalDateTime.now());
//            messageRepository.save(message);
//            List<Room> rooms = roomService.findByRoomId(roomIdConvert);
//            for (Room room : rooms) {
//                if (Objects.equals(room.getSenderId(), message.getSenderId())) {
//                    if(message.getContent() instanceof FileObject) {
//                        room.setLatestMessage(message.getMessageType().toString());
//                    } else room.setLatestMessage(message.getContent().toString());
//                    room.setTime(LocalDateTime.now());
//                    room.setSender(true);
//                    room.setNumberOfUnreadMessage(0);
//                    roomService.saveRoom(room);
//                } else {
//                    if(message.getContent() instanceof FileObject) {
//                        room.setLatestMessage(message.getMessageType().toString());
//                    } else room.setLatestMessage(message.getContent().toString());
//                    room.setNumberOfUnreadMessage(room.getNumberOfUnreadMessage() + 1);
//                    room.setTime(LocalDateTime.now());
//                    room.setSender(false);
//                    roomService.saveRoom(room);
//                }
//            }
//            UserNotify success = UserNotify.builder()
//                    .status("SUCCESS")
//                    .senderId(message.getSenderId())
//                    .receiverId(message.getReceiverId())
//                    .message(message)
//                    .build();
//            UserNotify sent = UserNotify.builder()
//                    .status("SENT")
//                    .senderId(message.getSenderId())
//                    .receiverId(message.getReceiverId())
//                    .message(message)
//                    .build();
//            simpMessagingTemplate.convertAndSendToUser(
//                    message.getSenderId(), "queue/messages",
//                    success
//            );
//            simpMessagingTemplate.convertAndSendToUser(
//                    message.getReceiverId(), "queue/messages",
//                    sent
//            );
//        } catch (Exception e) {
//            log.error("error line 59:  " + e);
//            List<Room> rooms = roomService.findByRoomId(roomIdConvert);
//            for (Room room : rooms) {
//                if (Objects.equals(room.getSenderId(), message.getSenderId())) {
//                    if (message.getContent() instanceof FileObject fileObject) {
//                        room.setLatestMessage(fileObject.getFilename());
//                    } else room.setLatestMessage(message.getContent().toString());
//                    room.setTime(LocalDateTime.now());
//                    room.setSender(true);
//                    room.setNumberOfUnreadMessage(0);
//                    roomService.saveRoom(room);
//                    break;
//                }
//            }
//            UserNotify error = UserNotify.builder()
//                    .senderId(message.getSenderId())
//                    .receiverId(message.getReceiverId())
//                    .status("ERROR")
//                    .build();
//            message.setMessageStatus(MessageStatus.ERROR);
//            message.setSendDate(LocalDateTime.now());
//            messageRepository.save(message);
//            simpMessagingTemplate.convertAndSendToUser(
//                    message.getSenderId(), "queue/messages",
//                    error
//            );
//        }
//
//
//    }

    public Message convertImageGroupToMessage(ChatImageGroupRequest chatImageGroupRequest) throws Exception {
        List<FileObject> fileObjects = new ArrayList<>();
        if (!chatImageGroupRequest.getFilesContent().isEmpty()) {
            List<MultipartFile> files = chatImageGroupRequest.getFilesContent();
            for (MultipartFile file : files) {
                String fileName = file.getOriginalFilename();
                assert fileName != null;
                String[] fileExtensions = fileName.split("\\.");
                String fileRegex = "(\\S+(\\.(?i)(jpg|png|gif|bmp))$)";
                Pattern pattern = Pattern.compile(fileRegex);
                Matcher matcher = pattern.matcher(fileName);
                if (!matcher.matches()) throw new Exception("all files must be image");
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
        if (chatRequest.getFileContent() != null) {
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


//    public FileObject uploadFile(MultipartFile multipartFile) throws IOException {
//        Map<String, String> fileInfo = s3UploadService.uploadFile(multipartFile);
//        String fileKey = fileInfo.keySet().stream().findFirst().orElseThrow();
//        String filePath = fileInfo.get(fileKey);
//        String fileName = Objects.requireNonNull(multipartFile.getOriginalFilename()).split("\\.")[0];
//        String[] extension = fileKey.split("\\.");
//        return FileObject.builder()
//                .fileKey(fileKey)
//                .fileExtension(extension[extension.length - 1])
//                .filePath(filePath)
//                .filename(fileName)
//                .build();
//    }
}
