package com.dat.backend.movied.common.config;

import io.lettuce.core.RedisClient;
import io.lettuce.core.RedisURI;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;

@Configuration
public class RedisConfig {
    private final RedisProperty redisProperty;

    public RedisConfig(RedisProperty redisProperty) {
        this.redisProperty = redisProperty;
    }

    /**
     * Redis client config
     */
    @Bean(destroyMethod = "shutdown")
    public RedisClient redisClient() {
        return RedisClient.create(redisProperty.getUrl());
    }

    @Bean
    public RedisStandaloneConfiguration redisStandaloneConfiguration() {
        RedisURI redisURI = RedisURI.create(redisProperty.getUrl());

        RedisStandaloneConfiguration redisStandaloneConfiguration = new RedisStandaloneConfiguration();

        redisStandaloneConfiguration.setHostName(redisURI.getHost());
        redisStandaloneConfiguration.setPort(redisURI.getPort());

        return redisStandaloneConfiguration;
    }

    /**
     * Lettuce config
     */
    @Bean
    public RedisConnectionFactory redisConnectionFactory(RedisStandaloneConfiguration redisStandaloneConfiguration) {
        return new LettuceConnectionFactory(redisStandaloneConfiguration);
    }
}
