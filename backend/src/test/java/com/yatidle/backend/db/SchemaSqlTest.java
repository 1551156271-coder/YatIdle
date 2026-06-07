package com.yatidle.backend.db;

import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;

class SchemaSqlTest {

    @Test
    void wantedConditionLevelUsesVarcharForCurrentConditionValues() throws Exception {
        String schema = Files.readString(Path.of("db", "schema.sql"));

        assertThat(schema)
                .contains("condition_level VARCHAR(50) DEFAULT NULL COMMENT '期望成色'")
                .doesNotContain("condition_level ENUM('全新','99新','95新','9成新','八成新','八成新以下') DEFAULT NULL COMMENT '期望成色'");
    }
}
