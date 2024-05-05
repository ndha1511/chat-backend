package com.project.chatbackend.calls;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.mongodb.core.mapping.Document;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class OfferMessage {
    private String receiverId;
    private String fromUser;
    private Offer offer;
}
