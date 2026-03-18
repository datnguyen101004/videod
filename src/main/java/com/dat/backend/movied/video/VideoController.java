package com.dat.backend.movied.video;

import com.dat.backend.movied.video.dto.*;
import com.dat.backend.movied.common.dto.ResponseApi;
import com.dat.backend.movied.video.service.VideoService;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.services.s3.model.CompleteMultipartUploadResponse;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/video")
public class VideoController {
    private final VideoService videoService;

    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    @PostMapping(path = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseApi<VideoResponse> uploadVideo(@RequestPart("video") MultipartFile file,
                                                  @Parameter(
                                                          description = "Video metadata",
                                                          content = @Content(mediaType = "application/json")
                                                  )
                                                  @RequestPart("information") CreateVideoDto createVideoDto,
                                                  Authentication authentication) {
        return ResponseApi.success(videoService.uploadVideo(file, createVideoDto, authentication.getName()));
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
            @RequestBody CompleteMultipartRequest request) {

        return ResponseEntity.ok(videoService.completeMultipartUpload(request));
    }

    @PostMapping("/abort")
    public ResponseEntity<String> abortMultipartUpload(
            @RequestBody AbortVideoRequest abortVideoRequest
    ) {
        return ResponseEntity.ok(videoService.abortMultipartUpload(abortVideoRequest));
    }

    @PostMapping("/trigger")
    public ResponseEntity<String> triggerVideo() {
        return ResponseEntity.ok(videoService.trigger());
    }
}
