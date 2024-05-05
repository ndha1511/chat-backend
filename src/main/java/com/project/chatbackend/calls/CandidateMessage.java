package com.project.chatbackend.calls;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class CandidateMessage {
    private String receiverId;
    private String fromUser;
    private Candidate candidate;
}
