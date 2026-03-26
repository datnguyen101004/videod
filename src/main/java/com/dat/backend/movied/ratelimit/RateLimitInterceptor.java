package com.dat.backend.movied.ratelimit;

import com.dat.backend.movied.user.entity.User;
import com.dat.backend.movied.user.repository.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.Objects;

@Component
public class RateLimitInterceptor implements HandlerInterceptor {

    private final RateLimitService rateLimitService;
    private final UserRepository userRepository;

    public RateLimitInterceptor(RateLimitService rateLimitService,
                                UserRepository userRepository) {
        this.rateLimitService = rateLimitService;
        this.userRepository = userRepository;
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
        RateLimitPlan rateLimitPlan = RateLimitPlan.getPlanForUser(email,plan);

        if (rateLimitService.tryConsume(email, rateLimitPlan, 1)) {
            Long availableTokens = rateLimitService.getAvailableTokens(email, rateLimitPlan);
            // Add rate limits headers
            response.setHeader("X-Rate-Limit-Remaining", String.valueOf(availableTokens));
            response.setHeader("X-RateLimit-Limit", String.valueOf(rateLimitPlan.getCapacity()));
            response.setHeader("X-RateLimit-Reset", String.valueOf(System.currentTimeMillis() + rateLimitPlan.getDuration().toMillis()));
            return true;
        }
        else {
            response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
            response.setHeader("X-RateLimit-Retry-After-Seconds", "300");
            response.getWriter().write("{\"error\": \"Rate limit exceeded. Try again after 300s.\"}");
            return false;
        }
    }
}
