package com.project.chatbackend.models;

import lombok.*;
import org.springframework.data.mongodb.core.mapping.Field;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class EmojiMessage {
    @Field(name = "user_id")
    private String userId;
    @Field(name = "emoji_id")
    private String emojiId;
    private int quantity;
}
