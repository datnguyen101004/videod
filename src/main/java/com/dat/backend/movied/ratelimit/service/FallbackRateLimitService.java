package com.dat.backend.movied.ratelimit.service;

import com.dat.backend.movied.ratelimit.common.RateLimitPlan;
import io.lettuce.core.RedisClient;
import io.lettuce.core.api.StatefulRedisConnection;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class FallbackRateLimitService implements RateLimitService {
    private final RedisRateLimitService redisRateLimitService;
    private final InMemoryRateLimitService inMemoryRateLimitService;
    private final RedisClient redisClient;


    // This flag trigger for action when redis down
    // if the value is true, using inMemory default instead redis
    private boolean redisActive = true;

    public FallbackRateLimitService(RedisRateLimitService redisRateLimitService,
                                    InMemoryRateLimitService inMemoryRateLimitService,
                                    RedisClient redisClient) {
        this.redisRateLimitService = redisRateLimitService;
        this.inMemoryRateLimitService = inMemoryRateLimitService;
        this.redisClient = redisClient;

    }

    @Override
    public boolean tryConsume(String key, RateLimitPlan plan, int tokens) {
        if (!redisActive) {
            return inMemoryRateLimitService.tryConsume(key, plan, tokens);
        }

        try {
            return redisRateLimitService.tryConsume(key, plan, tokens);
        } catch (Exception e) {
            redisActive = false;
            log.warn("Redis down, fallback to in-memory", e);
            return inMemoryRateLimitService.tryConsume(key, plan, tokens);
        }
    }

    @Override
    public long getAvailableTokens(String key, RateLimitPlan plan) {
        try {
            return redisRateLimitService.getAvailableTokens(key, plan);
        } catch (Exception e) {
            return inMemoryRateLimitService.getAvailableTokens(key, plan);
        }
    }

    @Scheduled(cron = "0 */2 * * * ?")
    public void checkHealthRedis() {
        try(StatefulRedisConnection<String, String> connection = redisClient.connect()) {
            String ping = connection.sync().ping();
            redisActive = "PONG".equalsIgnoreCase(ping);
            log.info("Redis state: {}", redisActive);
        }
        catch (Exception e) {
            redisActive = false;
            log.warn("Redis down");
        }
    }
}
