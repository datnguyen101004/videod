package com.dat.backend.movied.ratelimit;

import com.dat.backend.movied.user.entity.User;
import com.dat.backend.movied.user.repository.UserRepository;
import io.micrometer.core.instrument.MeterRegistry;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.Objects;

@Component
public class RateLimitInterceptor implements HandlerInterceptor {

    private final RateLimitService rateLimitService;
    private final UserRepository userRepository;
    private final MeterRegistry meterRegistry;

    public RateLimitInterceptor(@Qualifier("fallbackRateLimitService") RateLimitService rateLimitService,
                                UserRepository userRepository,
                                MeterRegistry meterRegistry) {
        this.rateLimitService = rateLimitService;
        this.userRepository = userRepository;
        this.meterRegistry = meterRegistry;
    }

    @Override
    public boolean preHandle(HttpServletRequest request,
                              HttpServletResponse response,
                              Object handler) throws Exception {
        // Check valid user
        String email = Objects.requireNonNull(SecurityContextHolder.getContext().getAuthentication()).getName();
        User user = userRepository.findByEmail(email).orElseThrow(() -> new RuntimeException("User not found"));

        // Get plan of user
        String plan = user.getPlan().toString();

        // Take suitable rate limit for corresponding
        RateLimitPlan rateLimitPlan = RateLimitPlan.getPlanForUser(plan);

        String key = "user:" + email;

        if (rateLimitService.tryConsume(key, rateLimitPlan, 1)) {
            Long availableTokens = rateLimitService.getAvailableTokens(key, rateLimitPlan);
            // Add rate limits headers
            response.setHeader("X-Rate-Limit-Remaining", String.valueOf(availableTokens));
            response.setHeader("X-RateLimit-Limit", String.valueOf(rateLimitPlan.getCapacity()));
            response.setHeader("X-RateLimit-Reset", String.valueOf(System.currentTimeMillis() + rateLimitPlan.getDuration().toMillis()));
            return true;
        }
        else {
            meterRegistry.counter(
                    "ratelimit.blocked",
                    "endpoint", request.getRequestURI(),
                    "method", request.getMethod(),
                    "plan", rateLimitPlan.name()
            ).increment();
            response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
            response.setHeader("X-RateLimit-Retry-After-Seconds", rateLimitPlan.getDuration().toSeconds() + "s");
            response.getWriter().write(
                    "{\"error\": \"Rate limit exceeded. Try again after "
                            + rateLimitPlan.getDuration().toSeconds()
                            + " seconds\"}"
            );
            return false;
        }
    }
}
