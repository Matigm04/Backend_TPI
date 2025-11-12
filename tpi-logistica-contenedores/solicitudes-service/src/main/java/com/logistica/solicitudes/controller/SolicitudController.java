package com.logistica.solicitudes.controller;

import com.logistica.solicitudes.dto.ActualizarCostosDTO;
import com.logistica.solicitudes.dto.SeguimientoDTO;
import com.logistica.solicitudes.dto.SolicitudRequestDTO;
import com.logistica.solicitudes.dto.SolicitudResponseDTO;
import com.logistica.solicitudes.model.EstadoSolicitud;
import com.logistica.solicitudes.service.SolicitudService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/solicitudes")
@RequiredArgsConstructor
@Tag(name = "Solicitudes", description = "API de gestión de solicitudes y contenedores")
public class SolicitudController {

    private final SolicitudService solicitudService;

    @PostMapping
    @Operation(summary = "Crear nueva solicitud", description = "Crea una nueva solicitud de transporte con su contenedor")
    public ResponseEntity<SolicitudResponseDTO> crearSolicitud(@Valid @RequestBody SolicitudRequestDTO request) {
        SolicitudResponseDTO response = solicitudService.crearSolicitud(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtener solicitud por ID")
    public ResponseEntity<SolicitudResponseDTO> obtenerPorId(@PathVariable Long id) {
        SolicitudResponseDTO response = solicitudService.obtenerPorId(id);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/numero/{numero}")
    @Operation(summary = "Obtener solicitud por número")
    public ResponseEntity<SolicitudResponseDTO> obtenerPorNumero(@PathVariable String numero) {
        SolicitudResponseDTO response = solicitudService.obtenerPorNumero(numero);
        return ResponseEntity.ok(response);
    }

    @GetMapping
    @Operation(summary = "Listar todas las solicitudes")
    public ResponseEntity<List<SolicitudResponseDTO>> listarTodas() {
        List<SolicitudResponseDTO> solicitudes = solicitudService.listarTodas();
        return ResponseEntity.ok(solicitudes);
    }

    @GetMapping("/cliente/{clienteId}")
    @Operation(summary = "Listar solicitudes por cliente")
    public ResponseEntity<List<SolicitudResponseDTO>> listarPorCliente(@PathVariable Long clienteId) {
        List<SolicitudResponseDTO> solicitudes = solicitudService.listarPorCliente(clienteId);
        return ResponseEntity.ok(solicitudes);
    }

    @GetMapping("/estado/{estado}")
    @Operation(summary = "Listar solicitudes por estado")
    public ResponseEntity<List<SolicitudResponseDTO>> listarPorEstado(@PathVariable EstadoSolicitud estado) {
        List<SolicitudResponseDTO> solicitudes = solicitudService.listarPorEstado(estado);
        return ResponseEntity.ok(solicitudes);
    }

    @PatchMapping("/{id}/estado")
    @Operation(summary = "Actualizar estado de solicitud")
    public ResponseEntity<SolicitudResponseDTO> actualizarEstado(
            @PathVariable Long id,
            @RequestParam EstadoSolicitud estado) {
        SolicitudResponseDTO response = solicitudService.actualizarEstado(id, estado);
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{id}/costos-tiempos")
    @Operation(summary = "Actualizar costos y tiempos de solicitud", 
               description = "Endpoint interno para que rutas-service actualice los costos estimados/finales y tiempos")
    public ResponseEntity<SolicitudResponseDTO> actualizarCostosYTiempos(
            @PathVariable Long id,
            @RequestBody ActualizarCostosDTO actualizacion) {
        SolicitudResponseDTO response = solicitudService.actualizarCostosYTiempos(id, actualizacion);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/seguimiento/{numero}")
    @Operation(summary = "Obtener seguimiento de contenedor", 
               description = "Permite al cliente consultar el estado actual de su contenedor")
    public ResponseEntity<SeguimientoDTO> obtenerSeguimiento(@PathVariable String numero) {
        SeguimientoDTO seguimiento = solicitudService.obtenerSeguimiento(numero);
        return ResponseEntity.ok(seguimiento);
    }
}
