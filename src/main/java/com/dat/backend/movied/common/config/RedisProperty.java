package com.dat.backend.movied.common.config;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("redis")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RedisProperty {
    private String url;
}
