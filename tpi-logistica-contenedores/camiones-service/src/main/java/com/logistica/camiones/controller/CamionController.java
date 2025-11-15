package com.logistica.camiones.controller;

import com.logistica.camiones.dto.CamionDisponibleDTO;
import com.logistica.camiones.dto.CamionRequestDTO;
import com.logistica.camiones.dto.CamionResponseDTO;
import com.logistica.camiones.service.CamionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/camiones")
@RequiredArgsConstructor
@Tag(name = "Camiones", description = "API de gestión de camiones y transportistas")
public class CamionController {
    
    private final CamionService camionService;
    
    @PostMapping
    @PreAuthorize("hasRole('OPERADOR')")
    @Operation(summary = "Crear un nuevo camión", description = "Registra un nuevo camión en el sistema")
    public ResponseEntity<CamionResponseDTO> crearCamion(@Valid @RequestBody CamionRequestDTO request) {
        CamionResponseDTO response = camionService.crearCamion(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
    
    @GetMapping
    @Operation(summary = "Listar todos los camiones", description = "Obtiene la lista de todos los camiones activos")
    public ResponseEntity<List<CamionResponseDTO>> listarTodos() {
        List<CamionResponseDTO> camiones = camionService.listarTodos();
        return ResponseEntity.ok(camiones);
    }
    
    @GetMapping("/{id}")
    @Operation(summary = "Obtener camión por ID", description = "Obtiene los detalles de un camión específico")
    public ResponseEntity<CamionResponseDTO> obtenerPorId(@PathVariable Long id) {
        CamionResponseDTO camion = camionService.obtenerPorId(id);
        return ResponseEntity.ok(camion);
    }
    
    @GetMapping("/disponibles")
    @Operation(summary = "Listar camiones disponibles", description = "Obtiene la lista de camiones disponibles para asignación")
    public ResponseEntity<List<CamionDisponibleDTO>> listarDisponibles() {
        List<CamionDisponibleDTO> camiones = camionService.listarDisponibles();
        return ResponseEntity.ok(camiones);
    }
    
    @GetMapping("/disponibles/capacidad")
    @Operation(summary = "Buscar camiones por capacidad", description = "Busca camiones disponibles que cumplan con los requisitos de peso y volumen")
    public ResponseEntity<List<CamionDisponibleDTO>> buscarPorCapacidad(
            @RequestParam BigDecimal peso,
            @RequestParam BigDecimal volumen) {
        List<CamionDisponibleDTO> camiones = camionService.buscarDisponiblesConCapacidad(peso, volumen);
        return ResponseEntity.ok(camiones);
    }
    
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('OPERADOR')")
    @Operation(summary = "Actualizar camión", description = "Actualiza los datos de un camión existente")
    public ResponseEntity<CamionResponseDTO> actualizarCamion(
            @PathVariable Long id,
            @Valid @RequestBody CamionRequestDTO request) {
        CamionResponseDTO response = camionService.actualizarCamion(id, request);
        return ResponseEntity.ok(response);
    }
    
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('OPERADOR')")
    @Operation(summary = "Eliminar camión", description = "Desactiva un camión del sistema")
    public ResponseEntity<Void> eliminarCamion(@PathVariable Long id) {
        camionService.eliminarCamion(id);
        return ResponseEntity.noContent().build();
    }
    
    @PatchMapping("/{id}/asignar")
    @Operation(summary = "Asignar camión", description = "Marca un camión como no disponible (asignado a un tramo)")
    public ResponseEntity<Void> asignarCamion(@PathVariable Long id) {
        camionService.asignarCamion(id);
        return ResponseEntity.ok().build();
    }
    
    @PatchMapping("/{id}/liberar")
    @Operation(summary = "Liberar camión", description = "Marca un camión como disponible nuevamente")
    public ResponseEntity<Void> liberarCamion(@PathVariable Long id) {
        camionService.liberarCamion(id);
        return ResponseEntity.ok().build();
    }
}
