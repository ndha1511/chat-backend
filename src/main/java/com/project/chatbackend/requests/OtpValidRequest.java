package com.project.chatbackend.requests;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.stereotype.Service;
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class OtpValidRequest implements java.io.Serializable{
    private String email;
    private String otp;

}
