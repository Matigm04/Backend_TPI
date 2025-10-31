package com.logistica.clientes.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.logistica.clientes.dto.ClienteRequestDTO;
import com.logistica.clientes.dto.ClienteResponseDTO;
import com.logistica.clientes.exception.ClienteNotFoundException;
import com.logistica.clientes.exception.DuplicateClienteException;
import com.logistica.clientes.service.ClienteService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ClienteController.class)
@DisplayName("Tests de integración de ClienteController")
class ClienteControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ClienteService clienteService;

    private ClienteRequestDTO clienteRequestDTO;
    private ClienteResponseDTO clienteResponseDTO;

    @BeforeEach
    void setUp() {
        clienteRequestDTO = ClienteRequestDTO.builder()
                .nombre("Juan")
                .apellido("Pérez")
                .dni("12345678")
                .email("juan.perez@example.com")
                .telefono("1234567890")
                .direccion("Calle Falsa 123")
                .ciudad("Buenos Aires")
                .provincia("Buenos Aires")
                .codigoPostal("1000")
                .build();

        clienteResponseDTO = ClienteResponseDTO.builder()
                .id(1L)
                .nombre("Juan")
                .apellido("Pérez")
                .dni("12345678")
                .email("juan.perez@example.com")
                .telefono("1234567890")
                .direccion("Calle Falsa 123")
                .ciudad("Buenos Aires")
                .provincia("Buenos Aires")
                .codigoPostal("1000")
                .activo(true)
                .fechaRegistro(LocalDateTime.now())
                .build();
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("POST /api/clientes - Crear cliente exitosamente")
    void crearCliente_Exitoso() throws Exception {
        // Given
        when(clienteService.crearCliente(any(ClienteRequestDTO.class))).thenReturn(clienteResponseDTO);

        // When & Then
        mockMvc.perform(post("/api/clientes")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(clienteRequestDTO)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.nombre", is("Juan")))
                .andExpect(jsonPath("$.apellido", is("Pérez")))
                .andExpect(jsonPath("$.dni", is("12345678")))
                .andExpect(jsonPath("$.email", is("juan.perez@example.com")))
                .andExpect(jsonPath("$.activo", is(true)));

        verify(clienteService).crearCliente(any(ClienteRequestDTO.class));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("POST /api/clientes - Crear cliente con DNI duplicado retorna 400")
    void crearCliente_DniDuplicado_Retorna400() throws Exception {
        // Given
        when(clienteService.crearCliente(any(ClienteRequestDTO.class)))
                .thenThrow(new DuplicateClienteException("Ya existe un cliente con el DNI: 12345678"));

        // When & Then
        mockMvc.perform(post("/api/clientes")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(clienteRequestDTO)))
                .andExpect(status().isBadRequest());

        verify(clienteService).crearCliente(any(ClienteRequestDTO.class));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("POST /api/clientes - Crear cliente con datos inválidos retorna 400")
    void crearCliente_DatosInvalidos_Retorna400() throws Exception {
        // Given
        ClienteRequestDTO invalidDTO = ClienteRequestDTO.builder()
                .nombre("") // Nombre vacío
                .apellido("Pérez")
                .dni("12345678")
                .email("email-invalido") // Email inválido
                .build();

        // When & Then
        mockMvc.perform(post("/api/clientes")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidDTO)))
                .andExpect(status().isBadRequest());

        verify(clienteService, never()).crearCliente(any(ClienteRequestDTO.class));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("GET /api/clientes/{id} - Obtener cliente por ID exitosamente")
    void obtenerClientePorId_Exitoso() throws Exception {
        // Given
        when(clienteService.obtenerClientePorId(1L)).thenReturn(clienteResponseDTO);

        // When & Then
        mockMvc.perform(get("/api/clientes/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.nombre", is("Juan")))
                .andExpect(jsonPath("$.dni", is("12345678")));

        verify(clienteService).obtenerClientePorId(1L);
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("GET /api/clientes/{id} - Cliente no encontrado retorna 404")
    void obtenerClientePorId_NoEncontrado_Retorna404() throws Exception {
        // Given
        when(clienteService.obtenerClientePorId(1L))
                .thenThrow(new ClienteNotFoundException("Cliente no encontrado con ID: 1"));

        // When & Then
        mockMvc.perform(get("/api/clientes/1"))
                .andExpect(status().isNotFound());

        verify(clienteService).obtenerClientePorId(1L);
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("GET /api/clientes - Obtener todos los clientes")
    void obtenerTodosLosClientes_Exitoso() throws Exception {
        // Given
        ClienteResponseDTO cliente2 = ClienteResponseDTO.builder()
                .id(2L)
                .nombre("María")
                .apellido("González")
                .dni("87654321")
                .email("maria.gonzalez@example.com")
                .activo(true)
                .build();

        List<ClienteResponseDTO> clientes = Arrays.asList(clienteResponseDTO, cliente2);
        when(clienteService.obtenerTodosLosClientes()).thenReturn(clientes);

        // When & Then
        mockMvc.perform(get("/api/clientes"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].nombre", is("Juan")))
                .andExpect(jsonPath("$[1].nombre", is("María")));

        verify(clienteService).obtenerTodosLosClientes();
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("GET /api/clientes/activos - Obtener clientes activos")
    void obtenerClientesActivos_Exitoso() throws Exception {
        // Given
        when(clienteService.obtenerClientesActivos()).thenReturn(Arrays.asList(clienteResponseDTO));

        // When & Then
        mockMvc.perform(get("/api/clientes/activos"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].activo", is(true)));

        verify(clienteService).obtenerClientesActivos();
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("GET /api/clientes/buscar - Buscar clientes por nombre")
    void buscarClientesPorNombre_Exitoso() throws Exception {
        // Given
        when(clienteService.buscarClientesPorNombre("Juan")).thenReturn(Arrays.asList(clienteResponseDTO));

        // When & Then
        mockMvc.perform(get("/api/clientes/buscar")
                        .param("nombre", "Juan"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].nombre", is("Juan")));

        verify(clienteService).buscarClientesPorNombre("Juan");
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("PUT /api/clientes/{id} - Actualizar cliente exitosamente")
    void actualizarCliente_Exitoso() throws Exception {
        // Given
        when(clienteService.actualizarCliente(eq(1L), any(ClienteRequestDTO.class)))
                .thenReturn(clienteResponseDTO);

        // When & Then
        mockMvc.perform(put("/api/clientes/1")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(clienteRequestDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.nombre", is("Juan")));

        verify(clienteService).actualizarCliente(eq(1L), any(ClienteRequestDTO.class));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("PATCH /api/clientes/{id}/desactivar - Desactivar cliente exitosamente")
    void desactivarCliente_Exitoso() throws Exception {
        // Given
        doNothing().when(clienteService).desactivarCliente(1L);

        // When & Then
        mockMvc.perform(patch("/api/clientes/1/desactivar")
                        .with(csrf()))
                .andExpect(status().isNoContent());

        verify(clienteService).desactivarCliente(1L);
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("PATCH /api/clientes/{id}/activar - Activar cliente exitosamente")
    void activarCliente_Exitoso() throws Exception {
        // Given
        doNothing().when(clienteService).activarCliente(1L);

        // When & Then
        mockMvc.perform(patch("/api/clientes/1/activar")
                        .with(csrf()))
                .andExpect(status().isNoContent());

        verify(clienteService).activarCliente(1L);
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("DELETE /api/clientes/{id} - Eliminar cliente exitosamente")
    void eliminarCliente_Exitoso() throws Exception {
        // Given
        doNothing().when(clienteService).eliminarCliente(1L);

        // When & Then
        mockMvc.perform(delete("/api/clientes/1")
                        .with(csrf()))
                .andExpect(status().isNoContent());

        verify(clienteService).eliminarCliente(1L);
    }
}
