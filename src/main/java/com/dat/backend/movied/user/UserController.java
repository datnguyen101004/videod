package com.dat.backend.movied.user;

import com.dat.backend.movied.common.dto.ResponseApi;
import com.dat.backend.movied.user.dto.UserResponse;
import com.dat.backend.movied.user.entity.User;
import com.dat.backend.movied.user.service.UserService;
import com.dat.backend.movied.video.dto.response.VideoResponse;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/api/v1/user")
public class UserController {
    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/all")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseApi<List<UserResponse>> getAllUsers() {
        return ResponseApi.success(userService.getAllUsers());
    }

    @GetMapping("/myvideo")
    public ResponseApi<List<VideoResponse>> getMyVideos(Authentication authentication) {
        String email = authentication.getName();

        return ResponseApi.success(userService.getMyVideos(email));
    }
}
