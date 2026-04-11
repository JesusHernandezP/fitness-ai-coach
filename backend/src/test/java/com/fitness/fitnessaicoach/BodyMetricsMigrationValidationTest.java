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
public class BodyMetricsMigrationValidationTest {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    void bodyMetricsTableShouldExistInTestSchema() {
        Integer matchCount = jdbcTemplate.queryForObject("""
                SELECT COUNT(*)
                FROM INFORMATION_SCHEMA.TABLES
                WHERE TABLE_NAME = 'BODY_METRICS'
                """, Integer.class);

        assertEquals(1, matchCount);
    }

    @Test
    void bodyMetricsUserDateConstraintShouldExistInTestSchema() {
        Integer matchCount = jdbcTemplate.queryForObject("""
                SELECT COUNT(*)
                FROM INFORMATION_SCHEMA.TABLE_CONSTRAINTS
                WHERE TABLE_NAME = 'BODY_METRICS'
                  AND CONSTRAINT_NAME = 'UNIQUE_BODY_METRICS_USER_DATE'
                  AND CONSTRAINT_TYPE = 'UNIQUE'
                """, Integer.class);

        assertEquals(1, matchCount);
    }

    @Test
    void bodyMetricsMigrationScriptShouldBePresent() throws IOException {
        ClassPathResource migrationResource = new ClassPathResource("db/migration/V028__create_body_metrics_table.sql");
        assertTrue(migrationResource.exists());
        assertTrue(migrationResource.contentLength() > 0);
    }

    @Test
    void bodyMetricsTableShouldStoreExtendedCompositionColumns() {
        Integer bodyFatColumns = jdbcTemplate.queryForObject("""
                SELECT COUNT(*)
                FROM INFORMATION_SCHEMA.COLUMNS
                WHERE TABLE_NAME = 'BODY_METRICS'
                  AND COLUMN_NAME = 'BODY_FAT'
                """, Integer.class);
        Integer muscleMassColumns = jdbcTemplate.queryForObject("""
                SELECT COUNT(*)
                FROM INFORMATION_SCHEMA.COLUMNS
                WHERE TABLE_NAME = 'BODY_METRICS'
                  AND COLUMN_NAME = 'MUSCLE_MASS'
                """, Integer.class);

        assertEquals(1, bodyFatColumns);
        assertEquals(1, muscleMassColumns);
    }
}
