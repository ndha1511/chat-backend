package com.project.chatbackend.models;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Document(collection = "emojis")
public class Emoji {
    @Id
    private String id;
    private String icon; // path to icon
    @Field(name = "emoji_type")
    private EmojiType emojiType;
}
