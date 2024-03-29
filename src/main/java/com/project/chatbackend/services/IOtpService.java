package com.project.chatbackend.services;

import com.project.chatbackend.models.Otp;
import com.project.chatbackend.requests.OtpRequest;
import com.project.chatbackend.requests.OtpValidRequest;

public interface IOtpService {
    boolean sendOTP(OtpRequest otpRequest);
    String verifyOTP(OtpValidRequest otpValidRequest);

    Otp saveOTP(Otp otp);
    void removeOTP(String email);
    Otp getOTP(String email, String otp);
}
