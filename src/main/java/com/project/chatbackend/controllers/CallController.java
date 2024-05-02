package com.project.chatbackend.controllers;

import lombok.RequiredArgsConstructor;
import org.cloudinary.json.JSONObject;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

@Controller
@RequiredArgsConstructor
public class CallController {

    private final SimpMessagingTemplate simpMessagingTemplate;

    @MessageMapping("/call")
    public void Call(String call){
        JSONObject jsonObject = new JSONObject(call);
        simpMessagingTemplate.convertAndSendToUser(jsonObject.getString("callTo"),"/topic/call",jsonObject.get("callFrom"));
    }

    @MessageMapping("/offer")
    public void Offer(String offer){
        JSONObject jsonObject = new JSONObject(offer);
        simpMessagingTemplate.convertAndSendToUser(jsonObject.getString("toUser"),"/topic/offer",offer);
    }

    @MessageMapping("/answer")
    public void Answer(String answer){
        JSONObject jsonObject = new JSONObject(answer);
        simpMessagingTemplate.convertAndSendToUser(jsonObject.getString("toUser"),"/topic/answer",answer);
    }

    @MessageMapping("/candidate")
    public void Candidate(String candidate){
        JSONObject jsonObject = new JSONObject(candidate);
        simpMessagingTemplate.convertAndSendToUser(jsonObject.getString("toUser"),"/topic/candidate",candidate);
    }


}
