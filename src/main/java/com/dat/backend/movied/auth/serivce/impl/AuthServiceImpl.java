package com.dat.backend.movied.auth.serivce.impl;

import com.dat.backend.movied.auth.dto.AuthRequest;
import com.dat.backend.movied.auth.dto.AuthResponse;
import com.dat.backend.movied.auth.entity.LoginMethod;
import com.dat.backend.movied.auth.entity.Role;
import com.dat.backend.movied.auth.entity.UserLogin;
import com.dat.backend.movied.auth.repository.UserLoginRepository;
import com.dat.backend.movied.auth.serivce.AuthService;
import com.dat.backend.movied.auth.serivce.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {
    private final AuthenticationManager authenticationManager;
    private final PasswordEncoder passwordEncoder;
    private final UserLoginRepository userRepository;
    private final JwtService jwtService;

    @Override
    public AuthResponse login(AuthRequest authRequest) {
        String username = authRequest.getUsername();
        String password = authRequest.getPassword();
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(username, password)
        );

        // Generate jwt token
        String accessToken = jwtService.generateAccessToken(username);
        String refreshToken = jwtService.generateRefreshToken(username);

        return AuthResponse.builder()
                .access_token(accessToken)
                .refresh_token(refreshToken)
                .build();
    }

    @Override
    public AuthResponse register(AuthRequest authRequest) {
        // Check existing user
        String username = authRequest.getUsername();
        Optional<UserLogin> userLogin = userRepository.findByUsername(username);
        if (userLogin.isPresent()) {
            throw new RuntimeException("Username is already in use");
        }

        // Create new User
        UserLogin newUserLogin = new UserLogin();
        newUserLogin.setUsername(username);
        newUserLogin.setPassword(passwordEncoder.encode(authRequest.getPassword()));
        newUserLogin.setRole(Role.USER);
        newUserLogin.setLoginMethod(LoginMethod.NORMAL);
        userRepository.save(newUserLogin);

        // Generate token
        String accessToken = jwtService.generateAccessToken(username);
        String refreshToken = jwtService.generateRefreshToken(username);

        return AuthResponse.builder()
                .access_token(accessToken)
                .refresh_token(refreshToken)
                .build();
    }
}
