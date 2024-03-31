package com.project.chatbackend.services;

import com.project.chatbackend.models.User;
import com.project.chatbackend.requests.CreateTempUserRequest;
import com.project.chatbackend.requests.UseRegisterRequest;
import com.project.chatbackend.requests.UserLoginRequest;
import com.project.chatbackend.responses.LoginResponse;
import com.project.chatbackend.responses.UserLoginResponse;


public interface IUserService {
    User createUser(UseRegisterRequest useRegisterRequest) throws Exception;
    LoginResponse login(UserLoginRequest userLoginRequest) throws Exception;

    LoginResponse refreshToken(String refreshToken) throws Exception;

    UserLoginResponse findByPhoneNumber(String phoneNumber) throws Exception;

    UserLoginResponse findById(String id) throws Exception;

    User createTempUser(CreateTempUserRequest createTempUserRequest) throws Exception;

    boolean updateVerificationStatus(String id, boolean status) throws Exception;


}
