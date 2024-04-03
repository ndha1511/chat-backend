package com.project.chatbackend.requests;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class OtpValidRequest implements java.io.Serializable{
    private String email;
    private String otp;

}
