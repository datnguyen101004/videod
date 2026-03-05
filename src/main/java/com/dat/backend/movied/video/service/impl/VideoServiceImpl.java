package com.dat.backend.movied.video.service.impl;

import com.dat.backend.movied.video.config.S3Properties;
import com.dat.backend.movied.video.dto.CreateVideoDto;
import com.dat.backend.movied.video.dto.VideoResponse;
import com.dat.backend.movied.video.entity.Video;
import com.dat.backend.movied.video.repository.VideoRepository;
import com.dat.backend.movied.video.service.VideoService;
import com.dat.backend.movied.video.util.S3Helper;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.async.AsyncRequestBody;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3AsyncClient;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;
import software.amazon.awssdk.transfer.s3.S3TransferManager;
import software.amazon.awssdk.transfer.s3.model.CompletedFileUpload;
import software.amazon.awssdk.transfer.s3.model.FileUpload;
import software.amazon.awssdk.transfer.s3.model.UploadFileRequest;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.nio.file.Paths;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Service
public class VideoServiceImpl implements VideoService {
    private final VideoRepository videoRepository;
    private final S3Client s3Client;
    private final S3Properties properties;

    // Note: enable multipart
    private final S3AsyncClient s3AsyncClient;

    public VideoServiceImpl(VideoRepository videoRepository,
                            S3Client s3Client,
                            S3AsyncClient s3AsyncClient,
                            S3Properties properties) {
        this.videoRepository = videoRepository;
        this.s3Client = s3Client;
        this.s3AsyncClient = s3AsyncClient;
        this.properties = properties;
    }

    // Upload video do DO spaces with using putobject
    @Override
    public VideoResponse uploadVideo(MultipartFile file, CreateVideoDto createVideoDto) {
        try {
            String key = S3Helper.createKey(file.getOriginalFilename());

            // Put video to DO via S3 api
            PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                    .bucket(properties.getBucket())
                    .key(key)
                    .contentType(file.getContentType())
                    .acl(ObjectCannedACL.PUBLIC_READ)
                    .build();

            s3Client.putObject(
                    putObjectRequest,
                    RequestBody.fromBytes(file.getBytes())
            );

            // final url video
            String url = S3Helper.createUrl(properties.getBucket(), key);

            //TODO: implement mapper for easy maintaining
            Video video = new Video();
            video.setUrl(url);
            video.setTitle(createVideoDto.getTitle());
            video.setDescription(createVideoDto.getDescription());
            videoRepository.save(video);

            return VideoResponse.builder()
                    .url(url)
                    .title(video.getTitle())
                    .description(video.getDescription())
                    .build();

        } catch (Exception e) {
            throw new RuntimeException("Upload failed", e);
        }
    }

    // Upload video to DO space with sync client API and multipart API
    public String uploadFile(S3TransferManager transferManager, String bucketName,
                             String key, URI filePathURI) {
        UploadFileRequest uploadFileRequest = UploadFileRequest.builder()
                .putObjectRequest(b -> b.bucket(bucketName).key(key))
                .source(Paths.get(filePathURI))
                .build();

        FileUpload fileUpload =  transferManager.uploadFile(uploadFileRequest);

        CompletedFileUpload uploadResult = fileUpload.completionFuture().join();
        return uploadResult.response().eTag();
    }

    public PutObjectResponse asyncClientMultipartUpload(
            MultipartFile file) throws IOException {

        String key = S3Helper.createKey(file.getOriginalFilename());

        ExecutorService executor = Executors.newSingleThreadExecutor();

        InputStream inputStream = file.getInputStream();

        AsyncRequestBody body =
                AsyncRequestBody.fromInputStream(inputStream, null, executor);

        CompletableFuture<PutObjectResponse> responseFuture =
                s3AsyncClient.putObject(
                        r -> r.bucket(properties.getBucket()).acl(ObjectCannedACL.PUBLIC_READ).key(key),
                        body
                ).exceptionally(e -> {
                    System.out.println(e.getMessage());
                    return null;
                });

        PutObjectResponse response = responseFuture.join();

        executor.shutdown();

        return response;
    }

    public String downloadVideo(MultipartFile file) {
        return "";
    }
}
