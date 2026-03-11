package com.dat.backend.movied.auth.config;

import com.dat.backend.movied.auth.dto.AuthResponse;
import com.dat.backend.movied.auth.entity.LoginMethod;
import com.dat.backend.movied.auth.entity.Role;
import com.dat.backend.movied.auth.entity.UserLogin;
import com.dat.backend.movied.auth.repository.UserLoginRepository;
import com.dat.backend.movied.auth.serivce.JwtService;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;

import javax.security.sasl.AuthenticationException;
import java.io.IOException;
import java.util.Optional;

@Configuration
public class CustomGoogleOauthSuccess implements AuthenticationSuccessHandler {
    private final JwtService jwtService;
    private final UserLoginRepository userLoginRepository;

    public CustomGoogleOauthSuccess(JwtService jwtService,
                                    UserLoginRepository userLoginRepository) {
        this.jwtService = jwtService;
        this.userLoginRepository = userLoginRepository;
    }
    @Override
    public void onAuthenticationSuccess(HttpServletRequest request,
                                        HttpServletResponse response,
                                        Authentication authentication) throws IOException, ServletException {

        //System.out.println(authentication.toString());
        OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();

        if (oAuth2User == null) {
            throw new AuthenticationException("No OAuth2User found");
        }

        // Get information
        //System.out.println(oAuth2User);
        String email = oAuth2User.getAttribute("email");

        // Create user if not exist
        Optional<UserLogin> userLogin = userLoginRepository.findByUsername(email);

        if (userLogin.isEmpty()) {
            UserLogin newUserLogin = new UserLogin();
            newUserLogin.setUsername(email);
            newUserLogin.setRole(Role.USER);
            newUserLogin.setLoginMethod(LoginMethod.OAUTH);
            userLoginRepository.save(newUserLogin);
        }

        // Generate token
        String accessToken = jwtService.generateAccessToken(email);
        String refreshToken = jwtService.generateRefreshToken(email);

        // Set cookie
        Cookie cookie1 = new Cookie("access_token", accessToken);
        cookie1.setPath("/");
        response.addCookie(cookie1);

        Cookie cookie2 = new Cookie("refresh_token", refreshToken);
        cookie2.setPath("/");
        response.addCookie(cookie2);

        String redirectUri = "http://localhost:8080/auth";
        response.sendRedirect(redirectUri);
    }
}
