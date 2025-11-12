package com.logistica.camiones.controller;

import com.logistica.camiones.dto.TransportistaDTO;
import com.logistica.camiones.service.TransportistaService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/transportistas")
public class TransportistaController {

    @Autowired
    private TransportistaService transportistaService;

    @GetMapping
    @PreAuthorize("hasAnyRole('OPERADOR', 'TRANSPORTISTA')")
    public ResponseEntity<List<TransportistaDTO>> listarTodos() {
        List<TransportistaDTO> transportistas = transportistaService.listarTodos();
        return ResponseEntity.ok(transportistas);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('OPERADOR', 'TRANSPORTISTA')")
    public ResponseEntity<TransportistaDTO> obtenerPorId(@PathVariable Long id) {
        TransportistaDTO transportista = transportistaService.obtenerPorId(id);
        return ResponseEntity.ok(transportista);
    }

    @GetMapping("/buscar")
    @PreAuthorize("hasAnyRole('OPERADOR', 'TRANSPORTISTA')")
    public ResponseEntity<List<TransportistaDTO>> buscarPorNombre(@RequestParam String nombre) {
        List<TransportistaDTO> transportistas = transportistaService.buscarPorNombre(nombre);
        return ResponseEntity.ok(transportistas);
    }

    @PostMapping
    @PreAuthorize("hasRole('OPERADOR')")
    public ResponseEntity<TransportistaDTO> crear(@Valid @RequestBody TransportistaDTO transportistaDTO) {
        TransportistaDTO nuevoTransportista = transportistaService.crear(transportistaDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(nuevoTransportista);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('OPERADOR')")
    public ResponseEntity<TransportistaDTO> actualizar(
            @PathVariable Long id,
            @Valid @RequestBody TransportistaDTO transportistaDTO) {
        TransportistaDTO actualizado = transportistaService.actualizar(id, transportistaDTO);
        return ResponseEntity.ok(actualizado);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('OPERADOR')")
    public ResponseEntity<Void> desactivar(@PathVariable Long id) {
        transportistaService.desactivar(id);
        return ResponseEntity.noContent().build();
    }
}
