package com.project.chatbackend.services;

import com.project.chatbackend.models.TempUser;
import com.project.chatbackend.models.User;
import com.project.chatbackend.requests.UseRegisterRequest;

public interface ITempUserService {
    TempUser createUser(UseRegisterRequest useRegisterRequest) throws Exception;
    TempUser findByEmail(String email) throws Exception;

    TempUser updateByEmail(String email, UseRegisterRequest useRegisterRequest) throws Exception;
    TempUser deleteByEmail(String email);
}
