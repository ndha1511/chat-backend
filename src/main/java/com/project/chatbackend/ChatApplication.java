package com.project.chatbackend;


import com.project.chatbackend.models.User;
import com.project.chatbackend.repositories.UserRepository;
import com.project.chatbackend.services.IRoomService;
import lombok.RequiredArgsConstructor;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import java.util.List;


@SpringBootApplication
@RequiredArgsConstructor
public class ChatApplication {
    private final UserRepository userRepository;
    private final IRoomService roomService;


    public static void main(String[] args) {
        SpringApplication.run(ChatApplication.class, args);
    }

    @Bean
    CommandLineRunner commandLineRunner() {
        return args -> {
            User user = userRepository.findByPhoneNumber("0998822113").get();
            List<String> friends = List.of("65e9fa104606986c2cf6a16f", "65e9f3b65ec07008aa56c910");
            user.setFriends(friends);
            userRepository.save(user);
            roomService.getRoomId(user.getId(), "65e9fa104606986c2cf6a16f");
            roomService.getRoomId(user.getId(), "65e9f3b65ec07008aa56c910");

        };

    }



}
