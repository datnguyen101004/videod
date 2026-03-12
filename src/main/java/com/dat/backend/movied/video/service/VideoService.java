package com.dat.backend.movied.video.service;

import com.dat.backend.movied.video.dto.CreateVideoDto;
import com.dat.backend.movied.video.dto.VideoDownloadRequest;
import com.dat.backend.movied.video.dto.VideoResponse;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.services.s3.model.PutObjectResponse;

import java.io.IOException;
import java.util.List;

public interface VideoService {
    public VideoResponse uploadVideo(MultipartFile file, CreateVideoDto createVideoDto);

    String downloadVideo(VideoDownloadRequest videoDownloadRequest);

    List<VideoResponse> getAllVideo();
}
