package com.logistica.gateway.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/fallback")
public class FallbackController {

    @GetMapping("/clientes")
    public ResponseEntity<Map<String, Object>> clientesFallback() {
        return buildFallbackResponse("Clientes Service");
    }

    @GetMapping("/depositos")
    public ResponseEntity<Map<String, Object>> depositosFallback() {
        return buildFallbackResponse("Depositos Service");
    }

    @GetMapping("/camiones")
    public ResponseEntity<Map<String, Object>> camionesFallback() {
        return buildFallbackResponse("Camiones Service");
    }

    @GetMapping("/tarifas")
    public ResponseEntity<Map<String, Object>> tarifasFallback() {
        return buildFallbackResponse("Tarifas Service");
    }

    @GetMapping("/solicitudes")
    public ResponseEntity<Map<String, Object>> solicitudesFallback() {
        return buildFallbackResponse("Solicitudes Service");
    }

    @GetMapping("/rutas")
    public ResponseEntity<Map<String, Object>> rutasFallback() {
        return buildFallbackResponse("Rutas Service");
    }

    private ResponseEntity<Map<String, Object>> buildFallbackResponse(String serviceName) {
        Map<String, Object> response = new HashMap<>();
        response.put("timestamp", LocalDateTime.now());
        response.put("status", HttpStatus.SERVICE_UNAVAILABLE.value());
        response.put("error", "Service Unavailable");
        response.put("message", serviceName + " is currently unavailable. Please try again later.");
        
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(response);
    }
}
