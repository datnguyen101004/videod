package com.dat.backend.movied.video.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.AwsCredentialsProvider;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.http.apache.ApacheHttpClient;
import software.amazon.awssdk.http.nio.netty.NettyNioAsyncHttpClient;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3AsyncClient;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
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
    public AwsCredentialsProvider awsCredentialsProvider() {
        return StaticCredentialsProvider.create(
                AwsBasicCredentials.create(
                        properties.getAccessKey(),
                        properties.getSecretKey()
                )
        );
    }

    @Bean
    public Region awsRegion() {
        return Region.of(properties.getRegion());
    }

    @Bean
    public URI s3Endpoint() {
        return URI.create("https://" + properties.getUrl());
    }

    @Bean
    public S3Client s3Client(
            AwsCredentialsProvider credentialsProvider,
            Region region,
            URI endpoint) {
        ApacheHttpClient httpClient = (ApacheHttpClient) ApacheHttpClient.builder()
                .connectionTimeout(Duration.ofSeconds(5))
                .socketTimeout(Duration.ofSeconds(5))
                .maxConnections(50) // Allow multiple concurrent connections
                .connectionTimeToLive(Duration.ofMinutes(1))
                .useIdleConnectionReaper(true)
                .build();

        return S3Client.builder()
                .region(region)
                .endpointOverride(endpoint)
                .accelerate(true)
                .credentialsProvider(credentialsProvider)
                .httpClient(httpClient)
                .build();
    }

    @Bean
    public S3AsyncClient s3AsyncClient(
            AwsCredentialsProvider credentialsProvider,
            Region region,
            URI endpoint) {

        NettyNioAsyncHttpClient httpClient =
                (NettyNioAsyncHttpClient) NettyNioAsyncHttpClient.builder()
                        .connectionTimeout(Duration.ofSeconds(10))
                        .writeTimeout(Duration.ofSeconds(60))
                        .readTimeout(Duration.ofSeconds(60))
                        .maxConcurrency(50)
                        .connectionTimeToLive(Duration.ofMinutes(1))
                        .build();

        return S3AsyncClient.builder()
                .multipartEnabled(true)
                .region(region)
                .endpointOverride(endpoint)
                .credentialsProvider(credentialsProvider)
                .httpClient(httpClient)
                .build();
    }


    @Bean
    public S3TransferManager s3TransferManagerAsync(S3AsyncClient s3AsyncClient) {
        return S3TransferManager.builder()
                .s3Client(s3AsyncClient)
                .build();
    }

    @Bean
    public S3Presigner s3Presigner(
            AwsCredentialsProvider credentialsProvider,
            Region region,
            URI endpoint) {

        return S3Presigner.builder()
                .region(region)
                .endpointOverride(endpoint)
                .credentialsProvider(credentialsProvider)
                .build();
    }
}
