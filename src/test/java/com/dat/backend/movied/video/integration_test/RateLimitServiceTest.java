package com.dat.backend.movied.video.integration_test;

import com.dat.backend.movied.ratelimit.common.RateLimitPlan;
import com.dat.backend.movied.ratelimit.service.RedisRateLimitService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


@Testcontainers
@SpringBootTest
public class RateLimitServiceTest {

    @Container
    static GenericContainer<?> redis =
            new GenericContainer<>("redis:7-alpine")
                    .withExposedPorts(6379);

    @Autowired
    private RedisRateLimitService redisRateLimitService;

    @DynamicPropertySource
    static void redisProperties(DynamicPropertyRegistry registry) {
        registry.add("REDIS_URL", () -> "redis://" + redis.getHost() + ":" + redis.getFirstMappedPort());
    }

    @Test
    public void testHappyCase() {
        // Set up param
        String key = "happy_case";
        RateLimitPlan rateLimitPlan = RateLimitPlan.FREE;

        // Call test function
        Boolean res = redisRateLimitService.tryConsume(key, rateLimitPlan, 1);

        // Assertion
        Assertions.assertEquals(
                redisRateLimitService.getAvailableTokens(key, rateLimitPlan),
                rateLimitPlan.getCapacity() -1
        );
    }

    @Test
    public void testLimited() {
        // Set up param
        String key = "limited";
        RateLimitPlan rateLimitPlan = RateLimitPlan.FREE;

        for (int i = 0; i < rateLimitPlan.getCapacity(); i++) {
            redisRateLimitService.tryConsume(key, rateLimitPlan, 1);
        }

        // This will cause rate limit
        Boolean res = redisRateLimitService.tryConsume(key, rateLimitPlan, 1);

        // Assertion
        Assertions.assertFalse(res);
    }

    @Test
    public void testHandleConcurrencyRequests() throws InterruptedException {
        // Set up param
        String key = "concurrency";
        RateLimitPlan rateLimitPlan = RateLimitPlan.FREE;

        CountDownLatch start = new CountDownLatch(1);
        CountDownLatch ready = new CountDownLatch(10);

        // Assume 10 concurrency request
        ExecutorService executorService = Executors.newFixedThreadPool(10);

        List<CompletableFuture<Boolean>> futures = new ArrayList<>();

        for (int i = 0 ; i < 10 ; i++) {
            futures.add(
                    CompletableFuture.supplyAsync(
                            () -> {
                                try {
                                    ready.countDown(); // decrease countdownlack ready by 1
                                    start.await(); // if countdownlack start = 0, allow all thread start
                                } catch (InterruptedException e) {
                                    throw new RuntimeException(e);
                                }
                                return redisRateLimitService.tryConsume(key, rateLimitPlan, 1);
                            }
                            , executorService
                    )
            );
        }

        ready.await(); // if loop done, ready is start
        start.countDown(); // countdownlack start is 0

        List<Boolean> results = futures.stream()
                .map(CompletableFuture::join)
                .toList();

        Long successCount = results.stream()
                .filter(Boolean::booleanValue)
                .count();

        // Assertion
        Assertions.assertEquals(
                rateLimitPlan.getCapacity(),
                successCount
        );
    }
}
