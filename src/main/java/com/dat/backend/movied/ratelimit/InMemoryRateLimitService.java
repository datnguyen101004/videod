package com.dat.backend.movied.ratelimit;

import org.springframework.stereotype.Service;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.Refill;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class InMemoryRateLimitService implements RateLimitService {
    private final Map<String, Bucket> cache = new ConcurrentHashMap<>();

    private Bucket createBucket(RateLimitPlan plan) {
        Bandwidth limit = Bandwidth.classic(
                plan.getCapacity(),
                Refill.greedy(plan.getRefillTokens(), plan.getDuration())
        );
        return Bucket.builder().addLimit(limit).build();
    }

    private Bucket resolveBucket(String key, RateLimitPlan plan) {
        return cache.computeIfAbsent(key, k -> createBucket(plan));
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
