package com.dat.backend.movied.ratelimit;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.Refill;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
//import org.springframework.data.redis.connection.RedisConnectionFactory;
//import org.springframework.data.redis.core.RedisTemplate;
//import org.springframework.data.redis.serializer.GenericToStringSerializer;

import java.time.Duration;

@Configuration
public class RateLimitConfig {

//    @Bean
//    public RedisTemplate<String, Long> redisTemplate(RedisConnectionFactory factory) {
//        RedisTemplate<String, Long> template = new RedisTemplate<>();
//        template.setConnectionFactory(factory);
//        template.setDefaultSerializer(new GenericToStringSerializer<>(Long.class));
//        return template;
//    }

    /**
     * Creates a bucket with:
     * - Capacity: 100 tokens
     * - Refill: 1 tokens per seconds ( greedy refill)
     */
    @Bean
    public Bucket createDefaultBucket() {
        Bandwidth limit = Bandwidth.classic(
                100, // capacity
                Refill.greedy(60, Duration.ofMinutes(1))
        );
        return Bucket.builder().addLimit(limit).build();
    }
}
