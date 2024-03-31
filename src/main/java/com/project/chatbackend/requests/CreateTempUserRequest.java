package com.project.chatbackend.requests;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@Builder
@AllArgsConstructor
public class CreateTempUserRequest {
    @NotBlank(message = "email is required")
    @Pattern(regexp = "[a-z0-9._%+-]+@[a-z0-9.-]+\\.[a-z]{2,4}$", message = "email is invalid")
    private String email;
    @NotBlank(message = "name is required")
    private String name;
}
