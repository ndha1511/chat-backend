package com.project.chatbackend.repositories;

import com.project.chatbackend.models.Otp;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

public interface OTPRepository extends MongoRepository<Otp, String> {

    Optional<Otp> findByEmailAndOtp(String email, String otp);
    @Transactional
    void deleteByEmail(String email);

}
