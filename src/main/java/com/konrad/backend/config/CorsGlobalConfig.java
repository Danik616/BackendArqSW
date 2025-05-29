package com.konrad.backend.config;


import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.config.CorsRegistry;
import org.springframework.web.reactive.config.WebFluxConfigurer;


@Configuration
public class CorsGlobalConfig implements WebFluxConfigurer {

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**") // Permite todos los endpoints
                .allowedOrigins("http://localhost:3000") // O desde donde sirvas React
                .allowedMethods("*")
                .allowedHeaders("*");
    }
}