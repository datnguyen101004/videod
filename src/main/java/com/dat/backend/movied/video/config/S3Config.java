package com.dat.backend.movied.video.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3AsyncClient;
import software.amazon.awssdk.services.s3.S3Client;

import java.net.URI;

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
        return S3AsyncClient.builder()
                .multipartEnabled(true)
                .region(Region.of(properties.getRegion()))
                .endpointOverride(URI.create("https://" + properties.getUrl()))
                .credentialsProvider(
                        StaticCredentialsProvider.create(
                                AwsBasicCredentials.create(properties.getAccessKey(), properties.getSecretKey())
                        )
                )
                .build();
    }
}
