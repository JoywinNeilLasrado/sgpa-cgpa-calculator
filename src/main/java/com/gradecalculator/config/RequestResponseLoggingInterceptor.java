package com.gradecalculator.config;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * Enterprise-grade Request/Response logging interceptor.
 * Logs method, path, remote address, query details, response status codes, and execution durations in ms.
 */
@Component
public class RequestResponseLoggingInterceptor implements HandlerInterceptor {
    private static final Logger logger = LoggerFactory.getLogger(RequestResponseLoggingInterceptor.class);

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        request.setAttribute("startTime", System.currentTimeMillis());
        
        String query = request.getQueryString() != null ? "?" + request.getQueryString() : "";
        logger.info("Incoming Request: {} {}{} | Remote IP: {}", 
                request.getMethod(), 
                request.getRequestURI(), 
                query,
                request.getRemoteAddr());
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        Long startTime = (Long) request.getAttribute("startTime");
        long duration = startTime != null ? System.currentTimeMillis() - startTime : 0;
        
        logger.info("Outgoing Response: {} {} | Status: {} | Duration: {}ms", 
                request.getMethod(), 
                request.getRequestURI(), 
                response.getStatus(), 
                duration);
    }
}
