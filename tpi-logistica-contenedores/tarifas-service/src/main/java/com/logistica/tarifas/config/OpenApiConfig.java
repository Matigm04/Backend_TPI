package com.logistica.tarifas.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.Contact;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {
    
    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Tarifas Service API")
                        .version("1.0.0")
                        .description("API REST para la gestión de tarifas y configuraciones del sistema de logística")
                        .contact(new Contact()
                                .name("Equipo de Desarrollo")
                                .email("dev@logistica.com")));
    }
}
