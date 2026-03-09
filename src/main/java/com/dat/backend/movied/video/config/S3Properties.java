package com.dat.backend.movied.video.config;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "do.space")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class S3Properties {
    private String bucket;
    private String region;
    private String url;
    private String accessKey;
    private String secretKey;
}
