package com.dat.backend.movied.auth.serivce.impl;

import com.dat.backend.movied.auth.entity.UserAuth;
import com.dat.backend.movied.auth.entity.UserLogin;
import com.dat.backend.movied.auth.repository.UserLoginRepository;
import com.dat.backend.movied.auth.serivce.UserAuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserAuthServiceImpl implements UserAuthService {
    private final UserLoginRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String email) {
        UserLogin user = userRepository.findByUsername(email).orElseThrow(() -> new UsernameNotFoundException("User Not Found with email: " + email));
        return new UserAuth(user);
    }
}
