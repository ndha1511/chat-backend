package com.project.chatbackend.requests;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;

@Data
@Builder
public class UseRegisterRequest {
    @NotBlank(message = "phone number is required")
    @Pattern(regexp = "^0[0-9]{9}", message = "phone number must start with 0 and have 10 digits")
    private String phoneNumber;
    @NotBlank(message = "name is required")
    private String name;
    private boolean gender;
    private LocalDate dateOfBirth;
    @NotBlank(message = "password is required")
    @Size(min = 6, message = "password must be 6 characters or more")
    private String password;
    private String avatar;
    @NotBlank(message = "email is required")
    @Pattern(regexp = "[a-z0-9._%+-]+@[a-z0-9.-]+\\.[a-z]{2,4}$",
    message = "email is invalid")
    private String email;
}
