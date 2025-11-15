package com.logistica.clientes.controller;

import com.logistica.clientes.dto.ClienteRequestDTO;
import com.logistica.clientes.dto.ClienteResponseDTO;
import com.logistica.clientes.service.ClienteService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/clientes")
@RequiredArgsConstructor
@Tag(name = "Clientes", description = "API para gestión de clientes")
public class ClienteController {

    private final ClienteService clienteService;

    @PostMapping
    @PreAuthorize("hasRole('OPERADOR')")
    @Operation(summary = "Crear un nuevo cliente", description = "Registra un nuevo cliente en el sistema")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Cliente creado exitosamente"),
            @ApiResponse(responseCode = "400", description = "Datos inválidos o cliente duplicado"),
            @ApiResponse(responseCode = "500", description = "Error interno del servidor")
    })
    public ResponseEntity<ClienteResponseDTO> crearCliente(
            @Valid @RequestBody ClienteRequestDTO requestDTO) {
        ClienteResponseDTO responseDTO = clienteService.crearCliente(requestDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(responseDTO);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtener cliente por ID", description = "Obtiene los datos de un cliente específico por su ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Cliente encontrado"),
            @ApiResponse(responseCode = "404", description = "Cliente no encontrado")
    })
    public ResponseEntity<ClienteResponseDTO> obtenerClientePorId(
            @Parameter(description = "ID del cliente") @PathVariable Long id) {
        ClienteResponseDTO responseDTO = clienteService.obtenerClientePorId(id);
        return ResponseEntity.ok(responseDTO);
    }

    @GetMapping("/dni/{dni}")
    @Operation(summary = "Obtener cliente por DNI", description = "Obtiene los datos de un cliente por su DNI")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Cliente encontrado"),
            @ApiResponse(responseCode = "404", description = "Cliente no encontrado")
    })
    public ResponseEntity<ClienteResponseDTO> obtenerClientePorDni(
            @Parameter(description = "DNI del cliente") @PathVariable String dni) {
        ClienteResponseDTO responseDTO = clienteService.obtenerClientePorDni(dni);
        return ResponseEntity.ok(responseDTO);
    }

    @GetMapping("/email/{email}")
    @Operation(summary = "Obtener cliente por email", description = "Obtiene los datos de un cliente por su email")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Cliente encontrado"),
            @ApiResponse(responseCode = "404", description = "Cliente no encontrado")
    })
    public ResponseEntity<ClienteResponseDTO> obtenerClientePorEmail(
            @Parameter(description = "Email del cliente") @PathVariable String email) {
        ClienteResponseDTO responseDTO = clienteService.obtenerClientePorEmail(email);
        return ResponseEntity.ok(responseDTO);
    }

    @GetMapping
    @Operation(summary = "Obtener todos los clientes", description = "Obtiene la lista completa de clientes")
    @ApiResponse(responseCode = "200", description = "Lista de clientes obtenida exitosamente")
    public ResponseEntity<List<ClienteResponseDTO>> obtenerTodosLosClientes() {
        List<ClienteResponseDTO> clientes = clienteService.obtenerTodosLosClientes();
        return ResponseEntity.ok(clientes);
    }

    @GetMapping("/activos")
    @Operation(summary = "Obtener clientes activos", description = "Obtiene la lista de clientes activos")
    @ApiResponse(responseCode = "200", description = "Lista de clientes activos obtenida exitosamente")
    public ResponseEntity<List<ClienteResponseDTO>> obtenerClientesActivos() {
        List<ClienteResponseDTO> clientes = clienteService.obtenerClientesActivos();
        return ResponseEntity.ok(clientes);
    }

    @GetMapping("/buscar")
    @Operation(summary = "Buscar clientes por nombre", description = "Busca clientes por nombre o apellido")
    @ApiResponse(responseCode = "200", description = "Búsqueda realizada exitosamente")
    public ResponseEntity<List<ClienteResponseDTO>> buscarClientesPorNombre(
            @Parameter(description = "Nombre o apellido a buscar") @RequestParam String nombre) {
        List<ClienteResponseDTO> clientes = clienteService.buscarClientesPorNombre(nombre);
        return ResponseEntity.ok(clientes);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('OPERADOR')")
    @Operation(summary = "Actualizar cliente", description = "Actualiza los datos de un cliente existente")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Cliente actualizado exitosamente"),
            @ApiResponse(responseCode = "400", description = "Datos inválidos"),
            @ApiResponse(responseCode = "404", description = "Cliente no encontrado")
    })
    public ResponseEntity<ClienteResponseDTO> actualizarCliente(
            @Parameter(description = "ID del cliente") @PathVariable Long id,
            @Valid @RequestBody ClienteRequestDTO requestDTO) {
        ClienteResponseDTO responseDTO = clienteService.actualizarCliente(id, requestDTO);
        return ResponseEntity.ok(responseDTO);
    }

    @PatchMapping("/{id}/desactivar")
    @Operation(summary = "Desactivar cliente", description = "Desactiva un cliente sin eliminarlo")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Cliente desactivado exitosamente"),
            @ApiResponse(responseCode = "404", description = "Cliente no encontrado")
    })
    public ResponseEntity<Void> desactivarCliente(
            @Parameter(description = "ID del cliente") @PathVariable Long id) {
        clienteService.desactivarCliente(id);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/activar")
    @Operation(summary = "Activar cliente", description = "Activa un cliente previamente desactivado")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Cliente activado exitosamente"),
            @ApiResponse(responseCode = "404", description = "Cliente no encontrado")
    })
    public ResponseEntity<Void> activarCliente(
            @Parameter(description = "ID del cliente") @PathVariable Long id) {
        clienteService.activarCliente(id);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('OPERADOR')")
    @Operation(summary = "Eliminar cliente", description = "Desactiva un cliente del sistema (eliminación lógica)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Cliente eliminado exitosamente"),
            @ApiResponse(responseCode = "404", description = "Cliente no encontrado"),
            @ApiResponse(responseCode = "401", description = "No autorizado")
    })
    public ResponseEntity<Void> eliminarCliente(
            @Parameter(description = "ID del cliente") @PathVariable Long id) {
        clienteService.eliminarCliente(id);
        return ResponseEntity.noContent().build();
    }
}
