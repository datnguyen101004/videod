package com.dat.backend.movied.video.service.impl;

import com.dat.backend.movied.video.dto.CreateVideoDto;
import com.dat.backend.movied.video.dto.VideoResponse;
import com.dat.backend.movied.video.entity.Video;
import com.dat.backend.movied.video.repository.VideoRepository;
import com.dat.backend.movied.video.service.VideoService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.ObjectCannedACL;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.io.IOException;
import java.util.UUID;

@Service
public class VideoServiceImpl implements VideoService {
    @Value("${do.space.name}")
    private String bucketName;
    private final VideoRepository videoRepository;
    private final S3Client s3Client;

    public VideoServiceImpl(VideoRepository videoRepository, S3Client s3Client) {
        this.videoRepository = videoRepository;
        this.s3Client = s3Client;
    }

    /**
     * @param file
     * Upload video by separate to multi part
     */
    @Override
    public VideoResponse uploadVideo(MultipartFile file, CreateVideoDto createVideoDto) {
        try {
            // TODO: util method to help create key automatic
            String fileName = UUID.randomUUID() + "_" + file.getOriginalFilename();
            String key = "videos/" + fileName;

            PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                    .bucket(bucketName)
                    .key(key)
                    .contentType(file.getContentType())
                    .acl(ObjectCannedACL.PUBLIC_READ)
                    .build();

            s3Client.putObject(
                    putObjectRequest,
                    RequestBody.fromBytes(file.getBytes())
            );

            // TODO: implement multipart upload

            // TODO: not hardcore code
            String url = "https://" + bucketName + ".sgp1.cdn.digitaloceanspaces.com/" + key;

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

    public String downloadVideo(MultipartFile file) {
        return "";
    }
}
