package com.project.chatbackend;



import com.project.chatbackend.models.Emoji;
import com.project.chatbackend.models.EmojiType;
import com.project.chatbackend.models.User;
import com.project.chatbackend.repositories.EmojiRepository;
import com.project.chatbackend.repositories.UserRepository;
import lombok.RequiredArgsConstructor;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import java.util.List;
import java.util.Optional;


@SpringBootApplication
@RequiredArgsConstructor
public class ChatApplication {



    public static void main(String[] args) {
        SpringApplication.run(ChatApplication.class, args);
    }


    @Bean
    CommandLineRunner initData(UserRepository userRepository,
                               EmojiRepository emojiRepository) {
        return args -> {
            Optional<User> optional = userRepository.findByEmail("system@gmail.com");
            if(optional.isEmpty()) {
                User system = User.builder()
                        .email("system@gmail.com")
                        .password("123456")
                        .build();
                userRepository.save(system);
            }
            List<Emoji> emojis = emojiRepository.findAll();
            if(emojis.isEmpty()) {
                emojis = List.of(
                        Emoji.builder()
                                .id("6610ff903df61304b883dce3")
                                .icon("❤")
                                .emojiType(EmojiType.HEART)
                                .build(),
                        Emoji.builder()
                                .id("6610ffd93df61304b883dce4")
                                .icon("👍")
                                .emojiType(EmojiType.LIKE)
                                .build(),
                        Emoji.builder()
                                .id("661100463df61304b883dce5")
                                .icon("😆")
                                .emojiType(EmojiType.SMILE)
                                .build(),
                        Emoji.builder()
                                .id("661100b23df61304b883dce6")
                                .icon("😭")
                                .emojiType(EmojiType.CRY)
                                .build(),
                        Emoji.builder()
                                .id("661101803df61304b883dce7")
                                .icon("😠")
                                .emojiType(EmojiType.ANGRY)
                                .build(),
                        Emoji.builder()
                                .id("6611056c3df61304b883dce8")
                                .icon("😮")
                                .emojiType(EmojiType.WOW)
                                .build()
                );
                emojiRepository.saveAll(emojis);
            }

        };
    }







}
