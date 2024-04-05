package com.project.chatbackend.services;

import com.project.chatbackend.models.TempUser;
import com.project.chatbackend.repositories.TempUserRepository;
import com.project.chatbackend.requests.UseRegisterRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class TempUserService implements ITempUserService{
    private final TempUserRepository tempUserRepository;
    @Override
    public TempUser createUser(UseRegisterRequest useRegisterRequest) throws Exception {
        return tempUserRepository.save(TempUser.builder()
                .email(useRegisterRequest.getEmail())
                .password(useRegisterRequest.getPassword())
                .name(useRegisterRequest.getName())
                .dateOfBirth(useRegisterRequest.getDateOfBirth())
                .avatar(useRegisterRequest.getAvatar())
                .phoneNumber(useRegisterRequest.getPhoneNumber())
                .gender(useRegisterRequest.isGender())
                .build());
    }

    @Override
    public TempUser findByEmail(String email) throws Exception {
        return tempUserRepository.findByEmail(email).orElse(null);
    }

    @Override
    public TempUser updateByEmail(String email, UseRegisterRequest useRegisterRequest) throws Exception {
        return tempUserRepository.findByEmail(email).map(tempUser -> {
            tempUser.setEmail(useRegisterRequest.getEmail());
            tempUser.setPassword(useRegisterRequest.getPassword());
            tempUser.setName(useRegisterRequest.getName());
            tempUser.setDateOfBirth(useRegisterRequest.getDateOfBirth());
            tempUser.setAvatar(useRegisterRequest.getAvatar());
            tempUser.setPhoneNumber(useRegisterRequest.getPhoneNumber());
            tempUser.setGender(useRegisterRequest.isGender());
            return tempUserRepository.save(tempUser);
        }).orElseThrow(()->new Exception("Email not found"));
    }

    @Override
    public TempUser deleteByEmail(String email) {
        return tempUserRepository.findByEmail(email).map(tempUser -> {
            tempUserRepository.delete(tempUser);
            return tempUser;
        }).orElse(null);
    }
}
