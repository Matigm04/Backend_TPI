package com.logistica.solicitudes.controller;

import com.logistica.solicitudes.dto.HistorialEstadoContenedorRequestDTO;
import com.logistica.solicitudes.dto.HistorialEstadoContenedorResponseDTO;
import com.logistica.solicitudes.service.HistorialEstadoContenedorService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/historial-estados")
@RequiredArgsConstructor
@Tag(name = "Historial Estado Contenedor", description = "Endpoints para gestionar el historial de estados de contenedores")
@SecurityRequirement(name = "bearer-jwt")
public class HistorialEstadoContenedorController {

    private final HistorialEstadoContenedorService historialService;

    @PostMapping
    @PreAuthorize("hasAnyRole('OPERADOR', 'TRANSPORTISTA')")
    @Operation(summary = "Registrar cambio de estado de contenedor")
    public ResponseEntity<HistorialEstadoContenedorResponseDTO> registrarCambioEstado(
            @Valid @RequestBody HistorialEstadoContenedorRequestDTO requestDTO) {
        HistorialEstadoContenedorResponseDTO response = historialService.registrarCambioEstado(requestDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    @PreAuthorize("hasRole('OPERADOR')")
    @Operation(summary = "Listar todo el historial de estados")
    public ResponseEntity<List<HistorialEstadoContenedorResponseDTO>> listarTodos() {
        List<HistorialEstadoContenedorResponseDTO> historial = historialService.listarTodos();
        return ResponseEntity.ok(historial);
    }

    @GetMapping("/contenedor/{contenedorId}")
    @PreAuthorize("hasAnyRole('OPERADOR', 'CLIENTE')")
    @Operation(summary = "Obtener historial de estados por contenedor")
    public ResponseEntity<List<HistorialEstadoContenedorResponseDTO>> obtenerPorContenedor(
            @PathVariable Long contenedorId) {
        List<HistorialEstadoContenedorResponseDTO> historial = historialService.obtenerHistorialPorContenedor(contenedorId);
        return ResponseEntity.ok(historial);
    }

    @GetMapping("/tramo/{tramoId}")
    @PreAuthorize("hasAnyRole('OPERADOR', 'TRANSPORTISTA')")
    @Operation(summary = "Obtener historial de estados por tramo")
    public ResponseEntity<List<HistorialEstadoContenedorResponseDTO>> obtenerPorTramo(
            @PathVariable Long tramoId) {
        List<HistorialEstadoContenedorResponseDTO> historial = historialService.obtenerHistorialPorTramo(tramoId);
        return ResponseEntity.ok(historial);
    }

    @GetMapping("/deposito/{depositoId}")
    @PreAuthorize("hasRole('OPERADOR')")
    @Operation(summary = "Obtener historial de estados por depósito")
    public ResponseEntity<List<HistorialEstadoContenedorResponseDTO>> obtenerPorDeposito(
            @PathVariable Long depositoId) {
        List<HistorialEstadoContenedorResponseDTO> historial = historialService.obtenerHistorialPorDeposito(depositoId);
        return ResponseEntity.ok(historial);
    }
}
