package com.fitness.fitnessaicoach.security;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class LogSanitizerTest {

    @Test
    void shouldMaskEmailForAuthLogs() {
        assertEquals("j***@mail.com", LogSanitizer.sanitizeEmail("john@mail.com"));
    }

    @Test
    void shouldRedactSensitiveValuesFromMessages() {
        String message = "Authorization=Bearer abc.def.ghi apiKey=secret password=1234 jwt=token";

        String sanitized = LogSanitizer.sanitizeMessage(message);

        assertFalse(sanitized.contains("abc.def.ghi"));
        assertFalse(sanitized.contains("secret"));
        assertFalse(sanitized.contains("1234"));
        assertFalse(sanitized.contains("token"));
        assertTrue(sanitized.contains("[REDACTED]"));
    }
}
