package com.dat.backend.movied.video.service;

import com.dat.backend.movied.video.dto.*;
import software.amazon.awssdk.services.s3.model.CompleteMultipartUploadResponse;

import java.util.List;

public interface VideoService {
    public PresignedUrlResponse createPresignUrlSmallVideo(PresignUploadRequest presignUploadRequest, String email);

    String downloadVideo(VideoDownloadRequest videoDownloadRequest);

    List<VideoResponse> getAllVideo();

    String deleteVideo(Long videoId, String email);

    MultipartInitiateResponse initiateMultipartUpload(UploadInitiateRequest uploadInitiateRequest);

    String getPresignedPartUrl(String key, String uploadId, int partNumber);

    CompleteMultipartUploadResponse completeMultipartUpload(CompleteMultipartRequest completeMultipartRequest, String email);

    String abortMultipartUpload(AbortPartRequest abortPartRequest);

    String trigger();

    String verifyAndSaveToDatabase(VerifyUploadPresign verifyUploadPresign, String name);
}
