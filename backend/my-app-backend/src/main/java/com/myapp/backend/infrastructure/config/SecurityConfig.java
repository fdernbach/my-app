package com.myapp.backend.infrastructure.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Value("${swagger.port:8081}")
    private int swaggerPort;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .authorizeHttpRequests(auth -> auth
                // Swagger UI + OpenAPI spec : uniquement sur le port swagger
                .requestMatchers(req -> isSwaggerPath(req.getRequestURI()) && req.getLocalPort() == swaggerPort).permitAll()
                // Bloquer swagger sur le port principal
                .requestMatchers(req -> isSwaggerPath(req.getRequestURI())).denyAll()
                // Actuator health en accès libre
                .requestMatchers("/actuator/health").permitAll()
                // Tout le reste : authentifié
                .anyRequest().authenticated()
            )
            .httpBasic(Customizer.withDefaults())
            .csrf(csrf -> csrf.disable());
        return http.build();
    }

    private boolean isSwaggerPath(String uri) {
        return uri.startsWith("/swagger-ui") || uri.startsWith("/v3/api-docs");
    }
}
