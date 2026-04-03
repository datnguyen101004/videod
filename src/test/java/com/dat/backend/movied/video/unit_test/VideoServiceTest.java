package com.dat.backend.movied.video.unit_test;

import com.dat.backend.movied.common.config.MetricConfig;
import com.dat.backend.movied.video.config.S3Properties;
import com.dat.backend.movied.video.dto.request.PresignUploadRequest;
import com.dat.backend.movied.video.dto.response.PresignedUrlResponse;
import com.dat.backend.movied.video.dto.request.VerifyUploadPresign;
import com.dat.backend.movied.video.entity.Video;
import com.dat.backend.movied.video.repository.VideoRepository;
import com.dat.backend.movied.video.service.impl.VideoServiceImpl;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.context.ActiveProfiles;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.HeadObjectRequest;
import software.amazon.awssdk.services.s3.model.HeadObjectResponse;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.PresignedPutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.model.PutObjectPresignRequest;

import java.net.MalformedURLException;
import java.net.URL;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@ActiveProfiles("test")
public class VideoServiceTest {

    /**
     * Dùng @Mock để mock các dependency không muốn gọi (fake)
     * Dùng @InjectMocks để khởi tạo các fake dependency vào object thật
     */
    @Mock
    private VideoRepository videoRepository;

    @Mock
    private S3Client s3Client;

    @Mock
    private S3Presigner s3Presigner;

    @Mock
    private S3Properties properties;

    @Mock
    private MetricConfig metricConfig;

    @InjectMocks
    private VideoServiceImpl videoService;

    private VerifyUploadPresign verifyUploadPresign;

    @BeforeEach
    public void setUp() {
         this.verifyUploadPresign = VerifyUploadPresign.builder()
                .key("test-key")
                .title("test-title")
                .description("test-description")
                .author("test-author")
                .category("EDUCATION")
                .build();

        // Mock bucket
        when(properties.getBucket()).thenReturn("test-bucket");
    }

    @Test
    public void testCreatePresignUrlSmallVideo() throws MalformedURLException {
        // Prepare input
        PresignUploadRequest presignUploadRequest = new PresignUploadRequest();

        presignUploadRequest.setFilename("test.mp4");
        presignUploadRequest.setFileSize(1024L);
        presignUploadRequest.setContentType("video/mp4");
        presignUploadRequest.setCategory("EDUCATION");

        // Mock PresingedPutObjectRequest
        PresignedPutObjectRequest presignedPutObjectRequestMock = mock(PresignedPutObjectRequest.class);

        // If mock .toString() cause null pointer exception
        // because presignedPutObjectRequestMock.url() is not assign value
        when(presignedPutObjectRequestMock.url()).thenReturn(new URL("https://presign-test-upload-video"));

        when(s3Presigner.presignPutObject(any(PutObjectPresignRequest.class)))
                .thenReturn(presignedPutObjectRequestMock);

        // Call service
        PresignedUrlResponse response = videoService.createPresignUrlSmallVideo(presignUploadRequest, "test@gmail.com");

        // Verify
        Assertions.assertNotNull(response);

        Assertions.assertEquals("https://presign-test-upload-video", response.getUrl());

        verify(s3Presigner, times(1)).presignPutObject(any(PutObjectPresignRequest.class));
    }

    @Test
    public void testVerifyAndSaveToDatabase_success() {

        String name = "test-name";

        // Happy case: mock headObject action success
        when(s3Client.headObject(any(HeadObjectRequest.class)))
                .thenReturn(HeadObjectResponse.builder().build());

        // Call method
        String result = videoService.verifyAndSaveToDatabase(verifyUploadPresign, name);

        // Check result
        Assertions.assertEquals("Successfully", result);

        // Check database work
        verify(videoRepository, times(1)).save(any(Video.class));

        // Check metrics work
        verify(metricConfig, times(1)).incrementSuccessUploadVideoCounter();
    }

    @Test
    public void testVerifyAndSaveToDatabase_fail() {

        String name = "test-name";

        // Bad case: mock headObject action fail
        when(s3Client.headObject(any(HeadObjectRequest.class)))
                .thenThrow(new RuntimeException("File not exist"));

        // Check result
        RuntimeException ex = Assertions.assertThrows(RuntimeException.class,
                () -> videoService.verifyAndSaveToDatabase(verifyUploadPresign, name)
        );

        // Check result
        Assertions.assertEquals("Verify exist file failed", ex.getMessage());

        // Check database work
        verify(videoRepository, times(0)).save(any(Video.class));

        // Check metric work
        verify(metricConfig, times(1)).incrementFailedUploadVideoCounter();
    }
}
