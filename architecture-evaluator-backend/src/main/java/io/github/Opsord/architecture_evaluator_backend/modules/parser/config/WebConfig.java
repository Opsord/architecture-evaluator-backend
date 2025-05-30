package io.github.Opsord.architecture_evaluator_backend.modules.parser.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**") // Allow CORS for all endpoints
                .allowedOrigins("http://localhost:3000", "http://127.0.0.1:3000", "http://localhost:5173") // Allowed origins
                .allowedMethods("POST") // Allowed HTTP methods
                .allowedHeaders("*") // Allow all headers
                .allowCredentials(true); // Allow credentials (e.g., cookies, authentication headers)
    }
}