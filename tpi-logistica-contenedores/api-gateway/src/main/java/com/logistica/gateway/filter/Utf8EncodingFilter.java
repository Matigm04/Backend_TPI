package com.logistica.gateway.filter;

import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;

import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpRequestDecorator;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;

/**
 * Filtro global que asegura que todas las peticiones con contenido JSON
 * sean procesadas correctamente con codificación UTF-8.
 * 
 * Este filtro soluciona problemas de encoding cuando los clientes envían
 * caracteres especiales (tildes, ñ, etc.) en el body del request.
 */
@Slf4j
@Component
public class Utf8EncodingFilter implements GlobalFilter, Ordered {

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        
        // Solo procesar peticiones con body (POST, PUT, PATCH)
        String method = request.getMethod().name();
        if (!method.equals("POST") && !method.equals("PUT") && !method.equals("PATCH")) {
            return chain.filter(exchange);
        }
        
        // Solo procesar si es JSON
        MediaType contentType = request.getHeaders().getContentType();
        if (contentType == null || !contentType.includes(MediaType.APPLICATION_JSON)) {
            return chain.filter(exchange);
        }
        
        log.debug("Aplicando filtro UTF-8 para: {} {}", method, request.getURI());
        
        // Decorador que asegura UTF-8 en los headers
        ServerHttpRequestDecorator decoratedRequest = new ServerHttpRequestDecorator(request) {
            @Override
            public HttpHeaders getHeaders() {
                HttpHeaders headers = new HttpHeaders();
                headers.putAll(super.getHeaders());
                
                // Asegurar que Content-Type incluya charset=UTF-8
                if (contentType != null) {
                    MediaType mediaType = new MediaType(
                        contentType.getType(),
                        contentType.getSubtype(),
                        StandardCharsets.UTF_8
                    );
                    headers.setContentType(mediaType);
                }
                
                // Asegurar que Accept-Charset incluya UTF-8
                if (!headers.containsKey(HttpHeaders.ACCEPT_CHARSET)) {
                    headers.set(HttpHeaders.ACCEPT_CHARSET, StandardCharsets.UTF_8.name());
                }
                
                return headers;
            }
            

        };
        
        // Continuar con el request decorado
        return chain.filter(exchange.mutate().request(decoratedRequest).build());
    }

    @Override
    public int getOrder() {
        // Ejecutar antes que LoggingGlobalFilter (que tiene order -1)
        return -2;
    }
}
