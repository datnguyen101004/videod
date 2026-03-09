package com.dat.backend.movied.video.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.http.nio.netty.NettyNioAsyncHttpClient;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3AsyncClient;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.transfer.s3.S3TransferManager;

import java.net.URI;
import java.time.Duration;

@Configuration
public class S3Config {
    private final S3Properties properties;

    public S3Config(S3Properties properties) {
        this.properties = properties;
    }

    @Bean
    public S3Client s3ClientSync() {
        return S3Client.builder()
                .region(Region.of(properties.getRegion()))
                .endpointOverride(URI.create("https://" + properties.getUrl()))
                .credentialsProvider(
                        StaticCredentialsProvider.create(
                                AwsBasicCredentials.create(properties.getAccessKey(), properties.getSecretKey())
                        )
                )
                .build();
    }

    @Bean
    public S3AsyncClient s3AsyncClientSync() {
        NettyNioAsyncHttpClient httpClient =
                (NettyNioAsyncHttpClient) NettyNioAsyncHttpClient.builder()
                        .connectionTimeout(Duration.ofSeconds(10))
                        .writeTimeout(Duration.ofSeconds(60))
                        .readTimeout(Duration.ofSeconds(60))
                        .maxConcurrency(50)
                        .build();
        return S3AsyncClient.builder()
                .multipartEnabled(true)
                .region(Region.of(properties.getRegion()))
                .endpointOverride(URI.create("https://" + properties.getUrl()))
                .credentialsProvider(
                        StaticCredentialsProvider.create(
                                AwsBasicCredentials.create(properties.getAccessKey(), properties.getSecretKey())
                        )
                )
                .httpClient(httpClient)
                .build();
    }

    @Bean
    public S3TransferManager s3TransferManagerAsync(S3AsyncClient s3AsyncClient) {
        return S3TransferManager.builder()
                .s3Client(s3AsyncClient)
                .build();
    }
}
