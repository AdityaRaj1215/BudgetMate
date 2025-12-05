package com.personalfin.server.security.filter;

import com.personalfin.server.security.exception.SecurityException;
import com.personalfin.server.security.util.SecurityUtils;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.ContentCachingRequestWrapper;

import java.io.IOException;
import java.util.Map;

@Component
public class InputSanitizationFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain) throws ServletException, IOException {

        // Check query parameters for malicious input
        Map<String, String[]> parameterMap = request.getParameterMap();
        for (Map.Entry<String, String[]> entry : parameterMap.entrySet()) {
            String paramName = entry.getKey();
            String[] paramValues = entry.getValue();

            // Check for SQL injection
            for (String value : paramValues) {
                if (SecurityUtils.containsSqlInjection(value)) {
                    throw new SecurityException("Invalid input detected in parameter: " + paramName);
                }
                if (SecurityUtils.containsXss(value)) {
                    throw new SecurityException("Invalid input detected in parameter: " + paramName);
                }
                if (SecurityUtils.containsPathTraversal(value)) {
                    throw new SecurityException("Invalid input detected in parameter: " + paramName);
                }
            }
        }

        // Check path for path traversal
        String path = request.getRequestURI();
        if (SecurityUtils.containsPathTraversal(path)) {
            throw new SecurityException("Invalid path detected");
        }

        filterChain.doFilter(request, response);
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        // Skip filtering for actuator endpoints
        String path = request.getRequestURI();
        return path.startsWith("/actuator/");
    }
}

