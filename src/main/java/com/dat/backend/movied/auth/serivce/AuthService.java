package com.dat.backend.movied.auth.serivce;

import com.dat.backend.movied.auth.dto.AuthRequest;
import com.dat.backend.movied.auth.dto.AuthResponse;
import jakarta.servlet.http.HttpSession;

public interface AuthService {
    AuthResponse login(AuthRequest authRequest);

    AuthResponse register(AuthRequest authRequest);

}
