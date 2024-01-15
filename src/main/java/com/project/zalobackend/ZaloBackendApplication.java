package com.project.zalobackend;

import com.project.zalobackend.models.Message;
import com.project.zalobackend.models.MessageType;
import com.project.zalobackend.models.User;
import com.project.zalobackend.services.IGroupService;
import com.project.zalobackend.services.IMessageService;
import com.project.zalobackend.services.IUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import java.time.LocalDate;
import java.time.LocalDateTime;

@SpringBootApplication
@RequiredArgsConstructor
public class ZaloBackendApplication {
    private final IGroupService iGroupService;
    private final IMessageService iMessageService;
    private final IUserService iUserService;

    public static void main(String[] args) {
        SpringApplication.run(ZaloBackendApplication.class, args);
    }

    @Bean
    CommandLineRunner commandLineRunner() {
        return args -> {
            User user = User.builder()
                    .name("Nguyen Van A")
                    .avatar("hehe.png")
                    .dateOfBirth(LocalDate.of(2002, 2, 15))
                    .password("1234")
                    .phoneNumber("0908070605")
                    .updatedAt(LocalDateTime.now())
                    .createdAt(LocalDateTime.now())
                    .gender(true)
                    .build();
            User user1 = iUserService.createUser(user);
            Message message = Message.builder()
                    .senderId(user1.getId())
                    .receiverId(user1.getId())
                    .content("test")
                    .seenDate(LocalDateTime.now())
                    .messageType(MessageType.text)
                    .build();
            iMessageService.createMessage(message);


        };
    }

}
