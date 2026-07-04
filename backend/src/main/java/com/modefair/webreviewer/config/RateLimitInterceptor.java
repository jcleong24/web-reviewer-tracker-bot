package com.modefair.webreviewer.config;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.time.Duration;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Per-client token-bucket rate limiting for the analyze endpoint, backed by
 * Bucket4j. Protects the Anthropic token budget: clients that exhaust their
 * bucket receive HTTP 429.
 *
 * <p>Buckets are held in-memory keyed by client IP. For a horizontally scaled
 * deployment, swap the in-memory map for a distributed Bucket4j backend
 * (e.g. Redis / Hazelcast).
 */
@Component
public class RateLimitInterceptor implements HandlerInterceptor {

    private static final Logger log = LoggerFactory.getLogger(RateLimitInterceptor.class);
    private static final long CAPACITY = 20;
    private static final Duration REFILL_WINDOW = Duration.ofMinutes(1);

    private final ConcurrentHashMap<String, Bucket> buckets = new ConcurrentHashMap<>();

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        Bucket bucket = buckets.computeIfAbsent(request.getRemoteAddr(), key -> newBucket());
        if (bucket.tryConsume(1)) {
            return true;
        }
        log.warn("Rate limit exceeded for client {}", request.getRemoteAddr());
        response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
        return false;
    }

    private Bucket newBucket() {
        Bandwidth limit = Bandwidth.builder()
                .capacity(CAPACITY)
                .refillGreedy(CAPACITY, REFILL_WINDOW)
                .build();
        return Bucket.builder().addLimit(limit).build();
    }
}
