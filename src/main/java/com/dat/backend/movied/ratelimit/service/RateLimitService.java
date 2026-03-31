package com.dat.backend.movied.ratelimit.service;


import com.dat.backend.movied.ratelimit.common.RateLimitPlan;

public interface RateLimitService {
    boolean tryConsume(String key, RateLimitPlan plan, int tokens);

    long getAvailableTokens(String key, RateLimitPlan plan);
}
