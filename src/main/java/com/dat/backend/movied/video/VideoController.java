package com.dat.backend.movied.video;

import com.dat.backend.movied.video.dto.CreateVideoDto;
import com.dat.backend.movied.common.dto.ResponseApi;
import com.dat.backend.movied.video.dto.VideoDownloadRequest;
import com.dat.backend.movied.video.dto.VideoResponse;
import com.dat.backend.movied.video.service.VideoService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequiredArgsConstructor
public class VideoController {
    private final VideoService videoService;

    @PostMapping("/auth/upload")
    public ResponseApi<VideoResponse> uploadVideo(@RequestPart("video") MultipartFile file,
                                                  @RequestPart("information") CreateVideoDto createVideoDto) {
        return ResponseApi.success(videoService.uploadVideo(file, createVideoDto));
    }

    @PostMapping("/download")
    public ResponseApi<String> downloadVideo(@RequestBody VideoDownloadRequest videoDownloadRequest) {
        return ResponseApi.success(videoService.downloadVideo(videoDownloadRequest));
    }

    /*@PostMapping("/upload/async")
    public PutObjectResponse uploadVideoAsync(@RequestPart("video") MultipartFile file) throws IOException {
        return videoService.asyncClientMultipartUpload(file);
    }

    @PostMapping("/upload/multipart")
    public String uploadMultipart(@RequestPart("video") MultipartFile file) throws IOException {
        return videoService.multipartUpload(file);
    }*/
}
