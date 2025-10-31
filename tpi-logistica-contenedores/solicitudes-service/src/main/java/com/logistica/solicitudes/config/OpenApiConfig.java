package com.logistica.solicitudes.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI solicitudesOpenAPI() {
        return new OpenAPI()
            .info(new Info()
                .title("Solicitudes Service API")
                .description("API para gestión de solicitudes de transporte y contenedores")
                .version("1.0.0"));
    }
}
