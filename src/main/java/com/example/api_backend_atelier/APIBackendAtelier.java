package com.example.api_backend_atelier;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.lang.NonNull;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.List;

@SpringBootApplication
public class APIBackendAtelier {

    private static final Logger log = LoggerFactory.getLogger(APIBackendAtelier.class);

    public static void main(String[] args) {
        SpringApplication.run(APIBackendAtelier.class, args);
        log.info("APIBackendAtelier запущен.");
    }

    @Bean
    public WebMvcConfigurer corsConfigurer(
            @Value("${cors.allowed-origins}") List<String> allowedOrigins) {
        return new CustomCorsConfigurer(allowedOrigins);
    }

    private record CustomCorsConfigurer(List<String> allowedOrigins) implements WebMvcConfigurer {

        @Override
            public void addCorsMappings(@NonNull CorsRegistry registry) {
                registry.addMapping("/**")
                        .allowedOrigins(allowedOrigins.toArray(String[]::new))
                        .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH")
                        .allowedHeaders("*")
                        .allowCredentials(true)
                        .maxAge(3600);
            }
        }
}
