package com.project.chatbackend.services;

import com.project.chatbackend.exceptions.DataNotFoundException;
import com.project.chatbackend.models.User;
import com.project.chatbackend.requests.*;
import com.project.chatbackend.responses.LoginResponse;
import com.project.chatbackend.responses.UserLoginResponse;
import java.util.List;


public interface IUserService {
    User createUser(UseRegisterRequest useRegisterRequest) throws Exception;
    LoginResponse login(UserLoginRequest userLoginRequest) throws Exception;
    LoginResponse refreshToken(String refreshToken) throws Exception;
    UserLoginResponse findByPhoneNumber(String phoneNumber) throws Exception;
    UserLoginResponse findByEmail(String email) throws Exception;
    UserLoginResponse findById(String id) throws Exception;
    User findUserByEmail(String email) throws DataNotFoundException;
    boolean deleteUserByEmail(String email);
    boolean isValidOTP(OtpValidRequest otpValidRequest);
    boolean changePassword(ChangePasswordRequest changePasswordRequest);
    boolean resetPassword(ResetPasswordRequest resetPasswordRequest);
    UserLoginResponse updateUser(UserUpdateRequest updateUserRequest) throws Exception;
    List<UserLoginResponse> getFriends(String userId);
    void blockUser(String senderId, String blockId);
    void unblockUser(String senderId, String unblockId);
    List<UserLoginResponse> getBlocksUser(String userId);
}
