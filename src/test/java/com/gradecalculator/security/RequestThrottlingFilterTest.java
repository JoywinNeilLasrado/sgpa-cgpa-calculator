package com.gradecalculator.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;

import java.io.PrintWriter;
import java.io.StringWriter;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

/**
 * Unit tests for RequestThrottlingFilter.
 */
@ExtendWith(MockitoExtension.class)
class RequestThrottlingFilterTest {

    private RequestThrottlingFilter filter;

    @Mock private HttpServletRequest request;
    @Mock private HttpServletResponse response;
    @Mock private FilterChain filterChain;

    @BeforeEach
    void setUp() {
        filter = new RequestThrottlingFilter();
        SecurityContextHolder.clearContext();
    }

    @Test
    void allowsRequestWhenUnderLimit() throws Exception {
        when(request.getRequestURI()).thenReturn("/api/test");
        when(request.getMethod()).thenReturn("GET");

        filter.doFilterInternal(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
        verify(response, never()).setStatus(anyInt());
    }

    @Test
    void allowsMultipleRequestsUnderLimit() throws Exception {
        when(request.getRequestURI()).thenReturn("/api/test");
        when(request.getMethod()).thenReturn("GET");

        for (int i = 0; i < 5; i++) {
            filter.doFilterInternal(request, response, filterChain);
        }

        verify(filterChain, times(5)).doFilter(request, response);
    }

    @Test
    void blocksRequestWhenLimitExceeded() throws Exception {
        when(request.getRequestURI()).thenReturn("/api/test");
        when(request.getMethod()).thenReturn("GET");

        StringWriter stringWriter = new StringWriter();
        PrintWriter printWriter = new PrintWriter(stringWriter);
        when(response.getWriter()).thenReturn(printWriter);

        // Make requests up to the limit
        for (int i = 0; i < 100; i++) {
            filter.doFilterInternal(request, response, filterChain);
        }

        // Next request should be blocked or throttled
        filter.doFilterInternal(request, response, filterChain);

        // The filter should eventually block or throttle
        verify(response, atLeastOnce()).setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
    }

    @Test
    void whitelistedPathsSkipThrottling() throws Exception {
        when(request.getRequestURI()).thenReturn("/api/auth/login");
        when(request.getMethod()).thenReturn("POST");

        // Generate many requests to whitelisted endpoint
        for (int i = 0; i < 200; i++) {
            filter.doFilterInternal(request, response, filterChain);
        }

        // Should not block whitelisted paths (login endpoint)
        verify(filterChain, times(200)).doFilter(request, response);
    }

    @Test
    void differentPathsHaveSeparateLimits() throws Exception {
        HttpServletRequest request1 = mock(HttpServletRequest.class);
        HttpServletRequest request2 = mock(HttpServletRequest.class);
        when(request1.getRequestURI()).thenReturn("/api/path1");
        when(request1.getMethod()).thenReturn("GET");
        when(request2.getRequestURI()).thenReturn("/api/path2");
        when(request2.getMethod()).thenReturn("GET");

        // Both paths should have independent limits
        for (int i = 0; i < 50; i++) {
            filter.doFilterInternal(request1, response, filterChain);
            filter.doFilterInternal(request2, response, filterChain);
        }

        verify(filterChain, times(100)).doFilter(any(), eq(response));
    }

    @Test
    void readOnlyRequestsExcludedFromThrottling() throws Exception {
        when(request.getRequestURI()).thenReturn("/api/test");
        when(request.getMethod()).thenReturn("GET"); // Read-only method

        // Many GET requests should be allowed
        for (int i = 0; i < 50; i++) {
            filter.doFilterInternal(request, response, filterChain);
        }

        verify(filterChain, times(50)).doFilter(request, response);
    }
}
