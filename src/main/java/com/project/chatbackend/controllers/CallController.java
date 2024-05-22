package com.project.chatbackend.controllers;


import com.project.chatbackend.calls.AnswerMessage;
import com.project.chatbackend.calls.Call;
import com.project.chatbackend.calls.CandidateMessage;
import com.project.chatbackend.calls.OfferMessage;
import com.project.chatbackend.responses.TypingChat;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

@Controller
@RequiredArgsConstructor
@Slf4j
public class CallController {

    private final SimpMessagingTemplate simpMessagingTemplate;

    @MessageMapping("/call")
    public void Call(@Payload Call call){
        simpMessagingTemplate.convertAndSendToUser(call.getCallTo(),
                "/topic/call", call);
    }

    @MessageMapping("/offer")
    public void offer(@Payload OfferMessage offer){
        simpMessagingTemplate
                .convertAndSendToUser(offer.getReceiverId(),
                        "/topic/offer",offer);
    }

    @MessageMapping("/answer")
    public void answer(@Payload AnswerMessage answer){
        simpMessagingTemplate.convertAndSendToUser(answer.getReceiverId(),
                "/topic/answer",answer);
    }

    @MessageMapping("/candidate")
    public void candidate(@Payload CandidateMessage candidate){
        simpMessagingTemplate
                .convertAndSendToUser(candidate.getReceiverId(),
                        "/topic/candidate",candidate);
    }

    @MessageMapping("/typing")
    public void typing(@Payload TypingChat typingChat) {
        simpMessagingTemplate
                .convertAndSendToUser(typingChat.getReceiverId(),
                        "/queue/messages",typingChat);
    }



}
