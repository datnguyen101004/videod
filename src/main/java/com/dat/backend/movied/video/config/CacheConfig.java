package com.dat.backend.movied.video.config;

import tools.jackson.databind.SerializationFeature;
import tools.jackson.databind.cfg.DateTimeFeature;
import tools.jackson.databind.jsontype.BasicPolymorphicTypeValidator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJacksonJsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.DefaultTyping;
import tools.jackson.databind.json.JsonMapper;
import tools.jackson.datatype.jsr310.JavaTimeModule;

@Configuration
public class CacheConfig {

    @Bean
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory redisConnectionFactory) {
        RedisTemplate<String, Object> redisTemplate = new RedisTemplate<>();

        redisTemplate.setConnectionFactory(redisConnectionFactory);

        /**
         * Hash sẽ giúp lưu giá trị dưới dạng 1 map
         */
        redisTemplate.setKeySerializer(StringRedisSerializer.UTF_8);
        redisTemplate.setHashKeySerializer(StringRedisSerializer.UTF_8);

        BasicPolymorphicTypeValidator basicPolymorphicTypeValidator = BasicPolymorphicTypeValidator.builder()
                .allowIfBaseType(Object.class)
                .build();

        ObjectMapper objectMapper = JsonMapper.builder()
                .addModule(new JavaTimeModule())
                .configure(DateTimeFeature.WRITE_DATES_AS_TIMESTAMPS, false)
                .enable(SerializationFeature.INDENT_OUTPUT)
                .activateDefaultTyping(basicPolymorphicTypeValidator, DefaultTyping.NON_FINAL)
                .build();

        GenericJacksonJsonRedisSerializer genericJacksonJsonRedisSerializer = new GenericJacksonJsonRedisSerializer(objectMapper);

        redisTemplate.setValueSerializer(genericJacksonJsonRedisSerializer);
        redisTemplate.setHashValueSerializer(genericJacksonJsonRedisSerializer);

        return redisTemplate;
    }
}
