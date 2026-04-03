package com.dat.backend.movied.ratelimit.common;

import lombok.Getter;

import java.time.Duration;

@Getter
public enum RateLimitPlan {

    FREE(5, 1, Duration.ofMillis(10)),
    PREMIUM(20, 4, Duration.ofMillis(10)),
    MAX_PREMIUM(1000, 10, Duration.ofMillis(10));

    private final Long capacity;
    private final Long refillTokens;
    private final Duration duration;

    RateLimitPlan(int capacity, long refillTokens, Duration duration) {
        this.capacity = (long) capacity;
        this.refillTokens = refillTokens;
        this.duration = duration;
    }

    public static RateLimitPlan getPlanForUser(String plan) {
        if (plan.equalsIgnoreCase("free")) {
            return FREE;
        }
        else if (plan.equalsIgnoreCase("premium")) {
            return PREMIUM;
        }

        return MAX_PREMIUM;
    }
}
