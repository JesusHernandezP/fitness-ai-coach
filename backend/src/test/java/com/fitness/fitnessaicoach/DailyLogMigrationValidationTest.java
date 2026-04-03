package com.fitness.fitnessaicoach;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@ActiveProfiles("test")
@TestPropertySource(properties = {
        "app.swagger.public=true",
        "springdoc.api-docs.enabled=true",
        "springdoc.swagger-ui.enabled=true"
})
public class DailyLogMigrationValidationTest {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    void dailyLogsTableShouldExposeSingleUniqueConstraintForUserAndDateInTestSchema() {
        Integer matchCount = jdbcTemplate.queryForObject("""
                SELECT COUNT(*)
                FROM INFORMATION_SCHEMA.TABLE_CONSTRAINTS
                WHERE TABLE_NAME = 'DAILY_LOGS' AND CONSTRAINT_NAME = 'UK_DAILY_LOGS_USER_DATE'
                """, Integer.class);

        assertEquals(1, matchCount);
    }

    @Test
    void dailyLogUniqueConstraintMigrationScriptShouldBePresent() throws IOException {
        ClassPathResource migrationResource = new ClassPathResource("db/migration/V027__enforce_unique_daily_log_per_user_day.sql");
        assertTrue(migrationResource.exists());
        assertTrue(migrationResource.contentLength() > 0);
    }
}
