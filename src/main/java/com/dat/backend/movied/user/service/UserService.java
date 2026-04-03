package com.dat.backend.movied.user.service;

import com.dat.backend.movied.user.dto.UserResponse;
import com.dat.backend.movied.user.entity.User;
import com.dat.backend.movied.user.repository.UserRepository;
import com.dat.backend.movied.video.dto.response.VideoResponse;
import com.dat.backend.movied.video.entity.Video;
import com.dat.backend.movied.video.repository.VideoRepository;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final VideoRepository videoRepository;

    public UserService(UserRepository userRepository,
                       VideoRepository videoRepository) {
        this.userRepository = userRepository;
        this.videoRepository = videoRepository;
    }

    public List<UserResponse> getAllUsers() {
        List<User> users = userRepository.findAll();

        List<UserResponse> userResponses = new ArrayList<>();

        for (User user : users) {
            UserResponse userResponse = new UserResponse();
            userResponse.setEmail(user.getEmail());
            userResponse.setId(user.getId());
            userResponse.setPlan(user.getPlan().toString());
            userResponse.setRole(user.getRole().toString());
            userResponses.add(userResponse);
        }

        return userResponses;
    }

    public List<VideoResponse> getMyVideos(String email) {
        List<Video> myVideos = videoRepository.findAllByAuthorEmail(email);

        List<VideoResponse> videoResponses = new ArrayList<>();

        for (Video video : myVideos) {
            VideoResponse videoResponse = new VideoResponse();
            videoResponse.setId(video.getId());
            videoResponse.setTitle(video.getTitle());
            videoResponse.setDescription(video.getDescription());
            videoResponse.setUrl(video.getUrl());
            videoResponse.setAuthorName(email);
            videoResponse.setCategory(video.getCategory().toString());
            videoResponses.add(videoResponse);
        }

        return videoResponses;
    }
}
