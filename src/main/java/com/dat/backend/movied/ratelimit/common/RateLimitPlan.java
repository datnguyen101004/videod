package com.dat.backend.movied.ratelimit.common;

import java.time.Duration;

public enum RateLimitPlan {

    FREE(5, 1, Duration.ofMinutes(1)),
    PREMIUM(10, 2, Duration.ofMinutes(1));

    private final Long capacity;
    private final Long refillTokens;
    private final Duration duration;

    RateLimitPlan(int capacity, long refillTokens, Duration duration) {
        this.capacity = Long.valueOf(capacity);
        this.refillTokens = Long.valueOf(refillTokens);
        this.duration = duration;
    }

    public Long getCapacity() {
        return capacity;
    }

    public Long getRefillTokens() {
        return refillTokens;
    }

    public Duration getDuration() {
        return duration;
    }

    public static RateLimitPlan getPlanForUser(String plan) {
        if (plan.equalsIgnoreCase("free")) {
            return FREE;
        }

        return PREMIUM;
    }
}
