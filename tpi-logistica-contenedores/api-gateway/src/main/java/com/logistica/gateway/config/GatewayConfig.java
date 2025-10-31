package com.logistica.gateway.config;

import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class GatewayConfig {

    @Bean
    public RouteLocator customRouteLocator(RouteLocatorBuilder builder) {
        return builder.routes()
                .route("clientes-swagger", r -> r
                        .path("/clientes/v3/api-docs/**", "/clientes/swagger-ui/**")
                        .uri("http://localhost:8081"))
                .route("depositos-swagger", r -> r
                        .path("/depositos/v3/api-docs/**", "/depositos/swagger-ui/**")
                        .uri("http://localhost:8082"))
                .route("camiones-swagger", r -> r
                        .path("/camiones/v3/api-docs/**", "/camiones/swagger-ui/**")
                        .uri("http://localhost:8083"))
                .route("tarifas-swagger", r -> r
                        .path("/tarifas/v3/api-docs/**", "/tarifas/swagger-ui/**")
                        .uri("http://localhost:8084"))
                .route("solicitudes-swagger", r -> r
                        .path("/solicitudes/v3/api-docs/**", "/solicitudes/swagger-ui/**")
                        .uri("http://localhost:8085"))
                .route("rutas-swagger", r -> r
                        .path("/rutas/v3/api-docs/**", "/rutas/swagger-ui/**")
                        .uri("http://localhost:8086"))
                .build();
    }
}
