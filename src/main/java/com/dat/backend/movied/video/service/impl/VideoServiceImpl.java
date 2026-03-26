package com.dat.backend.movied.video.service.impl;

import com.dat.backend.movied.common.config.MetricConfig;
import com.dat.backend.movied.video.config.S3Properties;
import com.dat.backend.movied.video.dto.*;
import com.dat.backend.movied.video.entity.Category;
import com.dat.backend.movied.video.entity.Video;
import com.dat.backend.movied.video.exception.ResourceNotExit;
import com.dat.backend.movied.video.exception.VideoUploadException;
import com.dat.backend.movied.video.repository.VideoRepository;
import com.dat.backend.movied.video.service.VideoService;
import com.dat.backend.movied.video.util.S3Helper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.*;

import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
public class VideoServiceImpl implements VideoService {
    private final VideoRepository videoRepository;
    private final S3Properties properties;
    private static final long CHUNK_SIZE = 10 * 1024 * 1024; // 10MB
    private final S3Presigner s3Presigner;
    private final S3Client s3Client;
    private final MetricConfig metricConfig;

    public VideoServiceImpl(VideoRepository videoRepository,
                            S3Client s3Client,
                            S3Properties properties,
                            S3Presigner s3Presigner,
                            MetricConfig metricConfig) {
        this.videoRepository = videoRepository;
        this.properties = properties;
        this.s3Presigner = s3Presigner;
        this.s3Client = s3Client;
        this.metricConfig = metricConfig;
    }

    // Upload video to DO space
    @Override
    public PresignedUrlResponse createPresignUrlSmallVideo(PresignUploadRequest presignUploadRequest, String email) {
        try {
            log.info("Uploading video...");
            // Get the enough properties
            String filename = presignUploadRequest.getFilename();
            long fileSize = presignUploadRequest.getFileSize();
            String category = presignUploadRequest.getCategory();

            // Check bad case
            if (filename == null) {
                throw new Exception("Filename is null");
            }

            if (fileSize == 0) {
                throw new Exception("File size is 0");
            }

            // Init key and bucket for upload request
            String key = S3Helper.createKey(filename, category);
            String bucket = properties.getBucket();

            PutObjectRequest putObjectRequest =
                    PutObjectRequest.builder()
                            .key(key)
                            .bucket(bucket)
                            .contentType(presignUploadRequest.getContentType())
                            .contentLength(fileSize)
                            .acl(ObjectCannedACL.PUBLIC_READ)
                            .build();

            PutObjectPresignRequest presignRequest =
                    PutObjectPresignRequest.builder()
                            .signatureDuration(Duration.ofMinutes(15))
                            .putObjectRequest(putObjectRequest)
                            .build();

            PresignedPutObjectRequest presignedPutObjectRequest =
                    s3Presigner.presignPutObject(presignRequest);

            String url = presignedPutObjectRequest.url().toString();

            log.info("Presign url: {}", url);

            return PresignedUrlResponse.builder()
                    .url(url)
                    .key(key)
                    .build();

        }
        catch (Exception e) {
            throw new VideoUploadException(e.getMessage());
        }
    }


    @Override
    public String downloadVideo(VideoDownloadRequest videoDownloadRequest) {
        // Get the video information
        Video video = videoRepository.findById(videoDownloadRequest.getVideoId())
                .orElseThrow(() -> new ResourceNotExit("Video not found"));

        // Generate the URL for download with GET request
        return createPresignedGetUrl(properties.getBucket(), video.getKeyStorage());
    }

    @Override
    public List<VideoResponse> getAllVideo() {
        List<Video> videos = videoRepository.findAll();
        List<VideoResponse> videoResponses = new ArrayList<>();
        for (Video video : videos) {
            VideoResponse videoResponse = VideoResponse.builder()
                    .url(video.getUrl())
                    .title(video.getTitle())
                    .description(video.getDescription())
                    .id(video.getId())
                    .category(String.valueOf(video.getCategory()))
                    .build();
            videoResponses.add(videoResponse);
        }
        return videoResponses;
    }

    @Override
    public String deleteVideo(Long videoId, String email) {
        Video video = videoRepository.findById(videoId).orElseThrow(() -> new ResourceNotExit("Video not found"));
        videoRepository.delete(video);
        return "Video deleted";
    }

    @Override
    public MultipartInitiateResponse initiateMultipartUpload(UploadInitiateRequest request) {
        String key = S3Helper.createKey(request.getFilename(), request.getCategory());
        String bucket = properties.getBucket();

        CreateMultipartUploadRequest createRequest = CreateMultipartUploadRequest.builder()
                .bucket(bucket)
                .key(key)
                .acl(ObjectCannedACL.PUBLIC_READ)
                .contentType(request.getContentType())
                .build();

        CreateMultipartUploadResponse response = s3Client.createMultipartUpload(createRequest);
        return new MultipartInitiateResponse(
                response.uploadId(),
                key,
                CHUNK_SIZE
        );
    }

    @Override
    public String getPresignedPartUrl(String key, String uploadId, int partNumber) {

        String bucket = properties.getBucket();

        UploadPartRequest uploadPartRequest = UploadPartRequest.builder()
                .bucket(bucket)
                .key(key)
                .uploadId(uploadId)
                .partNumber(partNumber)
                .build();

        UploadPartPresignRequest presignRequest =
                UploadPartPresignRequest.builder()
                        .signatureDuration(Duration.ofHours(1)) // Longer for large parts
                        .uploadPartRequest(uploadPartRequest)
                        .build();

        PresignedUploadPartRequest presignedRequest =
                s3Presigner.presignUploadPart(presignRequest);

        return presignedRequest.url().toString();
    }

