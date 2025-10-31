package com.logistica.rutas.controller;

import com.logistica.rutas.dto.AsignarCamionDTO;
import com.logistica.rutas.dto.RutaRequestDTO;
import com.logistica.rutas.dto.RutaResponseDTO;
import com.logistica.rutas.dto.TramoResponseDTO;
import com.logistica.rutas.service.RutaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/rutas")
@RequiredArgsConstructor
@Tag(name = "Rutas", description = "API de gestión de rutas y tramos")
public class RutaController {

    private final RutaService rutaService;

    @PostMapping("/calcular")
    @Operation(summary = "Calcular ruta tentativa", 
               description = "Calcula una ruta tentativa con todos los tramos y costos estimados")
    public ResponseEntity<RutaResponseDTO> calcularRutaTentativa(@Valid @RequestBody RutaRequestDTO request) {
        RutaResponseDTO response = rutaService.calcularRutaTentativa(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtener ruta por ID")
    public ResponseEntity<RutaResponseDTO> obtenerPorId(@PathVariable Long id) {
        RutaResponseDTO response = rutaService.obtenerPorId(id);
        return ResponseEntity.ok(response);
    }

    @GetMapping
    @Operation(summary = "Listar todas las rutas")
    public ResponseEntity<List<RutaResponseDTO>> listarTodas() {
        List<RutaResponseDTO> rutas = rutaService.listarTodas();
        return ResponseEntity.ok(rutas);
    }

    @PostMapping("/tramos/{tramoId}/asignar-camion")
    @Operation(summary = "Asignar camión a tramo")
    public ResponseEntity<RutaResponseDTO> asignarCamionATramo(
            @PathVariable Long tramoId,
            @Valid @RequestBody AsignarCamionDTO request) {
        RutaResponseDTO response = rutaService.asignarCamionATramo(tramoId, request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/tramos/{tramoId}/iniciar")
    @Operation(summary = "Iniciar tramo", description = "Registra el inicio de un tramo por parte del transportista")
    public ResponseEntity<TramoResponseDTO> iniciarTramo(@PathVariable Long tramoId) {
        TramoResponseDTO response = rutaService.iniciarTramo(tramoId);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/tramos/{tramoId}/finalizar")
    @Operation(summary = "Finalizar tramo", description = "Registra la finalización de un tramo")
    public ResponseEntity<TramoResponseDTO> finalizarTramo(@PathVariable Long tramoId) {
        TramoResponseDTO response = rutaService.finalizarTramo(tramoId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/camion/{camionId}/tramos")
    @Operation(summary = "Listar tramos por camión")
    public ResponseEntity<List<TramoResponseDTO>> listarTramosPorCamion(@PathVariable Long camionId) {
        List<TramoResponseDTO> tramos = rutaService.listarTramosPorCamion(camionId);
        return ResponseEntity.ok(tramos);
    }
}
