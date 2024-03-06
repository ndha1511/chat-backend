package com.project.chatbackend.services;

import com.project.chatbackend.configs.UserDetailConfig;
import com.project.chatbackend.exceptions.DataNotFoundException;
import com.project.chatbackend.models.Token;
import com.project.chatbackend.models.User;
import com.project.chatbackend.repositories.TokenRepository;
import com.project.chatbackend.repositories.UserRepository;
import com.project.chatbackend.requests.UseRegisterRequest;
import com.project.chatbackend.requests.UserLoginRequest;
import com.project.chatbackend.responses.LoginResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeoutException;

@Service
@RequiredArgsConstructor
public class UserService implements IUserService {
    private static final int MAX_DEVICE_LOGIN = 3;
    private final UserRepository userRepository;
    private final PasswordEncoder encoder;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final TokenRepository tokenRepository;
    @Value("${jwt.expiration}")
    private long expiration;
    @Value("${jwt.expiration-refresh-token}")
    private long expirationRefreshToken;
    @Override
    public User createUser(UseRegisterRequest useRegisterRequest) throws Exception{
        String phoneNumber = useRegisterRequest.getPhoneNumber();
        if(userRepository.existsByPhoneNumber(phoneNumber))
            throw new Exception("phone number already exist");
        User user = User.builder()
                .phoneNumber(phoneNumber)
                .name(useRegisterRequest.getName())
                .gender(useRegisterRequest.isGender())
                .dateOfBirth(useRegisterRequest.getDateOfBirth())
                .avatar(useRegisterRequest.getAvatar())
                .createdAt(LocalDateTime.now())
                .password(encoder.encode(useRegisterRequest.getPassword()))
                .build();
        return userRepository.save(user);
    }

    @Override
    public LoginResponse login(UserLoginRequest userLoginRequest) throws Exception{
        final String phoneNumber = userLoginRequest.getPhoneNumber();
        final String password = userLoginRequest.getPassword();
        Optional<User> optionalUser = userRepository.findByPhoneNumber(phoneNumber);
        if(optionalUser.isEmpty()) {
            throw new DataNotFoundException("phone number not exist");
        } else {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(phoneNumber, password)
            );
            User user = optionalUser.get();
            UserDetailConfig userDetailConfig = new UserDetailConfig(user);
            var jwt = jwtService.generateToken(userDetailConfig);
            var refreshToken = jwtService.generateRefreshToken(new HashMap<>(), userDetailConfig);
            Token token = Token.builder()
                    .token(jwt)
                    .userId(user.getId())
                    .refreshToken(refreshToken)
                    .tokenType("Bearer")
                    .revoked(false)
                    .isMobile(userLoginRequest.isMobile())
                    .expirationDate(LocalDateTime.now().plusSeconds(expiration))
                    .expirationDateRefreshToken(LocalDateTime.now().plusSeconds(expirationRefreshToken))
                    .build();

            List<Token> tokens = tokenRepository.findAllByUserId(user.getId());
            if(tokens.size() >= MAX_DEVICE_LOGIN) {
               Token tokenDelete = tokens.stream()
                       .filter(t -> !t.isMobile())
                       .min(Comparator.comparing(Token::getExpirationDate))
                       .orElseThrow();
               tokenRepository.deleteById(tokenDelete.getId());
               tokenRepository.save(token);
            }

            tokenRepository.save(token);
            return LoginResponse.builder()
                    .token(jwt)
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
            String phoneNumber = jwtService.extractUsername(refreshToken);
            User user = userRepository.findByPhoneNumber(phoneNumber)
                    .orElseThrow(() -> new UsernameNotFoundException("not found"));
            UserDetailConfig userDetailConfig = new UserDetailConfig(user);
            String newToken = jwtService.generateToken(userDetailConfig);
            token.setToken(newToken);
            token.setExpirationDate(LocalDateTime.now().plusSeconds(expiration));
            tokenRepository.save(token);
            return LoginResponse.builder()
                    .token(newToken)
                    .refreshToken(refreshToken)
                    .build();
        }

        throw new DataNotFoundException("token not found");
    }
}
