package com.dat.backend.movied.video;

import com.dat.backend.movied.video.dto.CreateVideoDto;
import com.dat.backend.movied.video.dto.ResponseApi;
import com.dat.backend.movied.video.dto.VideoResponse;
import com.dat.backend.movied.video.service.VideoService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequiredArgsConstructor
public class VideoController {
    private final VideoService videoService;

    @PostMapping("/upload")
    public ResponseApi<VideoResponse> uploadVideo(@RequestPart("video") MultipartFile file,
                                                  @RequestPart("information") CreateVideoDto createVideoDto) {
        return ResponseApi.success(videoService.uploadVideo(file, createVideoDto));
    }
}
