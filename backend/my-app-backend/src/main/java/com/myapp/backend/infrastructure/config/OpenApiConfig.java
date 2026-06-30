package com.myapp.backend.infrastructure.config;

import io.swagger.v3.oas.models.media.Schema;
import org.springdoc.core.customizers.OpenApiCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Map;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenApiCustomizer fixDateExamples() {
        return openApi -> {
            Map<String, Schema> schemas = openApi.getComponents().getSchemas();
            if (schemas == null) return;
            schemas.values().forEach(schema -> {
                Map<String, Schema> properties = schema.getProperties();
                if (properties == null) return;
                properties.forEach((field, prop) -> {
                    if ("date".equals(prop.getFormat())) {
                        prop.setExample("1985-06-15");
                    }
                });
            });
        };
    }
}
