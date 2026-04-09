package com.dat.backend.movied.video;

import com.dat.backend.movied.common.dto.ResponseApi;
import com.dat.backend.movied.video.dto.request.*;
import com.dat.backend.movied.video.dto.response.*;
import com.dat.backend.movied.video.service.VideoService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import software.amazon.awssdk.services.s3.model.CompleteMultipartUploadResponse;

import java.time.Instant;

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
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
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

    @PostMapping("/upload/multipart/initiate")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ResponseEntity<MultipartInitiateResponse> initiateMultipart(
            @RequestBody UploadInitiateRequest request) {
        return ResponseEntity.ok(videoService.initiateMultipartUpload(request));
    }

    @PostMapping("/upload/multipart/part-url")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
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
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ResponseEntity<CompleteMultipartUploadResponse> completeMultipart(
            @RequestBody CompleteMultipartRequest request,
            Authentication authentication) {

        return ResponseEntity.ok(videoService.completeMultipartUpload(request, authentication.getName()));
    }

    @PostMapping("/abort")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ResponseEntity<String> abortMultipartUpload(
            @RequestBody AbortPartRequest abortPartRequest
    ) {
        return ResponseEntity.ok(videoService.abortMultipartUpload(abortPartRequest));
    }

    @PostMapping("/trigger")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ResponseEntity<String> triggerVideo() {
        return ResponseEntity.ok(videoService.trigger());
    }

    @DeleteMapping("/delete/{videoId}")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ResponseApi<String> deleteVideo(@PathVariable("videoId") Long videoId,
                                           Authentication authentication) {
        return ResponseApi.success(videoService.deleteVideo(videoId, authentication.getName()));
    }

    @PutMapping("/update")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ResponseApi<VideoResponse> updateVideo(@RequestBody UpdateVideoRequest request,
                                                  Authentication authentication) {
        String title = request.getTitle();
        String description = request.getDescription();
        Long videoId = request.getVideoId();
        String email = authentication.getName();

        return ResponseApi.success(videoService.updateVideo(title, description, videoId, email));
    }

    @GetMapping("/relate/{id}")
    public ResponseApi<PagesResponse> getRelateVideos(@PathVariable("id") Long videoId,
                                                            @RequestParam(name = "cursor", required = false) String cursor) {
        return ResponseApi.success(videoService.findRelateVideo(videoId, cursor));
    }

    @GetMapping("/all")
    public ResponseApi<PagesResponse> getAllVideos(@RequestParam(name = "cursor", required = false) String cursor) {
        return ResponseApi.success(videoService.getAllVideos(cursor));
    }

    @GetMapping("/search")
    public ResponseApi<PagesResponse> searchWithSpecifications(
            @RequestParam(value = "title", required = false) String title,
            @RequestParam(value = "category", required = false) String category,
            @RequestParam(value = "description", required = false) String description,
            @RequestParam(value = "before_date", required = false) Instant beforeDate,
            @RequestParam(value = "after_date", required = false) Instant afterDate,
            @RequestParam(value = "cursor", required = false) String cursor
            ) {
        return ResponseApi.success(videoService.searchWithPredicate(title, category, description, beforeDate, afterDate, cursor));
    }
}
