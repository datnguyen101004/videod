package com.dat.backend.movied.video;

import com.dat.backend.movied.video.dto.*;
import com.dat.backend.movied.common.dto.ResponseApi;
import com.dat.backend.movied.video.service.VideoService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import software.amazon.awssdk.services.s3.model.CompleteMultipartUploadResponse;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/video")
public class VideoController {
    private final VideoService videoService;

    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    @PostMapping(path = "/upload/small")
    public ResponseApi<PresignedUrlResponse> createPresignUrlSmallVideo(@RequestBody PresignUploadRequest presignUploadRequest,
                                                  Authentication authentication) {
        return ResponseApi.success(videoService.createPresignUrlSmallVideo(presignUploadRequest, authentication.getName()));
    }

    @PostMapping("/verify")
    public ResponseEntity<String> verifyAndSaveToDatabase(
            @RequestBody VerifyUploadPresign verifyUploadPresign,
            Authentication authentication
    )  {
        return ResponseEntity.ok(videoService.verifyAndSaveToDatabase(verifyUploadPresign, authentication.getName()));
    }

    @PostMapping(path = "/download")
    public ResponseApi<String> downloadVideo(@RequestBody VideoDownloadRequest videoDownloadRequest) {
        return ResponseApi.success(videoService.downloadVideo(videoDownloadRequest));
    }

    @GetMapping("/all")
    public ResponseApi<List<VideoResponse>> getAllVideos() {
        return ResponseApi.success(videoService.getAllVideo());
    }

    @DeleteMapping("/delete")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ResponseApi<String> deleteVideo(@RequestParam("videoId") Long videoId,
                                           Authentication authentication) {
        return ResponseApi.success(videoService.deleteVideo(videoId, authentication.getName()));
    }

    @PostMapping("/upload/multipart/initiate")
    public ResponseEntity<MultipartInitiateResponse> initiateMultipart(
            @RequestBody UploadInitiateRequest request) {
        return ResponseEntity.ok(videoService.initiateMultipartUpload(request));
    }

    @PostMapping("/upload/multipart/part-url")
    public ResponseEntity<PartUrlResponse> getPartUrl(
            @RequestBody PartUrlRequest request) {

        String url = videoService.getPresignedPartUrl(
                request.getKey(),
                request.getUploadId(),
                request.getPartNumber()
        );

        return ResponseEntity.ok(new PartUrlResponse(url));
    }

    @PostMapping("/upload/multipart/complete")
    public ResponseEntity<CompleteMultipartUploadResponse> completeMultipart(
            @RequestBody CompleteMultipartRequest request,
            Authentication authentication) {

        return ResponseEntity.ok(videoService.completeMultipartUpload(request, authentication.getName()));
    }

    @PostMapping("/abort")
    public ResponseEntity<String> abortMultipartUpload(
            @RequestBody AbortPartRequest abortPartRequest
    ) {
        return ResponseEntity.ok(videoService.abortMultipartUpload(abortPartRequest));
    }

    @PostMapping("/trigger")
    public ResponseEntity<String> triggerVideo() {
        return ResponseEntity.ok(videoService.trigger());
    }
}
