package com.dat.backend.movied.video.service.impl;

import com.dat.backend.movied.common.config.CustomThread;
import com.dat.backend.movied.video.config.S3Properties;
import com.dat.backend.movied.video.dto.CreateVideoDto;
import com.dat.backend.movied.video.dto.VideoDownloadRequest;
import com.dat.backend.movied.video.dto.VideoResponse;
import com.dat.backend.movied.video.entity.Category;
import com.dat.backend.movied.video.entity.Video;
import com.dat.backend.movied.video.exception.ResourceNotExit;
import com.dat.backend.movied.video.exception.VideoUploadException;
import com.dat.backend.movied.video.repository.VideoRepository;
import com.dat.backend.movied.video.service.VideoService;
import com.dat.backend.movied.video.util.S3Helper;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.async.AsyncRequestBody;
import software.amazon.awssdk.services.s3.S3AsyncClient;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;
import software.amazon.awssdk.services.s3.presigner.model.PresignedGetObjectRequest;
import software.amazon.awssdk.transfer.s3.S3TransferManager;
import software.amazon.awssdk.transfer.s3.model.*;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;

@Service
public class VideoServiceImpl implements VideoService {
    private final VideoRepository videoRepository;
    private final S3Properties properties;
    private static final int PART_SIZE = 5 * 1024 * 1024; // 5MB
    private final CustomThread customThread;
    private static final long FILE_THRESHOLD = 200 * 1024 * 1024; // 200MB
    private final S3TransferManager s3TransferManager;
    private final ExecutorService executor;
    private final S3Presigner s3Presigner;

    // Note: enable multipart
    private final S3AsyncClient s3AsyncClient;

    public VideoServiceImpl(VideoRepository videoRepository,
                            S3Client s3Client,
                            S3AsyncClient s3AsyncClient,
                            S3Properties properties,
                            CustomThread customThread,
                            S3TransferManager s3TransferManager,
                            ExecutorService executor,
                            S3Presigner s3Presigner) {
        this.videoRepository = videoRepository;
        this.s3AsyncClient = s3AsyncClient;
        this.properties = properties;
        this.customThread = customThread;
        this.s3TransferManager = s3TransferManager;
        this.executor = executor;
        this.s3Presigner = s3Presigner;
    }

    // Upload video to DO space
    @Override
    public VideoResponse uploadVideo(MultipartFile file, CreateVideoDto createVideoDto, String email) {
        try {
            // Get the enough properties
            String filename = file.getOriginalFilename();
            long fileSize = file.getSize();
            String category = createVideoDto.getCategory();

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

            // Check file size for choosing reasonable method uploading
            String url;
            if (fileSize < FILE_THRESHOLD) {
                // Choosing async upload method
                url = asyncUpload(file, key, bucket);
            }
            else {
                // Choosing async multipart upload method
                url = asyncMultipartUpload(file, key, bucket);
            }

            // Store to db
            Video video = new Video();
            video.setUrl(url);
            video.setTitle(createVideoDto.getTitle());
            video.setCategory(Category.valueOf(createVideoDto.getCategory().toUpperCase()));
            video.setDescription(createVideoDto.getDescription());
            video.setKeyStorage(key);
            video.setAuthorEmail(email);
            videoRepository.save(video);

            return VideoResponse.builder()
                    .url(url)
                    .title(video.getTitle())
                    .description(video.getDescription())
                    .id(video.getId())
                    .category(String.valueOf(video.getCategory()))
                    .build();

        }
        catch (Exception e) {
            throw new VideoUploadException(e.getMessage());
        }
    }

    // Upload video to DO space with sync client API and multipart API
    public String asyncMultipartUpload(MultipartFile file, String key, String bucket) throws IOException {
        String ext = Objects.requireNonNull(file.getOriginalFilename())
                .substring(file.getOriginalFilename().lastIndexOf("."));
        Path tempFile = Files.createTempFile("upload", ext);
        try {
            file.transferTo(tempFile);

            FileUpload upload = s3TransferManager.uploadFile(
                    UploadFileRequest.builder()
                            .putObjectRequest(p -> p
                                    .bucket(bucket)
                                    .key(key)
                                    .acl(ObjectCannedACL.PUBLIC_READ)
                                    .contentType(file.getContentType()))
                            .source(tempFile)
                            .build()
            );

            upload.completionFuture().join();

            return S3Helper.finalUrl(key, bucket);
        }
        finally {
            Files.deleteIfExists(tempFile);
        }
    }

    public String asyncUpload(MultipartFile file, String key, String bucket) {

        try {

            InputStream inputStream = file.getInputStream();

            AsyncRequestBody body =
                    AsyncRequestBody.fromInputStream(inputStream, file.getSize(), executor);

            CompletableFuture<PutObjectResponse> responseFuture =
                    s3AsyncClient.putObject(
                            r -> r.bucket(properties.getBucket())
                                    .contentType(file.getContentType())
                                    .acl(ObjectCannedACL.PUBLIC_READ)
                                    .key(key),
                            body
                    ).exceptionally(e -> {
                        throw new VideoUploadException(e.getMessage());
                    });

            PutObjectResponse response = responseFuture.join();

            return S3Helper.finalUrl(key, bucket);
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
}
