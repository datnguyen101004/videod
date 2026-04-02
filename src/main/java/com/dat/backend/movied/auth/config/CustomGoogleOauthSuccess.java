package com.dat.backend.movied.auth.config;

import com.dat.backend.movied.auth.entity.LoginMethod;
import com.dat.backend.movied.auth.entity.Role;
import com.dat.backend.movied.user.entity.User;
import com.dat.backend.movied.auth.repository.UserLoginRepository;
import com.dat.backend.movied.auth.serivce.JwtService;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
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

    @Value("${FE_URL}")
    private String FE_URL;

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
        Optional<User> userLogin = userLoginRepository.findByEmail(email);

        if (userLogin.isEmpty()) {
            User newUser = new User();
            newUser.setEmail(email);
            newUser.setRole(Role.USER);
            newUser.setLoginMethod(LoginMethod.OAUTH);
            userLoginRepository.save(newUser);
        }

        // Generate token
        String accessToken = jwtService.generateAccessToken(email);
        String refreshToken = jwtService.generateRefreshToken(email);

        Cookie refreshCookie = new Cookie("refresh_token", refreshToken);
        refreshCookie.setHttpOnly(false);
        refreshCookie.setSecure(false);
        refreshCookie.setPath("/");
        refreshCookie.setMaxAge(3 * 24 * 60 * 60); // 3 ngày

        response.addCookie(refreshCookie);

        String redirectUri = FE_URL + "/login/success?token=" + accessToken;
        response.sendRedirect(redirectUri);
    }
}
