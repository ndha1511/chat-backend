package com.project.chatbackend.services;

import com.project.chatbackend.configs.UserDetailConfig;
import com.project.chatbackend.exceptions.DataNotFoundException;
import com.project.chatbackend.models.Otp;
import com.project.chatbackend.models.Token;
import com.project.chatbackend.models.User;
import com.project.chatbackend.repositories.OTPRepository;
import com.project.chatbackend.repositories.TokenRepository;
import com.project.chatbackend.repositories.UserRepository;
import com.project.chatbackend.requests.*;
import com.project.chatbackend.responses.LoginResponse;
import com.project.chatbackend.responses.UserLoginResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeoutException;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserService implements IUserService {
//    @Value("${login.max-device}")
////    private int MAX_DEVICE_LOGIN;
    private final UserRepository userRepository;
    private final PasswordEncoder encoder;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final TokenRepository tokenRepository;
    private final OTPRepository otpRepository;
    @Value("${jwt.expiration}")
    private long expiration;
    @Value("${jwt.expiration-refresh-token}")
    private long expirationRefreshToken;
    @Override
    public User createUser(UseRegisterRequest useRegisterRequest) throws Exception{
        String email = useRegisterRequest.getEmail();
        if(userRepository.existsByEmail(email))
            throw new Exception("email already exist");
        User user = User.builder()
                .phoneNumber(useRegisterRequest.getPhoneNumber())
                .name(useRegisterRequest.getName())
                .gender(useRegisterRequest.isGender())
                .dateOfBirth(useRegisterRequest.getDateOfBirth())
                .avatar(useRegisterRequest.getAvatar())
                .createdAt(LocalDateTime.now())
                .isVerified(true)
                .password(encoder.encode(useRegisterRequest.getPassword()))
                .email(email)
                .build();
        return userRepository.save(user);
    }

    @Override
    public LoginResponse login(UserLoginRequest userLoginRequest) throws Exception{
        final String email = userLoginRequest.getEmail();
        final String password = userLoginRequest.getPassword();
        Optional<User> optionalUser = userRepository.findByEmail(email);
        if(optionalUser.isEmpty()) {
            throw new DataNotFoundException("email is not exists");
        } else {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(email, password)
            );
            User user = optionalUser.get();
            UserDetailConfig userDetailConfig = new UserDetailConfig(user);
            var jwt = jwtService.generateToken(userDetailConfig);
            var refreshToken = jwtService.generateRefreshToken(new HashMap<>(), userDetailConfig);
            Token token = Token.builder()
                    .accessToken(jwt)
                    .user(user)
                    .refreshToken(refreshToken)
                    .tokenType("Bearer")
                    .revoked(false)
                    .isMobile(userLoginRequest.isMobile())
                    .expirationDateAccessToken(LocalDateTime.now().plusSeconds(expiration))
                    .expirationDateRefreshToken(LocalDateTime.now().plusSeconds(expirationRefreshToken))
                    .build();

            List<Token> tokens = tokenRepository.findAllByUserEmail(user.getEmail());

            boolean mobile = userLoginRequest.isMobile();
            Optional<Token> tokenDelete = tokens.stream()
                    .filter(t -> t.isMobile() == mobile)
                    .min(Comparator.comparing(Token::getExpirationDateAccessToken));
            tokenDelete.ifPresent(value -> tokenRepository.deleteById(value.getId()));
            tokenRepository.save(token);
            return LoginResponse.builder()
                    .accessToken(jwt)
                    .refreshToken(refreshToken)
                    .build();
        }

    }

    @Override
    public LoginResponse refreshToken(String refreshToken) throws Exception {
        Optional<Token> optionalToken = tokenRepository.findByRefreshToken(refreshToken);
        if(optionalToken.isPresent()) {
            Token token = optionalToken.get();
            if(token.isRevoked() ||
                    token.getExpirationDateRefreshToken()
                            .isBefore(LocalDateTime.now()))
                throw new TimeoutException("refresh fail");
            String email = jwtService.extractUsername(refreshToken);
            User user = userRepository.findByEmail(email)
                    .orElseThrow(() -> new UsernameNotFoundException("not found"));
            UserDetailConfig userDetailConfig = new UserDetailConfig(user);
            String newToken = jwtService.generateToken(userDetailConfig);
            token.setAccessToken(newToken);
            token.setExpirationDateAccessToken(LocalDateTime.now().plusSeconds(expiration));
            tokenRepository.save(token);
            return LoginResponse.builder()
                    .accessToken(newToken)
                    .refreshToken(refreshToken)
                    .build();
        }

        throw new DataNotFoundException("token not found");
    }

    @Override
    public UserLoginResponse findByPhoneNumber(String phoneNumber) throws Exception {
        Optional<User> optionalUser = userRepository.findByPhoneNumber(phoneNumber);
        return convertUserResponse(optionalUser);
    }

    @Override
    public UserLoginResponse findByEmail(String email) throws Exception {
        Optional<User> optionalUser = userRepository.findByEmail(email);
        return convertUserResponse(optionalUser);
    }



    private UserLoginResponse convertUserResponse(Optional<User> optionalUser) throws DataNotFoundException {
        if(optionalUser.isPresent()) {
            User user = optionalUser.get();
            return UserLoginResponse.builder()
                    .avatar(user.getAvatar())
                    .name(user.getName())
                    .phoneNumber(user.getPhoneNumber())
                    .dob(String.valueOf(user.getDateOfBirth()))
                    .gender(user.isGender())
                    .coverImage(user.getCoverImage())
                    .images(user.getImages())
                    .email(user.getEmail())
                    .friends(user.getFriends())
                    .build();
        }
        throw new DataNotFoundException("user not found");
    }

    @Override
    public UserLoginResponse findById(String id) throws Exception {
        return userRepository.findById(id)
                .map(user -> UserLoginResponse
                        .builder()
                        .avatar(user.getAvatar())
                        .name(user.getName())
                        .phoneNumber(user.getPhoneNumber())
                        .gender(user.isGender())
                        .coverImage(user.getCoverImage())
                        .images(user.getImages())
                        .build())
                .orElseThrow(() -> new DataNotFoundException("user not found"));
    }




    @Override
    public User findUserByEmail(String email) throws DataNotFoundException {
        return userRepository.findByEmail(email).orElseThrow(()->new DataNotFoundException("user not found"));
    }

    @Override
    public boolean deleteUserByEmail(String email) {
        try{
            User user = userRepository.findByEmail(email).orElseThrow();
            userRepository.deleteById(user.getEmail());
            return true;
        }catch (Exception e){
            return false;
        }
    }
    @Override
    public boolean isValidOTP(OtpValidRequest otpValidRequest) {
        Optional<Otp> optionalOtp = otpRepository.findByEmailAndOtp(
                otpValidRequest.getEmail(),
                otpValidRequest.getOtp()
        );
        if(optionalOtp.isPresent()) {
            Otp otp = optionalOtp.get();
            return otp.getExpiredDate().isAfter(LocalDateTime.now());
        }
        return false;
    }
    @Override
    @Transactional
    public boolean changePassword(ChangePasswordRequest changePasswordRequest) {
        Optional<User> optionalUser = userRepository.findByEmail(changePasswordRequest.getEmail());
        if(optionalUser.isPresent()) {
            User user = optionalUser.get();
            if(isValidPassword(changePasswordRequest.getOldPassword(), user.getPassword())) {
                String newPassword = encoder.encode(changePasswordRequest.getNewPassword());
                user.setPassword(newPassword);
                userRepository.save(user);
                List<Token> tokens = tokenRepository.findAllByUserEmail(user.getEmail());
                if(!tokens.isEmpty()) {
                    for (Token token : tokens) {
                        tokenRepository.deleteById(token.getId());
                    }
                }
                return true;
            }
            return false;
        }
        return false;
    }

    @Override
    @Transactional
    public boolean resetPassword(ResetPasswordRequest resetPasswordRequest) {
        OtpValidRequest otpValidRequest = OtpValidRequest.builder()
                .email(resetPasswordRequest.getEmail())
                .otp(resetPasswordRequest.getOtp())
                .build();
        if(isValidOTP(otpValidRequest)) {
            Optional<User> optionalUser = userRepository.findByEmail(resetPasswordRequest.getEmail());
            if(optionalUser.isPresent()) {
                String newPassword = encoder.encode(resetPasswordRequest.getNewPassword());
                User user = optionalUser.get();
                user.setPassword(newPassword);
                userRepository.save(user);
                List<Token> tokens = tokenRepository.findAllByUserEmail(user.getEmail());
                if(!tokens.isEmpty()) {
                    for (Token token : tokens) {
                        tokenRepository.deleteById(token.getId());
                    }
                }
                return true;

            }
        }
        return false;
    }

    @Override
    public User updateUser(UserUpdateRequest updateUserRequest) throws Exception {
        Optional<User> optionalUser = userRepository.findByEmail(updateUserRequest.getEmail());
        if(optionalUser.isPresent()) {
            User user = optionalUser.get();
            user.setName(updateUserRequest.getName());
            user.setGender(updateUserRequest.isGender());
            user.setAvatar(updateUserRequest.getAvatar());
            user.setDateOfBirth(updateUserRequest.getDob());
            return userRepository.save(user);
        }else {
            throw new DataNotFoundException("user not found");
        }
    }

    @Override
    public List<UserLoginResponse> getFriends(String userId) {
        Optional<User> optionalUser = userRepository.findByEmail(userId);
        User user = optionalUser.orElseThrow();
        List<String> listFriends = user.getFriends();
        return listFriends.stream().map(id -> {
            Optional<User> optionalFriend = userRepository.findByEmail(id);
            User friend = optionalFriend.orElseThrow();
            return UserLoginResponse.builder()
                    .email(friend.getEmail())
                    .dob(String.valueOf(friend.getDateOfBirth()))
                    .avatar(friend.getAvatar())
                    .gender(friend.isGender())
                    .name(friend.getName())
                    .coverImage(friend.getCoverImage())
                    .images(friend.getImages())
                    .build();
        }).toList();
    }

    private boolean isValidPassword(String password, String passwordDb) {
        return encoder.matches(password, passwordDb);
    }
}
