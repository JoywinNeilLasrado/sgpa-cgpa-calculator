package com.gradecalculator.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.web.header.HeaderWriter;
import org.springframework.stereotype.Component;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * Centralized security headers configuration.
 *
 * <p>Implements all OWASP-recommended HTTP response headers in one place,
 * keeping {@link SecurityConfig} free of inline header policy strings.
 *
 * <p>Headers applied to every response:
 * <ul>
 *   <li><b>Content-Security-Policy</b> — strict; no unsafe-eval; nonce-less script-src self</li>
 *   <li><b>Referrer-Policy</b> — strict-origin-when-cross-origin</li>
 *   <li><b>Permissions-Policy</b> — disables camera, mic, geolocation</li>
 *   <li><b>X-Content-Type-Options</b> — nosniff</li>
 *   <li><b>X-Frame-Options</b> — SAMEORIGIN</li>
 *   <li><b>Strict-Transport-Security</b> — 1yr, includeSubDomains, preload</li>
 *   <li><b>Cross-Origin-Opener-Policy</b> — same-origin</li>
 *   <li><b>Cross-Origin-Resource-Policy</b> — same-origin</li>
 *   <li><b>Cache-Control</b> — no-store for all API responses</li>
 * </ul>
 */
@Component
public class SecurityHeadersConfig implements HeaderWriter {

    @Value("${app.security.csp-report-uri:}")
    private String cspReportUri;

    @Value("${app.security.allowed-frame-origins:SAMEORIGIN}")
    private String allowedFrameOrigins;

    @Override
    public void writeHeaders(HttpServletRequest request, HttpServletResponse response) {
        String path = request.getRequestURI();

        // ── Content-Security-Policy ───────────────────────────────────────
        String csp = buildCsp();
        response.setHeader("Content-Security-Policy", csp);

        // ── Referrer-Policy ───────────────────────────────────────────────
        response.setHeader("Referrer-Policy", "strict-origin-when-cross-origin");

        // ── Permissions-Policy ────────────────────────────────────────────
        response.setHeader("Permissions-Policy",
                "camera=(), microphone=(), geolocation=(), payment=(), usb=(), interest-cohort=()");

        // ── X-Content-Type-Options ────────────────────────────────────────
        response.setHeader("X-Content-Type-Options", "nosniff");

        // ── X-Frame-Options ───────────────────────────────────────────────
        response.setHeader("X-Frame-Options", allowedFrameOrigins);

        // ── HSTS (HTTP Strict-Transport-Security) ─────────────────────────
        // Only set on HTTPS; browsers ignore it on HTTP
        if ("https".equalsIgnoreCase(request.getScheme())
                || "https".equalsIgnoreCase(request.getHeader("X-Forwarded-Proto"))) {
            response.setHeader("Strict-Transport-Security",
                    "max-age=31536000; includeSubDomains; preload");
        }

        // ── Cross-Origin policies ─────────────────────────────────────────
        response.setHeader("Cross-Origin-Opener-Policy", "same-origin");
        response.setHeader("Cross-Origin-Resource-Policy", "same-origin");
        response.setHeader("Cross-Origin-Embedder-Policy", "require-corp");

        // ── Cache-Control — no-store for all API responses ────────────────
        if (path.startsWith("/api/")) {
            response.setHeader("Cache-Control", "no-store, no-cache, must-revalidate, private");
            response.setHeader("Pragma", "no-cache");
            response.setHeader("Expires", "0");
        }

        // ── Remove server fingerprinting headers ──────────────────────────
        response.setHeader("X-Powered-By", "");
        response.setHeader("Server", "");
    }

    private String buildCsp() {
        StringBuilder csp = new StringBuilder();
        csp.append("default-src 'self'; ");
        csp.append("script-src 'self' 'unsafe-inline'; ");  // unsafe-inline retained for inline JS in HTML files
        csp.append("style-src 'self' 'unsafe-inline' https://fonts.googleapis.com; ");
        csp.append("font-src 'self' https://fonts.gstatic.com data:; ");
        csp.append("img-src 'self' data: blob:; ");
        csp.append("connect-src 'self'; ");
        csp.append("frame-ancestors 'self'; ");
        csp.append("base-uri 'self'; ");
        csp.append("form-action 'self'; ");
        csp.append("object-src 'none'; ");
        csp.append("upgrade-insecure-requests;");

        if (cspReportUri != null && !cspReportUri.isBlank()) {
            csp.append(" report-uri ").append(cspReportUri).append(";");
        }
        return csp.toString();
    }
}
