package com.dat.backend.movied.video.service;

import com.dat.backend.movied.video.dto.*;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.services.s3.model.CompleteMultipartUploadResponse;

import java.util.List;

public interface VideoService {
    public VideoResponse uploadVideo(MultipartFile file, CreateVideoDto createVideoDto, String email);

    String downloadVideo(VideoDownloadRequest videoDownloadRequest);

    List<VideoResponse> getAllVideo();

    String deleteVideo(Long videoId, String email);

    MultipartInitiateResponse initiateMultipartUpload(UploadInitiateRequest uploadInitiateRequest);

    String getPresignedPartUrl(String key, String uploadId, int partNumber);

    CompleteMultipartUploadResponse completeMultipartUpload(CompleteMultipartRequest completeMultipartRequest);

    String abortMultipartUpload(AbortVideoRequest abortVideoRequest);

    String trigger();
}
