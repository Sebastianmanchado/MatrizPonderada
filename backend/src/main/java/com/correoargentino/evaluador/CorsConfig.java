package com.correoargentino.evaluador;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * CORS: permite requests desde la URL del frontend definida en la variable
 * de entorno {@code FRONTEND_URL} (o {@code http://localhost:3000} por
 * defecto en local). Habilita todos los métodos HTTP y headers para todos
 * los endpoints.
 */
@Configuration
public class CorsConfig implements WebMvcConfigurer {

    @Value("${frontend.url}")
    private String frontendUrl;

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOrigins(frontendUrl)
                .allowedMethods("*")
                .allowedHeaders("*")
                .allowCredentials(true);
    }
}
