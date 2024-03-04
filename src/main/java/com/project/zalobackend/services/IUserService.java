package com.project.zalobackend.services;

import com.project.zalobackend.models.User;
import com.project.zalobackend.requests.UseRegisterRequest;
import com.project.zalobackend.requests.UserLoginRequest;
import com.project.zalobackend.responses.LoginResponse;

public interface IUserService {
    User createUser(UseRegisterRequest useRegisterRequest) throws Exception;
    LoginResponse login(UserLoginRequest userLoginRequest) throws Exception;
}
