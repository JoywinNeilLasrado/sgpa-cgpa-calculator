package com.gradecalculator.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.gradecalculator.dto.response.ErrorResponse;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

/**
 * Filter that enforces per-endpoint rate limits (throttling) per client IP.
 * Uses a Redis backing store if configured, otherwise falls back to a local thread-safe counter structure.
 */
@Component
public class RequestThrottlingFilter extends OncePerRequestFilter {

    private static final Logger logger = LoggerFactory.getLogger(RequestThrottlingFilter.class);




    @Value("${app.throttle.limit:60}")
    private int throttleLimit = 60;

    @Value("${app.throttle.window-ms:60000}")
    private long windowMs = 60000;

    @Autowired(required = false)
    private org.springframework.data.redis.core.StringRedisTemplate redisTemplate;

    private final ConcurrentHashMap<String, ThrottlerState> localStates = new ConcurrentHashMap<>();

    @Autowired
    private ObjectMapper objectMapper = new ObjectMapper()
            .registerModule(new JavaTimeModule())
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        String ip = getClientIp(request);
        String uri = request.getRequestURI();
        String method = request.getMethod();
        // Bypass throttling only for whitelisted login endpoint
        if (uri.endsWith("/login")) {
            filterChain.doFilter(request, response);
            return;
        }
        String throttleKey = String.format("throttle:%s:%s:%s", ip, method, uri);

        boolean isThrottled = false;
        long currentCount = 0;

        if (redisTemplate != null) {
            try {
                Long count = redisTemplate.opsForValue().increment(throttleKey);
                if (count != null) {
                    currentCount = count;
                    if (count == 1) {
                        redisTemplate.expire(throttleKey, windowMs, TimeUnit.MILLISECONDS);
                    }
                    if (count > throttleLimit) {
                        isThrottled = true;
                    }
                }
            } catch (Exception e) {
                // fall through to in-memory fallback
            }
        }

        if (redisTemplate == null || currentCount == 0) {
            long now = System.currentTimeMillis();
            ThrottlerState state = localStates.compute(throttleKey, (key, value) -> {
                if (value == null || (now - value.windowStartMs > windowMs)) {
                    return new ThrottlerState(1, now);
                }
                value.count++;
                return value;
            });
            currentCount = state.count;
            if (state.count > throttleLimit) {
                isThrottled = true;
            }
        }

        if (isThrottled) {
            logger.warn(String.format("Security Warning: Client IP '%s' has been throttled on endpoint '%s %s' after exceeding limit of %d requests per %d ms.",
                    ip, method, uri, throttleLimit, windowMs));

            response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);

            ErrorResponse error = new ErrorResponse(
                    "TOO_MANY_REQUESTS",
                    HttpStatus.TOO_MANY_REQUESTS.value(),
                    "Too many requests. Please try again later.",
                    uri,
                    LocalDateTime.now(),
                    List.of(String.format("Rate limit of %d requests per %d seconds exceeded.", throttleLimit, TimeUnit.MILLISECONDS.toSeconds(windowMs)))
            );

            response.getWriter().write(objectMapper.writeValueAsString(error));
            return;
        }

        filterChain.doFilter(request, response);
    }

    private String getClientIp(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.trim().isEmpty()) {
            return xForwardedFor.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }

    private static class ThrottlerState {
        int count;
        long windowStartMs;

        ThrottlerState(int count, long windowStartMs) {
            this.count = count;
            this.windowStartMs = windowStartMs;
        }
    }
}
