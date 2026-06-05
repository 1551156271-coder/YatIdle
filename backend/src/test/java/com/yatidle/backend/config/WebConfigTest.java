package com.yatidle.backend.config;

import org.junit.jupiter.api.Test;

import java.nio.file.Paths;

import static org.assertj.core.api.Assertions.assertThat;

class WebConfigTest {

    @Test
    void uploadResourceLocationEndsWithSlash() {
        String location = WebConfig.uploadResourceLocation(Paths.get("uploads"));

        assertThat(location).startsWith("file:");
        assertThat(location).endsWith("/");
    }

    @Test
    void projectUploadFallbackLocationEndsWithSlash() {
        String location = WebConfig.uploadResourceLocation(Paths.get("..", "uploads"));

        assertThat(location).startsWith("file:");
        assertThat(location).endsWith("/");
    }
}
