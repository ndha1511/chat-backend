package com.project.zalobackend.services;

import com.project.zalobackend.configs.UserDetailConfig;
import com.project.zalobackend.exceptions.DataNotFoundException;
import com.project.zalobackend.models.Token;
import com.project.zalobackend.models.User;
import com.project.zalobackend.repositories.UserRepository;
import com.project.zalobackend.requests.UseRegisterRequest;
import com.project.zalobackend.requests.UserLoginRequest;
import com.project.zalobackend.responses.LoginResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserService implements IUserService {
    private static final int MAX_DEVICE_LOGIN = 3;
    private final UserRepository userRepository;
    private final PasswordEncoder encoder;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final ITokenService tokenService;
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
                    .expirationDate(LocalDateTime.now().plusSeconds(expiration))
                    .expirationDateRefreshToken(LocalDateTime.now().plusSeconds(expirationRefreshToken))
                    .build();
            List<Token> tokens = tokenService.findAllByUserId(user.getId());
            if(tokens.size() >= 3) {
               Token tokenDelete = tokens.stream()
                       .filter(t -> !t.isMobile())
                       .min(Comparator.comparing(Token::getExpirationDate))
                       .orElseThrow();
               tokenService.delete(tokenDelete.getId());
               tokenService.save(token);
            }

            tokenService.save(token);
            return LoginResponse.builder()
                    .token(jwt)
                    .refreshToken(refreshToken)
                    .build();
        }

    }
}
