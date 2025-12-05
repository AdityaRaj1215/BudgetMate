package com.personalfin.server.security.util;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.util.UriUtils;

import java.nio.charset.StandardCharsets;
import java.util.regex.Pattern;

public class SecurityUtils {

    private static final Pattern SQL_INJECTION_PATTERN = Pattern.compile(
            "(?i)(union|select|insert|update|delete|drop|create|alter|exec|execute|script|javascript|onerror|onload)"
    );

    private static final Pattern XSS_PATTERN = Pattern.compile(
            "(?i)(<script|</script>|javascript:|onerror=|onload=|onclick=|<iframe|</iframe>)"
    );

    private static final Pattern PATH_TRAVERSAL_PATTERN = Pattern.compile(
            "(\\.\\./|\\.\\.\\\\|%2e%2e%2f|%2e%2e%5c)"
    );

    /**
     * Sanitize user input to prevent XSS attacks
     */
    public static String sanitizeInput(String input) {
        if (input == null) {
            return null;
        }
        // Remove potentially dangerous characters
        return input.replaceAll("[<>\"'&]", "")
                .replaceAll("(?i)javascript:", "")
                .replaceAll("(?i)on\\w+=", "");
    }

    /**
     * Check if input contains SQL injection patterns
     */
    public static boolean containsSqlInjection(String input) {
        if (input == null) {
            return false;
        }
        return SQL_INJECTION_PATTERN.matcher(input).find();
    }

    /**
     * Check if input contains XSS patterns
     */
    public static boolean containsXss(String input) {
        if (input == null) {
            return false;
        }
        return XSS_PATTERN.matcher(input).find();
    }

    /**
     * Check if input contains path traversal patterns
     */
    public static boolean containsPathTraversal(String input) {
        if (input == null) {
            return false;
        }
        return PATH_TRAVERSAL_PATTERN.matcher(input).find();
    }

    /**
     * Get client IP address from request
     */
    public static String getClientIpAddress(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0].trim();
        }
        String xRealIp = request.getHeader("X-Real-IP");
        if (xRealIp != null && !xRealIp.isEmpty()) {
            return xRealIp;
        }
        return request.getRemoteAddr();
    }

    /**
     * Validate and sanitize file name
     */
    public static String sanitizeFileName(String fileName) {
        if (fileName == null) {
            return null;
        }
        // Remove path separators and dangerous characters
        String sanitized = fileName.replaceAll("[\\\\/:*?\"<>|]", "_");
        // Limit length
        if (sanitized.length() > 255) {
            sanitized = sanitized.substring(0, 255);
        }
        return sanitized;
    }

    /**
     * URL decode safely
     */
    public static String urlDecode(String input) {
        if (input == null) {
            return null;
        }
        try {
            return UriUtils.decode(input, StandardCharsets.UTF_8);
        } catch (Exception e) {
            return input; // Return original if decoding fails
        }
    }
}

