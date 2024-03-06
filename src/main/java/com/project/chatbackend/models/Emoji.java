package com.project.chatbackend.models;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Emoji {
    private String userId;
    private String icon; // path to icon
    private int quantity;
}
