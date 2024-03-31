package com.project.chatbackend.services;

import com.project.chatbackend.models.Otp;
import com.project.chatbackend.models.TempUser;
import com.project.chatbackend.repositories.OTPRepository;
import com.project.chatbackend.repositories.UserRepository;
import com.project.chatbackend.requests.OtpRequest;
import com.project.chatbackend.requests.OtpValidRequest;
import com.project.chatbackend.requests.UseRegisterRequest;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.ClassPathResource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.Random;

@Service
@RequiredArgsConstructor
public class OTPService implements IOtpService {

    private final JavaMailSender javaMailSender;
    private final  OTPRepository otpRepository;
    private final UserRepository userRepository;
    private final UserService userService;
    private final TempUserService tempUserService;
    @Override
    public boolean sendOTP(OtpRequest otpRequest) {
        try{
            if(userRepository.existsByEmail(otpRequest.getEmail())){
                throw new Exception("email is exists");
            }else{
                TempUser tempUser = tempUserService.findByEmail(otpRequest.getEmail());
                UseRegisterRequest useRegisterRequest = UseRegisterRequest.builder()
                        .password(otpRequest.getPassword())
                        .email(otpRequest.getEmail())
                        .name(otpRequest.getName())
                        .dateOfBirth(otpRequest.getDateOfBirth())
                        .gender(otpRequest.isGender())
                        .avatar(otpRequest.getAvatar())
                        .phoneNumber(otpRequest.getPhoneNumber())
                        .build();
                if(tempUser == null){
                    tempUserService.createUser(useRegisterRequest);
                }else{
                   tempUserService.updateByEmail(otpRequest.getEmail(), useRegisterRequest);
                }
            }
            String otpToken = generateOTP();
            Timestamp timestamp = Timestamp.valueOf(LocalDateTime.now());
            long updateAt = timestamp.getTime();
            Otp otp = Otp.builder().otp(otpToken).email(otpRequest.getEmail()).createdAt(updateAt).build();
            saveOTP(otp);
            String htmlContent = loadHtmlTemplate();
            htmlContent = htmlContent.replace("codeOtp", otpToken);
            htmlContent = htmlContent.replace("userName", otpRequest.getEmail());
            sendHtmlEmail(otpRequest.getEmail(), htmlContent);
            return true;
        }catch (Exception e) {
            throw new RuntimeException("Error while sending OTP:"+e.getMessage());
        }
    }

    @Override
    public String verifyOTP(OtpValidRequest otpValidRequest) {
        try{
            Otp otp = getOTP(otpValidRequest.getEmail(), otpValidRequest.getOtp());
            Timestamp timestamp = Timestamp.valueOf(LocalDateTime.now());
            long diffSeconds = (timestamp.getTime() - otp.getCreatedAt()) / 1000;
            removeOTP(otpValidRequest.getEmail());
            if (diffSeconds > 180) {
                return "expired";
            }
            String otpToken = otp.getOtp();
            if(otpToken == null){
                return "not exist";
            }
            if(otpToken.equals(otpValidRequest.getOtp())){
                TempUser tempUser = tempUserService.findByEmail(otpValidRequest.getEmail());
                UseRegisterRequest useRegisterRequest = UseRegisterRequest.builder()
                        .password(tempUser.getPassword())
                        .email(tempUser.getEmail())
                        .name(tempUser.getName())
                        .dateOfBirth(tempUser.getDateOfBirth())
                        .gender(tempUser.isGender())
                        .avatar(tempUser.getAvatar())
                        .phoneNumber(tempUser.getPhoneNumber())
                        .build();
                userService.createUser(useRegisterRequest);
                return "valid";
            }else{
                return "invalid";
            }
        }catch (RuntimeException e) {
           return "invalid";
        }catch (Exception e) {
            return "cannot find user";
        }
    }

    @Override
    public Otp saveOTP(Otp otp) {
        try{
            return otpRepository.save(otp);
        }catch (Exception e) {
            throw new RuntimeException("Error while saving Otp:"+e.getMessage());
        }
    }


    @Override
    public void removeOTP(String email) {
        try{
            otpRepository.deleteByEmail(email);
        }catch (Exception e) {
            throw new RuntimeException("Error while removing Otp");
        }

    }

    @Override
    public Otp getOTP(String email, String otp) {
        return otpRepository.findByEmailAndOtp(email, otp).orElseThrow(()->new RuntimeException("Otp not found"));
    }


    private static String generateOTP(){
        Random random = new Random();
        int otpLength = 6;
        StringBuilder sb= new StringBuilder(otpLength);
        for(int i=0;i<otpLength;i++){
            sb.append(random.nextInt(10));
        }
        return sb.toString();
    }

    private String loadHtmlTemplate() throws IOException {
        try(InputStream inputStream = new ClassPathResource("templates/OTP.html").getInputStream()) {
            return new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);
        }
    }

    private void sendHtmlEmail(String to, String htmlBody) throws MessagingException {
        MimeMessage message = javaMailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
        helper.setTo(to);
        helper.setSubject("OTP for register");
        helper.setText(htmlBody, true);
        javaMailSender.send(message);
    }



}
