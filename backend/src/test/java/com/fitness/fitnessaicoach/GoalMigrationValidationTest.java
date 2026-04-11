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
public class GoalMigrationValidationTest {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    void goalCreatedAtColumnShouldExistAndBeNonNullableInTestSchema() {
        Integer matchCount = jdbcTemplate.queryForObject("""
                SELECT COUNT(*)
                FROM INFORMATION_SCHEMA.COLUMNS
                WHERE TABLE_NAME = 'GOALS' AND COLUMN_NAME = 'CREATED_AT'
                """, Integer.class);

        assertEquals(1, matchCount);

        String nullable = jdbcTemplate.queryForObject("""
                SELECT IS_NULLABLE
                FROM INFORMATION_SCHEMA.COLUMNS
                WHERE TABLE_NAME = 'GOALS' AND COLUMN_NAME = 'CREATED_AT'
                """, String.class);

        assertEquals("NO", nullable);
    }

    @Test
    void goalCreatedAtMigrationScriptShouldBePresent() throws IOException {
        ClassPathResource migrationResource = new ClassPathResource("db/migration/V026__add_goal_created_at.sql");
        assertTrue(migrationResource.exists());
        assertTrue(migrationResource.contentLength() > 0);
    }
}