    @Override
    public CompleteMultipartUploadResponse  completeMultipartUpload(CompleteMultipartRequest completeMultipartRequest,
                                                                    String email) {

        try {
            String key = completeMultipartRequest.getKey();
            String uploadId = completeMultipartRequest.getUploadId();
            List<CompletedPartDto> parts = completeMultipartRequest.getParts();

            List<CompletedPart> completedParts = parts.stream()
                    .map(p -> CompletedPart.builder()
                            .partNumber(p.getPartNumber())
                            .eTag(p.getETag())
                            .build())
                    .sorted((a, b) -> Integer.compare(a.partNumber(), b.partNumber()))
                    .collect(Collectors.toList());

            CompletedMultipartUpload completedUpload =
                    CompletedMultipartUpload.builder()
                            .parts(completedParts)
                            .build();

            CompleteMultipartUploadRequest completeRequest =
                    CompleteMultipartUploadRequest.builder()
                            .bucket(properties.getBucket())
                            .key(key)
                            .uploadId(uploadId)
                            .multipartUpload(completedUpload)
                            .build();
            System.out.println("Upload successful");

            // Get video information
            String title = completeMultipartRequest.getTitle();
            String description = completeMultipartRequest.getDescription();
            String category = completeMultipartRequest.getCategory();
            String url = S3Helper.finalUrl(key, properties.getBucket());

            Video video = new Video();
            video.setTitle(title);
            video.setDescription(description);
            video.setCategory(Category.valueOf(category.toUpperCase()));
            video.setUrl(url);
            video.setAuthorEmail(email);
            videoRepository.save(video);

            metricConfig.incrementSuccessUploadVideoCounter();

            return s3Client.completeMultipartUpload(completeRequest);
        }
        catch (Exception e) {
            metricConfig.incrementFailedUploadVideoCounter();
            throw new VideoUploadException(e.getMessage());
        }
    }

    @Override
    public String abortMultipartUpload(AbortPartRequest abortPartRequest) {

        AbortMultipartUploadRequest abortMultipartUploadRequest =
                AbortMultipartUploadRequest.builder()
                        .key(abortPartRequest.getKey())
                        .bucket(properties.getBucket())
                        .uploadId(abortPartRequest.getUploadId())
                        .build();

        s3Client.abortMultipartUpload(abortMultipartUploadRequest);

        return "Successfully aborted";
    }

    private String createPresignedGetUrl(String bucket, String key) {
        GetObjectRequest getObjectRequest = GetObjectRequest.builder()
                .bucket(bucket)
                .key(key)
                .responseContentDisposition("attachment") // Property define the method display. if not use, it is preview display
                .build();

        GetObjectPresignRequest getObjectPresignRequest = GetObjectPresignRequest.builder()
                .signatureDuration(Duration.ofMinutes(10))
                .getObjectRequest(getObjectRequest)
                .build();

        PresignedGetObjectRequest presignedGetObjectRequest = s3Presigner.presignGetObject(getObjectPresignRequest);
        return presignedGetObjectRequest.url().toExternalForm();
    }

    // Cron job clear orphaned upload
    // Run cleanup daily at 2 AM
    @Scheduled(cron = "0 0 2 * * ?")
    public void cleanupAbandonedUploads() {
        log.info("Starting cleanup of abandoned multipart uploads");

        ListMultipartUploadsRequest listRequest = ListMultipartUploadsRequest.builder()
                .bucket(properties.getBucket())
                .build();

        ListMultipartUploadsResponse response = s3Client.listMultipartUploads(listRequest);
        List<MultipartUpload> uploads = response.uploads();

        Instant oneDayAgo = Instant.now().minus(1, ChronoUnit.DAYS);

        int abortedCount = 0;
        for (MultipartUpload upload : uploads) {
            if (upload.initiated().isBefore(oneDayAgo)) {
                try {
                    AbortMultipartUploadRequest abortRequest =
                            AbortMultipartUploadRequest.builder()
                                    .bucket(properties.getBucket())
                                    .key(upload.key())
                                    .uploadId(upload.uploadId())
                                    .build();

                    s3Client.abortMultipartUpload(abortRequest);
                    abortedCount++;
                    log.info("Aborted abandoned upload: {} (initiated: {})",
                            upload.key(), upload.initiated());
                } catch (Exception e) {
                    log.error("Failed to abort upload: {}", upload.key(), e);
                }
            }
        }

        log.info("Cleanup completed. Aborted {} uploads", abortedCount);
    }

    public String trigger() {
        cleanupAbandonedUploads();
        return "Trigger successful";
    }

    @Override
    public String verifyAndSaveToDatabase(VerifyUploadPresign verifyUploadPresign, String name) {
        String key = verifyUploadPresign.getKey();

        try {
            // Verify exist file in s3
            HeadObjectRequest headObjectRequest = HeadObjectRequest.builder()
                    .bucket(properties.getBucket())
                    .key(key)
                    .build();

            s3Client.headObject(headObjectRequest);

            // Store to db
            Video video = new Video();
            video.setTitle(verifyUploadPresign.getTitle());
            video.setUrl(S3Helper.finalUrl(key, properties.getBucket()));
            video.setDescription(verifyUploadPresign.getDescription());
            video.setCategory(Category.valueOf(verifyUploadPresign.getCategory().toUpperCase()));
            video.setAuthorEmail(verifyUploadPresign.getAuthor());
            videoRepository.save(video);

            // Increase metric success upload
            metricConfig.incrementSuccessUploadVideoCounter();

            return "Successfully";
        }
        catch (Exception e) {
            // Increase metric fail upload
            metricConfig.incrementFailedUploadVideoCounter();
            throw new RuntimeException("Verify exist file failed");
        }
    }
}
