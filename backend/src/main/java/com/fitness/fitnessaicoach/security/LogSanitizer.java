package com.fitness.fitnessaicoach.security;

import java.util.regex.Pattern;

public final class LogSanitizer {
    private static final Pattern BEARER_TOKEN_PATTERN = Pattern.compile("(?i)bearer\\s+[a-z0-9\\-._~+/]+=*");
    private static final Pattern AUTHORIZATION_PATTERN = Pattern.compile("(?i)(authorization\\s*[:=]\\s*)([^,\\s]+)");
    private static final Pattern API_KEY_PATTERN = Pattern.compile("(?i)(api[-_ ]?key\\s*[:=]\\s*)([^,\\s]+)");
    private static final Pattern PASSWORD_PATTERN = Pattern.compile("(?i)(password\\s*[:=]\\s*)([^,\\s]+)");
    private static final Pattern JWT_PATTERN = Pattern.compile("(?i)(jwt\\s*[:=]\\s*)([^,\\s]+)");
    private static final String REDACTED = "[REDACTED]";

    private LogSanitizer() {
    }

    public static String sanitizeMessage(String message) {
        if (message == null || message.isBlank()) {
            return "";
        }

        String sanitized = BEARER_TOKEN_PATTERN.matcher(message).replaceAll("Bearer " + REDACTED);
        sanitized = AUTHORIZATION_PATTERN.matcher(sanitized).replaceAll("$1" + REDACTED);
        sanitized = API_KEY_PATTERN.matcher(sanitized).replaceAll("$1" + REDACTED);
        sanitized = PASSWORD_PATTERN.matcher(sanitized).replaceAll("$1" + REDACTED);
        sanitized = JWT_PATTERN.matcher(sanitized).replaceAll("$1" + REDACTED);
        return sanitized;
    }

    public static String sanitizeEmail(String email) {
        if (email == null || email.isBlank()) {
            return "unknown";
        }

        String trimmed = email.trim();
        int atIndex = trimmed.indexOf('@');
        if (atIndex <= 0 || atIndex == trimmed.length() - 1) {
            return REDACTED;
        }

        return trimmed.charAt(0) + "***@" + trimmed.substring(atIndex + 1);
    }

    public static String sanitizeExceptionMessage(Throwable throwable) {
        if (throwable == null) {
            return "";
        }
        return sanitizeMessage(throwable.getMessage());
    }
}
