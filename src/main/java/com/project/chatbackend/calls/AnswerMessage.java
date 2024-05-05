package com.project.chatbackend.calls;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class AnswerMessage {
    private String receiverId;
    private String fromUser;
    private Answer answer;
}
