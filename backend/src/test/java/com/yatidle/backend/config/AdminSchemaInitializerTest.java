package com.yatidle.backend.config;

import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.JdbcTemplate;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

class AdminSchemaInitializerTest {

    @Test
    void runCreatesAdminTablesWhenMissing() {
        JdbcTemplate jdbcTemplate = mock(JdbcTemplate.class);
        AdminSchemaInitializer initializer = new AdminSchemaInitializer(jdbcTemplate);

        initializer.run();

        verify(jdbcTemplate, times(2)).execute(anyString());
        verify(jdbcTemplate).execute(argThat((String sql) -> sql.contains("CREATE TABLE IF NOT EXISTS report")));
        verify(jdbcTemplate).execute(argThat((String sql) -> sql.contains("CREATE TABLE IF NOT EXISTS admin_action_log")));
    }
}
