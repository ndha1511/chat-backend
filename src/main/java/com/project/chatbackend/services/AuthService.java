package com.project.chatbackend.services;

import com.project.chatbackend.exceptions.PermissionAccessDenied;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {
    private final JwtService jwtService;

    public void AuthenticationToken(HttpServletRequest httpServletRequest, String senderId) throws PermissionAccessDenied {
        String token = httpServletRequest.getHeader("Authorization");
        token = token.substring(7);
        String username =  jwtService.extractUsername(token);
        if(!username.equals(senderId)) throw new PermissionAccessDenied("Permission Access Denied");
    }

}
