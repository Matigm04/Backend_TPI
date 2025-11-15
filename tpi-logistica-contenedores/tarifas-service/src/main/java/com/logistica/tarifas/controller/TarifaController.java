package com.logistica.tarifas.controller;

import com.logistica.tarifas.dto.TarifaRequestDTO;
import com.logistica.tarifas.dto.TarifaResponseDTO;
import com.logistica.tarifas.model.TipoTarifa;
import com.logistica.tarifas.service.TarifaService;
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
@RequestMapping("/api/tarifas")
@RequiredArgsConstructor
@Tag(name = "Tarifas", description = "API de gestión de tarifas y configuraciones")
@SecurityRequirement(name = "bearer-jwt")
public class TarifaController {
    
    private final TarifaService tarifaService;
    
    @PostMapping
    @PreAuthorize("hasRole('OPERADOR')")
    @Operation(summary = "Crear una nueva tarifa", description = "Registra una nueva tarifa en el sistema")
    public ResponseEntity<TarifaResponseDTO> crearTarifa(@Valid @RequestBody TarifaRequestDTO request) {
        TarifaResponseDTO response = tarifaService.crearTarifa(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
    
    @GetMapping
    @PreAuthorize("hasRole('OPERADOR')")
    @Operation(summary = "Listar todas las tarifas", description = "Obtiene la lista de todas las tarifas activas")
    public ResponseEntity<List<TarifaResponseDTO>> listarTodas() {
        List<TarifaResponseDTO> tarifas = tarifaService.listarTodas();
        return ResponseEntity.ok(tarifas);
    }
    
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('OPERADOR')")
    @Operation(summary = "Obtener tarifa por ID", description = "Obtiene los detalles de una tarifa específica")
    public ResponseEntity<TarifaResponseDTO> obtenerPorId(@PathVariable Long id) {
        TarifaResponseDTO tarifa = tarifaService.obtenerPorId(id);
        return ResponseEntity.ok(tarifa);
    }
    
    @GetMapping("/tipo/{tipo}")
    @PreAuthorize("hasRole('OPERADOR')")
    @Operation(summary = "Listar tarifas por tipo", description = "Obtiene todas las tarifas de un tipo específico")
    public ResponseEntity<List<TarifaResponseDTO>> listarPorTipo(@PathVariable TipoTarifa tipo) {
        List<TarifaResponseDTO> tarifas = tarifaService.listarPorTipo(tipo);
        return ResponseEntity.ok(tarifas);
    }
    
    @GetMapping("/vigente/{tipo}")
    @PreAuthorize("hasAnyRole('OPERADOR', 'CLIENTE')")
    @Operation(summary = "Obtener tarifa vigente por tipo", description = "Obtiene la tarifa actualmente vigente para un tipo específico")
    public ResponseEntity<TarifaResponseDTO> obtenerVigentePorTipo(@PathVariable TipoTarifa tipo) {
        TarifaResponseDTO tarifa = tarifaService.obtenerVigentePorTipo(tipo);
        return ResponseEntity.ok(tarifa);
    }
    
    @GetMapping("/vigentes")
    @PreAuthorize("hasAnyRole('OPERADOR', 'CLIENTE')")
    @Operation(summary = "Listar tarifas vigentes", description = "Obtiene todas las tarifas actualmente vigentes")
    public ResponseEntity<List<TarifaResponseDTO>> listarVigentes() {
        List<TarifaResponseDTO> tarifas = tarifaService.listarVigentes();
        return ResponseEntity.ok(tarifas);
    }
    
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('OPERADOR')")
    @Operation(summary = "Actualizar tarifa", description = "Actualiza los datos de una tarifa existente")
    public ResponseEntity<TarifaResponseDTO> actualizarTarifa(
            @PathVariable Long id,
            @Valid @RequestBody TarifaRequestDTO request) {
        TarifaResponseDTO response = tarifaService.actualizarTarifa(id, request);
        return ResponseEntity.ok(response);
    }
    
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('OPERADOR')")
    @Operation(summary = "Eliminar tarifa", description = "Desactiva una tarifa del sistema")
    public ResponseEntity<Void> eliminarTarifa(@PathVariable Long id) {
        tarifaService.eliminarTarifa(id);
        return ResponseEntity.noContent().build();
    }
}
