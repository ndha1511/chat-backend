package com.project.zalobackend.services;

import com.project.zalobackend.configs.UserDetailConfig;
import com.project.zalobackend.models.User;
import com.project.zalobackend.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserDetailService implements UserDetailsService {
    private final UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Optional<User> user = userRepository.findByPhoneNumber(username);
        return user.map(UserDetailConfig::new).orElseThrow(() -> new UsernameNotFoundException("user not found"));
    }
}
