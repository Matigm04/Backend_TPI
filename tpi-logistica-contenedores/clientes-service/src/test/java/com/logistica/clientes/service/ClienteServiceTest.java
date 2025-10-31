package com.logistica.clientes.service;

import com.logistica.clientes.dto.ClienteRequestDTO;
import com.logistica.clientes.dto.ClienteResponseDTO;
import com.logistica.clientes.exception.ClienteNotFoundException;
import com.logistica.clientes.exception.DuplicateClienteException;
import com.logistica.clientes.model.Cliente;
import com.logistica.clientes.repository.ClienteRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Tests unitarios de ClienteService")
class ClienteServiceTest {

    @Mock
    private ClienteRepository clienteRepository;

    @InjectMocks
    private ClienteService clienteService;

    private ClienteRequestDTO clienteRequestDTO;
    private Cliente cliente;

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

        cliente = Cliente.builder()
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
    @DisplayName("Crear cliente exitosamente")
    void crearCliente_Exitoso() {
        // Given
        when(clienteRepository.existsByDni(anyString())).thenReturn(false);
        when(clienteRepository.existsByEmail(anyString())).thenReturn(false);
        when(clienteRepository.save(any(Cliente.class))).thenReturn(cliente);

        // When
        ClienteResponseDTO resultado = clienteService.crearCliente(clienteRequestDTO);

        // Then
        assertThat(resultado).isNotNull();
        assertThat(resultado.getNombre()).isEqualTo("Juan");
        assertThat(resultado.getApellido()).isEqualTo("Pérez");
        assertThat(resultado.getDni()).isEqualTo("12345678");
        assertThat(resultado.getEmail()).isEqualTo("juan.perez@example.com");
        assertThat(resultado.getActivo()).isTrue();

        verify(clienteRepository).existsByDni("12345678");
        verify(clienteRepository).existsByEmail("juan.perez@example.com");
        verify(clienteRepository).save(any(Cliente.class));
    }

    @Test
    @DisplayName("Crear cliente con DNI duplicado lanza excepción")
    void crearCliente_DniDuplicado_LanzaExcepcion() {
        // Given
        when(clienteRepository.existsByDni(anyString())).thenReturn(true);

        // When & Then
        assertThatThrownBy(() -> clienteService.crearCliente(clienteRequestDTO))
                .isInstanceOf(DuplicateClienteException.class)
                .hasMessageContaining("Ya existe un cliente con el DNI");

        verify(clienteRepository).existsByDni("12345678");
        verify(clienteRepository, never()).save(any(Cliente.class));
    }

    @Test
    @DisplayName("Crear cliente con email duplicado lanza excepción")
    void crearCliente_EmailDuplicado_LanzaExcepcion() {
        // Given
        when(clienteRepository.existsByDni(anyString())).thenReturn(false);
        when(clienteRepository.existsByEmail(anyString())).thenReturn(true);

        // When & Then
        assertThatThrownBy(() -> clienteService.crearCliente(clienteRequestDTO))
                .isInstanceOf(DuplicateClienteException.class)
                .hasMessageContaining("Ya existe un cliente con el email");

        verify(clienteRepository).existsByDni("12345678");
        verify(clienteRepository).existsByEmail("juan.perez@example.com");
        verify(clienteRepository, never()).save(any(Cliente.class));
    }

    @Test
    @DisplayName("Obtener cliente por ID exitosamente")
    void obtenerClientePorId_Exitoso() {
        // Given
        when(clienteRepository.findById(1L)).thenReturn(Optional.of(cliente));

        // When
        ClienteResponseDTO resultado = clienteService.obtenerClientePorId(1L);

        // Then
        assertThat(resultado).isNotNull();
        assertThat(resultado.getId()).isEqualTo(1L);
        assertThat(resultado.getNombre()).isEqualTo("Juan");

        verify(clienteRepository).findById(1L);
    }

    @Test
    @DisplayName("Obtener cliente por ID inexistente lanza excepción")
    void obtenerClientePorId_NoExiste_LanzaExcepcion() {
        // Given
        when(clienteRepository.findById(1L)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> clienteService.obtenerClientePorId(1L))
                .isInstanceOf(ClienteNotFoundException.class)
                .hasMessageContaining("Cliente no encontrado con ID");

        verify(clienteRepository).findById(1L);
    }

    @Test
    @DisplayName("Obtener cliente por DNI exitosamente")
    void obtenerClientePorDni_Exitoso() {
        // Given
        when(clienteRepository.findByDni("12345678")).thenReturn(Optional.of(cliente));

        // When
        ClienteResponseDTO resultado = clienteService.obtenerClientePorDni("12345678");

        // Then
        assertThat(resultado).isNotNull();
        assertThat(resultado.getDni()).isEqualTo("12345678");

        verify(clienteRepository).findByDni("12345678");
    }

