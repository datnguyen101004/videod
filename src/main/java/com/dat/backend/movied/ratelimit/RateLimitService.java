package com.dat.backend.movied.ratelimit;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.Refill;
import org.springframework.stereotype.Service;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class RateLimitService {

    private final Map<String, Bucket> cache = new ConcurrentHashMap<>();

    public Bucket resolveBucket(String email, RateLimitPlan rateLimitPlan) {
        return cache.computeIfAbsent(
                email, k -> createBucket(rateLimitPlan)
        );
    }

    private Bucket createBucket(RateLimitPlan rateLimitPlan) {
        Bandwidth limit = Bandwidth.classic(
                rateLimitPlan.getCapacity(),
                Refill.greedy(rateLimitPlan.getRefillTokens(), rateLimitPlan.getDuration())
        );

        return Bucket.builder().addLimit(limit).build();
    }

    public boolean tryConsume(String email, RateLimitPlan rateLimitPlan, int tokens) {
        Bucket bucket = resolveBucket(email, rateLimitPlan);

        return bucket.tryConsume(tokens);
    }

    public Long getAvailableTokens(String email, RateLimitPlan rateLimitPlan) {
        Bucket bucket = resolveBucket(email, rateLimitPlan);

        return bucket.getAvailableTokens();
    }

}
