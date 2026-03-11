package com.dat.backend.movied.auth;

import com.dat.backend.movied.auth.dto.AuthRequest;
import com.dat.backend.movied.auth.dto.AuthResponse;
import com.dat.backend.movied.auth.serivce.AuthService;
import com.dat.backend.movied.common.dto.ResponseApi;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RequiredArgsConstructor
@RestController
@RequestMapping("/auth")
public class AuthController {
    private final AuthService authService;

    @PostMapping("/login")
    public ResponseApi<AuthResponse> login(@RequestBody AuthRequest authRequest) {
        return ResponseApi.success(authService.login(authRequest));
    }

    @PostMapping("/register")
    public ResponseApi<AuthResponse> register(@RequestBody AuthRequest authRequest) {
        return ResponseApi.success(authService.register(authRequest));
    }
}
