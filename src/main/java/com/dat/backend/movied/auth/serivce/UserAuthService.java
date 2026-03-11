package com.dat.backend.movied.auth.serivce;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;

public interface UserAuthService extends UserDetailsService {
    public UserDetails loadUserByUsername(String username);
}
