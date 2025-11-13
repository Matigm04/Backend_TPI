package com.logistica.depositos.controller;

import com.logistica.depositos.dto.DepositoCercanoDTO;
import com.logistica.depositos.dto.DepositoRequestDTO;
import com.logistica.depositos.dto.DepositoResponseDTO;
import com.logistica.depositos.service.DepositoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
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
@RequestMapping("/api/depositos")
@RequiredArgsConstructor
@Tag(name = "Depósitos", description = "API para gestión de depósitos de contenedores")
@SecurityRequirement(name = "bearer-jwt")
public class DepositoController {

    private final DepositoService depositoService;

    @PostMapping
    @PreAuthorize("hasRole('OPERADOR')")
    @Operation(summary = "Crear nuevo depósito", description = "Crea un nuevo depósito en el sistema")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Depósito creado exitosamente"),
        @ApiResponse(responseCode = "400", description = "Datos inválidos"),
        @ApiResponse(responseCode = "401", description = "No autorizado")
    })
    public ResponseEntity<DepositoResponseDTO> crearDeposito(
            @Valid @RequestBody DepositoRequestDTO requestDTO) {
        DepositoResponseDTO response = depositoService.crearDeposito(requestDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('OPERADOR', 'TRANSPORTISTA')")
    @Operation(summary = "Listar todos los depósitos", description = "Obtiene la lista completa de depósitos")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Lista obtenida exitosamente"),
        @ApiResponse(responseCode = "401", description = "No autorizado")
    })
    public ResponseEntity<List<DepositoResponseDTO>> listarTodos(
            @Parameter(description = "Filtrar solo depósitos activos")
            @RequestParam(required = false, defaultValue = "false") Boolean soloActivos) {
        List<DepositoResponseDTO> depositos = soloActivos 
                ? depositoService.listarActivos() 
                : depositoService.listarTodos();
        return ResponseEntity.ok(depositos);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('OPERADOR', 'TRANSPORTISTA')")
    @Operation(summary = "Obtener depósito por ID", description = "Obtiene la información de un depósito específico")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Depósito encontrado"),
        @ApiResponse(responseCode = "404", description = "Depósito no encontrado"),
        @ApiResponse(responseCode = "401", description = "No autorizado")
    })
    public ResponseEntity<DepositoResponseDTO> obtenerPorId(
            @Parameter(description = "ID del depósito", required = true)
            @PathVariable Long id) {
        DepositoResponseDTO response = depositoService.obtenerPorId(id);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('OPERADOR')")
    @Operation(summary = "Actualizar depósito", description = "Actualiza la información de un depósito existente")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Depósito actualizado exitosamente"),
        @ApiResponse(responseCode = "404", description = "Depósito no encontrado"),
        @ApiResponse(responseCode = "400", description = "Datos inválidos"),
        @ApiResponse(responseCode = "401", description = "No autorizado")
    })
    public ResponseEntity<DepositoResponseDTO> actualizarDeposito(
            @Parameter(description = "ID del depósito", required = true)
            @PathVariable Long id,
            @Valid @RequestBody DepositoRequestDTO requestDTO) {
        DepositoResponseDTO response = depositoService.actualizarDeposito(id, requestDTO);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('OPERADOR')")
    @Operation(summary = "Eliminar depósito", description = "Desactiva un depósito (eliminación lógica)")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "204", description = "Depósito eliminado exitosamente"),
        @ApiResponse(responseCode = "404", description = "Depósito no encontrado"),
        @ApiResponse(responseCode = "401", description = "No autorizado")
    })
    public ResponseEntity<Void> eliminarDeposito(
            @Parameter(description = "ID del depósito", required = true)
            @PathVariable Long id) {
        depositoService.eliminarDeposito(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/cercanos")
    @PreAuthorize("hasAnyRole('OPERADOR', 'TRANSPORTISTA')")
    @Operation(summary = "Buscar depósitos cercanos", 
               description = "Busca depósitos activos dentro de un radio específico desde una ubicación")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Búsqueda exitosa"),
        @ApiResponse(responseCode = "400", description = "Parámetros inválidos"),
        @ApiResponse(responseCode = "401", description = "No autorizado")
    })
    public ResponseEntity<List<DepositoCercanoDTO>> buscarDepositosCercanos(
            @Parameter(description = "Latitud del punto de origen", required = true, example = "-34.603722")
            @RequestParam Double latitud,
            @Parameter(description = "Longitud del punto de origen", required = true, example = "-58.381592")
            @RequestParam Double longitud,
            @Parameter(description = "Radio de búsqueda en kilómetros", required = true, example = "50")
            @RequestParam Double radioKm) {
        List<DepositoCercanoDTO> depositos = depositoService.buscarDepositosCercanos(latitud, longitud, radioKm);
        return ResponseEntity.ok(depositos);
    }
}
