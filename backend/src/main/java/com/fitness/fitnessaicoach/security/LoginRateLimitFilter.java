package com.fitness.fitnessaicoach.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
@Slf4j
public class LoginRateLimitFilter extends OncePerRequestFilter {
    private static final int MAX_ATTEMPTS_PER_WINDOW = 5;
    private static final Duration WINDOW = Duration.ofMinutes(1);

    private final Map<String, Deque<Instant>> failedAttemptsByIp = new ConcurrentHashMap<>();

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {
        if (!isLoginRequest(request)) {
            filterChain.doFilter(request, response);
            return;
        }

        String clientIp = resolveClientIp(request);
        Instant now = Instant.now();

        if (isBlocked(clientIp, now)) {
            log.warn("Blocked login attempt due to rate limit from IP {}", clientIp);
            response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
            response.setHeader("Retry-After", String.valueOf(WINDOW.toSeconds()));
            response.getWriter().write("Too many login attempts. Please try again later.");
            return;
        }

        filterChain.doFilter(request, response);

        if (response.getStatus() >= 400 && response.getStatus() != HttpStatus.TOO_MANY_REQUESTS.value()) {
            recordFailedAttempt(clientIp, now);
            return;
        }

        clearAttempts(clientIp);
    }

    private boolean isLoginRequest(HttpServletRequest request) {
        return HttpMethod.POST.matches(request.getMethod()) && "/api/auth/login".equals(request.getRequestURI());
    }

    private String resolveClientIp(HttpServletRequest request) {
        String forwardedFor = request.getHeader("X-Forwarded-For");
        if (forwardedFor != null && !forwardedFor.isBlank()) {
            return forwardedFor.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }

    private boolean isBlocked(String clientIp, Instant now) {
        return failedAttemptsByIp.compute(clientIp, (ip, attempts) -> {
            Deque<Instant> currentAttempts = attempts == null ? new ArrayDeque<>() : attempts;
            pruneExpiredAttempts(currentAttempts, now);
            return currentAttempts;
        }).size() >= MAX_ATTEMPTS_PER_WINDOW;
    }

    private void recordFailedAttempt(String clientIp, Instant now) {
        failedAttemptsByIp.compute(clientIp, (ip, attempts) -> {
            Deque<Instant> currentAttempts = attempts == null ? new ArrayDeque<>() : attempts;
            pruneExpiredAttempts(currentAttempts, now);
            currentAttempts.addLast(now);
            return currentAttempts;
        });
    }

    private void clearAttempts(String clientIp) {
        failedAttemptsByIp.remove(clientIp);
    }

    private void pruneExpiredAttempts(Deque<Instant> attempts, Instant now) {
        Instant cutoff = now.minus(WINDOW);
        while (!attempts.isEmpty() && attempts.peekFirst().isBefore(cutoff)) {
            attempts.removeFirst();
        }
    }
}
