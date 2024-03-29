package com.project.chatbackend.services;

import com.project.chatbackend.models.Otp;
import com.project.chatbackend.repositories.OTPRepository;
import com.project.chatbackend.requests.OtpRequest;
import com.project.chatbackend.requests.OtpValidRequest;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
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
    @Autowired
    private final JavaMailSender javaMailSender;
    @Autowired
    private final  OTPRepository otpRepository;
    @Override
    public boolean sendOTP(OtpRequest otpRequest) {
        String otpToken = generateOTP();
        try{
            Timestamp timestamp = Timestamp.valueOf(LocalDateTime.now());
            long updateAt = timestamp.getTime();
            Otp otp = Otp.builder().otp(otpToken).email(otpRequest.getEmail()).createdAt(updateAt).build();
            saveOTP(otp);
            String htmlContent = loadHtmlTemplate("templates/OTP.html");
            htmlContent = htmlContent.replace("codeOtp", otpToken);
            htmlContent = htmlContent.replace("userName", otpRequest.getEmail());
            sendHtmlEmail(otpRequest.getEmail(), otpRequest.getSubject(), htmlContent);
            return true;
        }catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Error while sending Otp");
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
                return "valid";
            }else{
                return "invalid";
            }
        }catch (RuntimeException e) {
           return "invalid";
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

    private String loadHtmlTemplate(String templateName) throws IOException {
        try(InputStream inputStream = new ClassPathResource(templateName).getInputStream()) {
            return new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);
        }
    }

    private void sendHtmlEmail(String to, String subject, String htmlBody) throws MessagingException {
        try{
            MimeMessage message = javaMailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(htmlBody, true);
            javaMailSender.send(message);
        } catch (Exception e) {
            throw e;
        }
    }



}
