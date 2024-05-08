package com.project.chatbackend.utils;

import com.project.chatbackend.models.*;
import com.project.chatbackend.repositories.MessageRepository;
import com.project.chatbackend.repositories.RoomRepository;
import com.project.chatbackend.repositories.UserRepository;
import com.project.chatbackend.responses.UserNotify;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * xử lý cuộc gọi
 */
@Component
@RequiredArgsConstructor
public class CallHandler {
    private ScheduledExecutorService scheduledExecutorService;
    private final SimpMessagingTemplate simpMessagingTemplate;
    private final MessageRepository messageRepository;
    private final RoomRepository roomRepository;
    private final UserRepository userRepository;

    // hàm này sẽ được thực thi sau 1 phút khi user a yêu cầu 1 cuộc gọi với user b
    // mà user b không phản hồi (missed call)
    public void startCall(Message message) {
        // lên lịch thời gian chờ cho cuộc gọi
        scheduledExecutorService = Executors.newScheduledThreadPool(1);
        scheduledExecutorService.schedule(() -> {
            handleMissedCall(message);
            notify(message.getSenderId(), message.getReceiverId(), "MISSED_CALL");
        }, 1, TimeUnit.MINUTES);
    }

    // hàm này sẽ ngừng schedule (user b chấp nhận cuộc gọi)
    public void acceptCall(Message message) {
        scheduledExecutorService.shutdownNow();
        CallInfo callInfo = (CallInfo) message.getContent();
        callInfo.setStartTime(LocalDateTime.now());
        callInfo.setCallStatus(CallStatus.CALLING);
        message.setContent(callInfo);
        Message newMessage = messageRepository.save(message);
        Message latestMessage = messageRepository.findTopByOrderBySendDateDesc();
        if(latestMessage.getId().equals(newMessage.getId())) {
            List<Room> rooms = roomRepository.findByRoomId(newMessage.getRoomId());
            for (Room room : rooms) {
                room.setLatestMessage("Cuộc gọi đang diễn ra");
                roomRepository.save(room);
            }
        }
        notify(message.getSenderId(), "", "ACCEPT_CALL");
    }

    public void endCall(Message message) {
        CallInfo callInfo = (CallInfo) message.getContent();
        callInfo.setCallStatus(CallStatus.END);
        callInfo.setEndTime(LocalDateTime.now());
        callInfo.setDuration(callInfo.getEndTime().toEpochSecond(ZoneOffset.UTC) -
                callInfo.getStartTime().toEpochSecond(ZoneOffset.UTC));
        message.setContent(callInfo);
        Message newMessage = messageRepository.save(message);
        Message latestMessage = messageRepository.findTopByOrderBySendDateDesc();
        if(latestMessage.getId().equals(newMessage.getId())) {
            List<Room> rooms = roomRepository.findByRoomId(newMessage.getRoomId());
            for (Room room : rooms) {
                if (room.getRoomType().equals(RoomType.GROUP_CHAT)) {
                    if (room.getSenderId().equals(newMessage.getSenderId())) {
                        room.setLatestMessage("Cuộc gọi đã kết thúc");
                    } else {
                        User sender = userRepository.findByEmail(newMessage.getSenderId())
                                .orElseThrow();
                        room.setLatestMessage(sender.getName() + ": cuộc gọi đã kết thúc");
                    }
                } else {
                    room.setLatestMessage("Cuộc gọi đã kết thúc");
                }
                roomRepository.save(room);
            }
        }
        notify(message.getSenderId(), message.getReceiverId(), "END_CALL");
    }

    public void rejectCall(Message message) {
        scheduledExecutorService.shutdownNow();
        CallInfo callInfo = (CallInfo) message.getContent();
        callInfo.setCallStatus(CallStatus.REJECT);
        message.setContent(callInfo);
        Message newMessage = messageRepository.save(message);
        Message latestMessage = messageRepository.findTopByOrderBySendDateDesc();
        if(latestMessage.getId().equals(newMessage.getId())) {
            List<Room> rooms = roomRepository.findByRoomId(newMessage.getRoomId());
            for (Room room : rooms) {
                if (room.getRoomType().equals(RoomType.GROUP_CHAT)) {
                    if (room.getSenderId().equals(newMessage.getSenderId())) {
                        room.setLatestMessage("Cuộc gọi đến");
                    } else {
                        User sender = userRepository.findByEmail(newMessage.getSenderId())
                                .orElseThrow();
                        room.setLatestMessage(sender.getName() + ": cuộc gọi đến");
                    }
                } else {
                    room.setLatestMessage("Cuộc gọi đến");
                }
                roomRepository.save(room);
            }
        }
        notify(message.getSenderId(), message.getReceiverId(), "REJECT_CALL");
    }

    public void joinCall(Message message) {


    }

    public void leaveCall(Message message) {

    }

    private void notify(String senderId, String receiverId, String status) {
        UserNotify callRequestNotify = UserNotify.builder()
                .senderId(senderId)
                .receiverId(receiverId)
                .status(status)
                .build();
        if(!senderId.isEmpty()) {
            simpMessagingTemplate.convertAndSendToUser(
                    senderId, "queue/messages",
                    callRequestNotify
            );
        }
        if(!receiverId.isEmpty()) {
            simpMessagingTemplate.convertAndSendToUser(
                    receiverId, "queue/messages",
                    callRequestNotify
            );
        }
    }

    private void handleMissedCall(Message message) {
        CallInfo callInfo = (CallInfo) message.getContent();
        callInfo.setCallStatus(CallStatus.MISSED);
        message.setContent(callInfo);
        Message newMessage = messageRepository.save(message);
        Message latestMessage = messageRepository.findTopByOrderBySendDateDesc();
        if(latestMessage.getId().equals(newMessage.getId())) {
            List<Room> rooms = roomRepository.findByRoomId(newMessage.getRoomId());
            for (Room room : rooms) {
                if (room.getRoomType().equals(RoomType.GROUP_CHAT)) {
                    if (room.getSenderId().equals(newMessage.getSenderId())) {
                        room.setLatestMessage("Cuộc gọi đến");
                    } else {
                        User sender = userRepository.findByEmail(newMessage.getSenderId())
                                .orElseThrow();
                        room.setLatestMessage(sender.getName() + ": cuộc gọi đến");
                    }
                } else {
                    room.setLatestMessage("Cuộc gọi đến");
                }
                roomRepository.save(room);
            }
        }


    }


}
