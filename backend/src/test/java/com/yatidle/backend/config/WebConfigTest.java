package com.yatidle.backend.config;

import org.junit.jupiter.api.Test;
import org.springframework.web.servlet.config.annotation.CorsRegistry;

import java.nio.file.Paths;
import java.util.Map;

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

    @Test
    void corsAllowsLanH5DebugRequestsWithoutCredentials() {
        WebConfig config = new WebConfig(null);
        TestCorsRegistry registry = new TestCorsRegistry();

        config.addCorsMappings(registry);

        Map<String, org.springframework.web.cors.CorsConfiguration> mappings = registry.mappings();
        org.springframework.web.cors.CorsConfiguration cors = mappings.get("/**");
        assertThat(cors).isNotNull();
        assertThat(cors.getAllowedOriginPatterns()).contains("*");
        assertThat(cors.getAllowedMethods()).contains("GET", "POST", "PUT", "DELETE", "OPTIONS");
        assertThat(cors.getAllowedHeaders()).contains("*");
        assertThat(cors.getAllowCredentials()).isFalse();
    }

    private static class TestCorsRegistry extends CorsRegistry {
        Map<String, org.springframework.web.cors.CorsConfiguration> mappings() {
            return getCorsConfigurations();
        }
    }
}
