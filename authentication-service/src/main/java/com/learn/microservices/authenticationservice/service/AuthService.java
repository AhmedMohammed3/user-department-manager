package com.learn.microservices.authenticationservice.service;

import com.learn.microservices.authenticationservice.dto.LoginRequest;
import com.learn.microservices.authenticationservice.dto.LoginResponse;
import com.learn.microservices.authenticationservice.entity.User;
import com.learn.microservices.authenticationservice.exception.UserNotFoundException;
import com.learn.microservices.authenticationservice.repository.UserRepository;
import com.learn.microservices.authenticationservice.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    public LoginResponse login(LoginRequest loginRequest) {
        User user = userRepository.getByEmail(loginRequest.getEmail()).orElse(null);
        if (user == null || !passwordEncoder.matches(loginRequest.getPassword(), user.getPassword())) {
            throw new UserNotFoundException("User not found");
        }
        String token = jwtUtil.generateToken(user);
        return new LoginResponse(token);
    }
}
