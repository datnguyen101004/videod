package com.dat.backend.movied.ratelimit.service;

import com.dat.backend.movied.ratelimit.common.RateLimitPlan;
import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.BucketConfiguration;
import io.github.bucket4j.Refill;
import io.github.bucket4j.distributed.proxy.ProxyManager;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
@Slf4j
public class RedisRateLimitService implements RateLimitService {

    private final ProxyManager<String> proxyManager;
    private final Map<String, Bucket> cache = new ConcurrentHashMap<>();


    public RedisRateLimitService(ProxyManager<String> proxyManager) {
        this.proxyManager = proxyManager;
    }

    private Bucket resolveBucket(String key, RateLimitPlan plan) {
        //log.info("Redis");
        String cacheKey = key + ":" + plan;
        //log.info("Cache key: {}", cacheKey);
        return cache.computeIfAbsent(cacheKey, k -> {
            BucketConfiguration config = BucketConfiguration.builder()
                    .addLimit(Bandwidth.classic(
                            plan.getCapacity(),
                            Refill.greedy(plan.getRefillTokens(), plan.getDuration())
                    ))
                    .build();

            return proxyManager.builder().build(k, () -> config);
        });
    }

    @Override
    public boolean tryConsume(String key, RateLimitPlan plan, int tokens) {
        return resolveBucket(key, plan).tryConsume(tokens);
    }

    @Override
    public long getAvailableTokens(String key, RateLimitPlan plan) {
        return resolveBucket(key, plan).getAvailableTokens();
    }
}
