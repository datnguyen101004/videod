package com.dat.backend.movied.video.service;

import com.dat.backend.movied.video.dto.CreateVideoDto;
import com.dat.backend.movied.video.dto.VideoResponse;
import org.springframework.web.multipart.MultipartFile;

public interface VideoService {
    public VideoResponse uploadVideo(MultipartFile file, CreateVideoDto createVideoDto);
}