    @Test
    @DisplayName("Obtener todos los clientes")
    void obtenerTodosLosClientes_Exitoso() {
        // Given
        Cliente cliente2 = Cliente.builder()
                .id(2L)
                .nombre("María")
                .apellido("González")
                .dni("87654321")
                .email("maria.gonzalez@example.com")
                .activo(true)
                .build();

        when(clienteRepository.findAll()).thenReturn(Arrays.asList(cliente, cliente2));

        // When
        List<ClienteResponseDTO> resultado = clienteService.obtenerTodosLosClientes();

        // Then
        assertThat(resultado).hasSize(2);
        assertThat(resultado.get(0).getNombre()).isEqualTo("Juan");
        assertThat(resultado.get(1).getNombre()).isEqualTo("María");

        verify(clienteRepository).findAll();
    }

    @Test
    @DisplayName("Obtener clientes activos")
    void obtenerClientesActivos_Exitoso() {
        // Given
        when(clienteRepository.findByActivo(true)).thenReturn(Arrays.asList(cliente));

        // When
        List<ClienteResponseDTO> resultado = clienteService.obtenerClientesActivos();

        // Then
        assertThat(resultado).hasSize(1);
        assertThat(resultado.get(0).getActivo()).isTrue();

        verify(clienteRepository).findByActivo(true);
    }

    @Test
    @DisplayName("Buscar clientes por nombre")
    void buscarClientesPorNombre_Exitoso() {
        // Given
        when(clienteRepository.findByNombreContainingIgnoreCaseOrApellidoContainingIgnoreCase("Juan", "Juan"))
                .thenReturn(Arrays.asList(cliente));

        // When
        List<ClienteResponseDTO> resultado = clienteService.buscarClientesPorNombre("Juan");

        // Then
        assertThat(resultado).hasSize(1);
        assertThat(resultado.get(0).getNombre()).isEqualTo("Juan");

        verify(clienteRepository).findByNombreContainingIgnoreCaseOrApellidoContainingIgnoreCase("Juan", "Juan");
    }

    @Test
    @DisplayName("Actualizar cliente exitosamente")
    void actualizarCliente_Exitoso() {
        // Given
        when(clienteRepository.findById(1L)).thenReturn(Optional.of(cliente));
        when(clienteRepository.save(any(Cliente.class))).thenReturn(cliente);

        ClienteRequestDTO updateDTO = ClienteRequestDTO.builder()
                .nombre("Juan Carlos")
                .apellido("Pérez")
                .dni("12345678")
                .email("juan.perez@example.com")
                .telefono("9876543210")
                .direccion("Nueva Dirección 456")
                .ciudad("Buenos Aires")
                .provincia("Buenos Aires")
                .codigoPostal("1000")
                .build();

        // When
        ClienteResponseDTO resultado = clienteService.actualizarCliente(1L, updateDTO);

        // Then
        assertThat(resultado).isNotNull();
        verify(clienteRepository).findById(1L);
        verify(clienteRepository).save(any(Cliente.class));
    }

    @Test
    @DisplayName("Actualizar cliente inexistente lanza excepción")
    void actualizarCliente_NoExiste_LanzaExcepcion() {
        // Given
        when(clienteRepository.findById(1L)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> clienteService.actualizarCliente(1L, clienteRequestDTO))
                .isInstanceOf(ClienteNotFoundException.class)
                .hasMessageContaining("Cliente no encontrado con ID");

        verify(clienteRepository).findById(1L);
        verify(clienteRepository, never()).save(any(Cliente.class));
    }

    @Test
    @DisplayName("Desactivar cliente exitosamente")
    void desactivarCliente_Exitoso() {
        // Given
        when(clienteRepository.findById(1L)).thenReturn(Optional.of(cliente));
        when(clienteRepository.save(any(Cliente.class))).thenReturn(cliente);

        // When
        clienteService.desactivarCliente(1L);

        // Then
        verify(clienteRepository).findById(1L);
        verify(clienteRepository).save(any(Cliente.class));
    }

    @Test
    @DisplayName("Activar cliente exitosamente")
    void activarCliente_Exitoso() {
        // Given
        cliente.setActivo(false);
        when(clienteRepository.findById(1L)).thenReturn(Optional.of(cliente));
        when(clienteRepository.save(any(Cliente.class))).thenReturn(cliente);

        // When
        clienteService.activarCliente(1L);

        // Then
        verify(clienteRepository).findById(1L);
        verify(clienteRepository).save(any(Cliente.class));
    }

    @Test
    @DisplayName("Eliminar cliente exitosamente")
    void eliminarCliente_Exitoso() {
        // Given
        when(clienteRepository.existsById(1L)).thenReturn(true);
        doNothing().when(clienteRepository).deleteById(1L);

        // When
        clienteService.eliminarCliente(1L);

        // Then
        verify(clienteRepository).existsById(1L);
        verify(clienteRepository).deleteById(1L);
    }

    @Test
    @DisplayName("Eliminar cliente inexistente lanza excepción")
    void eliminarCliente_NoExiste_LanzaExcepcion() {
        // Given
        when(clienteRepository.existsById(1L)).thenReturn(false);

        // When & Then
        assertThatThrownBy(() -> clienteService.eliminarCliente(1L))
                .isInstanceOf(ClienteNotFoundException.class)
                .hasMessageContaining("Cliente no encontrado con ID");

        verify(clienteRepository).existsById(1L);
        verify(clienteRepository, never()).deleteById(1L);
    }
}
