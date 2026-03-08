package com.interview.demo.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.ContentCachingRequestWrapper;
import org.springframework.web.util.ContentCachingResponseWrapper;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.stream.Collectors;

/**
 * HttpLoggingFilter
 *
 * WHY OncePerRequestFilter?
 *   Spring's filter chain can call a filter multiple times (e.g. for forwarded requests).
 *   OncePerRequestFilter guarantees exactly one execution per HTTP request.
 *
 * WHY ContentCachingRequestWrapper / ContentCachingResponseWrapper?
 *   HttpServletRequest's InputStream can only be read ONCE.  After your controller
 *   reads the body (e.g. @RequestBody), the stream is exhausted – you can't read it again.
 *   ContentCachingRequestWrapper buffers the bytes so you can read them after the fact.
 *   ContentCachingResponseWrapper does the same for the response body.
 *
 * EXECUTION ORDER:
 *   1. Wrap request & response
 *   2. Log incoming request (URI, method, headers, body)
 *   3. Let the rest of the filter chain / controller execute  ← filterChain.doFilter()
 *   4. Log outgoing response (status, headers, body)
 *   5. Copy the cached response bytes back to the real response  ← MUST do this!
 */
@Slf4j
@Component
@Order(1)  // Run this filter before all others so nothing is missed
public class HttpLoggingFilter extends OncePerRequestFilter {

    // Skip logging for Swagger/H2 console noise
    private static final String[] SKIP_URLS = {
            "/swagger-ui", "/v3/api-docs", "/h2-console", "/favicon.ico"
    };

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String uri = request.getRequestURI();
        for (String skip : SKIP_URLS) {
            if (uri.startsWith(skip)) return true;
        }
        return false;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        // ── 1. Wrap so we can read body multiple times ────────────────────────
        ContentCachingRequestWrapper  wrappedReq = new ContentCachingRequestWrapper(request);
        ContentCachingResponseWrapper wrappedRes = new ContentCachingResponseWrapper(response);

        long startMs = System.currentTimeMillis();

        try {
            // ── 2. Let controller / security filters run ──────────────────────
            filterChain.doFilter(wrappedReq, wrappedRes);
        } finally {
            long durationMs = System.currentTimeMillis() - startMs;

            // ── 3. Log request ────────────────────────────────────────────────
            // NOTE: body bytes are only available AFTER filterChain.doFilter()
            //       because only then has Spring's DispatcherServlet read them.
            logRequest(wrappedReq);

            // ── 4. Log response ───────────────────────────────────────────────
            logResponse(wrappedRes, durationMs);

            // ── 5. CRITICAL: copy cached bytes back to the real response ──────
            // ContentCachingResponseWrapper holds the body in memory.
            // Without this call the client receives an empty body.
            wrappedRes.copyBodyToResponse();
        }
    }

    // ─────────────────────────── helpers ──────────────────────────────────────

    private void logRequest(ContentCachingRequestWrapper req) {
        String headers = Collections.list(req.getHeaderNames())
                .stream()
                .map(name -> name + ": " + req.getHeader(name))
                .collect(Collectors.joining(", "));

        String body = new String(req.getContentAsByteArray(), StandardCharsets.UTF_8);

        log.debug("""

                ┌──────────── INCOMING REQUEST ────────────────────────────────
                │ {} {}
                │ Headers : {}
                │ Body    : {}
                └──────────────────────────────────────────────────────────────""",
                req.getMethod(), req.getRequestURI(),
                headers.isBlank() ? "(none)" : headers,
                body.isBlank()    ? "(empty)" : body);
    }

    private void logResponse(ContentCachingResponseWrapper res, long durationMs) {
        String headers = res.getHeaderNames()
                .stream()
                .map(name -> name + ": " + res.getHeader(name))
                .collect(Collectors.joining(", "));

        String body = new String(res.getContentAsByteArray(), StandardCharsets.UTF_8);

        log.debug("""

                ┌──────────── OUTGOING RESPONSE ({} ms) ───────────────────────
                │ Status  : {}
                │ Headers : {}
                │ Body    : {}
                └──────────────────────────────────────────────────────────────""",
                durationMs,
                res.getStatus(),
                headers.isBlank() ? "(none)" : headers,
                body.isBlank()    ? "(empty)" : body);
    }
}
