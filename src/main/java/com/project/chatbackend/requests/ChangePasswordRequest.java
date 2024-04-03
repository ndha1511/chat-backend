package com.project.chatbackend.requests;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ChangePasswordRequest {
    @NotBlank(message = "email is required")
    @Pattern(regexp = "[a-z0-9._%+-]+@[a-z0-9.-]+\\.[a-z]{2,4}$", message = "email is invalid")
    private String email;
    @NotBlank(message = "password is required")
    @Size(min = 6, message = "password must be 6 characters or more")
    private String oldPassword;
    @NotBlank(message = "new password is required")
    @Size(min = 6, message = "new password must be 6 characters or more")
    private String newPassword;
    @NotBlank(message = "confirm password is required")
    private String confirmNewPassword;
}
