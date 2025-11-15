package com.logistica.clientes.service;

import com.logistica.clientes.dto.ClienteRequestDTO;
import com.logistica.clientes.dto.ClienteResponseDTO;
import com.logistica.clientes.exception.ClienteNotFoundException;
import com.logistica.clientes.exception.DuplicateClienteException;
import com.logistica.clientes.model.Cliente;
import com.logistica.clientes.repository.ClienteRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ClienteService {

    private final ClienteRepository clienteRepository;

    @Transactional
    public ClienteResponseDTO crearCliente(ClienteRequestDTO requestDTO) {
        log.info("Creando nuevo cliente con DNI: {}", requestDTO.getDni());

        // Validar que no exista un cliente con el mismo DNI
        if (clienteRepository.existsByDni(requestDTO.getDni())) {
            log.error("Ya existe un cliente con DNI: {}", requestDTO.getDni());
            throw new DuplicateClienteException("Ya existe un cliente con el DNI: " + requestDTO.getDni());
        }

        // Validar que no exista un cliente con el mismo email
        if (clienteRepository.existsByEmail(requestDTO.getEmail())) {
            log.error("Ya existe un cliente con email: {}", requestDTO.getEmail());
            throw new DuplicateClienteException("Ya existe un cliente con el email: " + requestDTO.getEmail());
        }

        Cliente cliente = Cliente.builder()
                .nombre(requestDTO.getNombre())
                .apellido(requestDTO.getApellido())
                .dni(requestDTO.getDni())
                .email(requestDTO.getEmail())
                .telefono(requestDTO.getTelefono())
                .direccion(requestDTO.getDireccion())
                .ciudad(requestDTO.getCiudad())
                .provincia(requestDTO.getProvincia())
                .codigoPostal(requestDTO.getCodigoPostal())
                .activo(true)
                .build();

        Cliente clienteGuardado = clienteRepository.save(cliente);
        log.info("Cliente creado exitosamente con ID: {}", clienteGuardado.getId());

        return mapToResponseDTO(clienteGuardado);
    }

    @Transactional(readOnly = true)
    public ClienteResponseDTO obtenerClientePorId(Long id) {
        log.info("Buscando cliente con ID: {}", id);
        Cliente cliente = clienteRepository.findById(id)
                .orElseThrow(() -> new ClienteNotFoundException("Cliente no encontrado con ID: " + id));
        return mapToResponseDTO(cliente);
    }

    @Transactional(readOnly = true)
    public ClienteResponseDTO obtenerClientePorDni(String dni) {
        log.info("Buscando cliente con DNI: {}", dni);
        Cliente cliente = clienteRepository.findByDni(dni)
                .orElseThrow(() -> new ClienteNotFoundException("Cliente no encontrado con DNI: " + dni));
        return mapToResponseDTO(cliente);
    }

    @Transactional(readOnly = true)
    public ClienteResponseDTO obtenerClientePorEmail(String email) {
        log.info("Buscando cliente con email: {}", email);
        Cliente cliente = clienteRepository.findByEmail(email)
                .orElseThrow(() -> new ClienteNotFoundException("Cliente no encontrado con email: " + email));
        return mapToResponseDTO(cliente);
    }

    @Transactional(readOnly = true)
    public List<ClienteResponseDTO> obtenerTodosLosClientes() {
        log.info("Obteniendo todos los clientes activos");
        return clienteRepository.findByActivo(true).stream()
                .map(this::mapToResponseDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<ClienteResponseDTO> obtenerClientesActivos() {
        log.info("Obteniendo clientes activos");
        return clienteRepository.findByActivo(true).stream()
                .map(this::mapToResponseDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<ClienteResponseDTO> buscarClientesPorNombre(String nombre) {
        log.info("Buscando clientes con nombre: {}", nombre);
        return clienteRepository.findByNombreContainingIgnoreCaseOrApellidoContainingIgnoreCase(nombre, nombre)
                .stream()
                .map(this::mapToResponseDTO)
                .collect(Collectors.toList());
    }

    @Transactional
    public ClienteResponseDTO actualizarCliente(Long id, ClienteRequestDTO requestDTO) {
        log.info("Actualizando cliente con ID: {}", id);

        Cliente cliente = clienteRepository.findById(id)
                .orElseThrow(() -> new ClienteNotFoundException("Cliente no encontrado con ID: " + id));

        // Validar DNI si cambió
        if (!cliente.getDni().equals(requestDTO.getDni()) && 
            clienteRepository.existsByDni(requestDTO.getDni())) {
            throw new DuplicateClienteException("Ya existe un cliente con el DNI: " + requestDTO.getDni());
        }

        // Validar email si cambió
        if (!cliente.getEmail().equals(requestDTO.getEmail()) && 
            clienteRepository.existsByEmail(requestDTO.getEmail())) {
            throw new DuplicateClienteException("Ya existe un cliente con el email: " + requestDTO.getEmail());
        }

        cliente.setNombre(requestDTO.getNombre());
        cliente.setApellido(requestDTO.getApellido());
        cliente.setDni(requestDTO.getDni());
        cliente.setEmail(requestDTO.getEmail());
        cliente.setTelefono(requestDTO.getTelefono());
        cliente.setDireccion(requestDTO.getDireccion());
        cliente.setCiudad(requestDTO.getCiudad());
        cliente.setProvincia(requestDTO.getProvincia());
        cliente.setCodigoPostal(requestDTO.getCodigoPostal());

        Cliente clienteActualizado = clienteRepository.save(cliente);
        log.info("Cliente actualizado exitosamente con ID: {}", clienteActualizado.getId());

        return mapToResponseDTO(clienteActualizado);
    }

    @Transactional
    public void desactivarCliente(Long id) {
        log.info("Desactivando cliente con ID: {}", id);

        Cliente cliente = clienteRepository.findById(id)
                .orElseThrow(() -> new ClienteNotFoundException("Cliente no encontrado con ID: " + id));

        cliente.setActivo(false);
        clienteRepository.save(cliente);

        log.info("Cliente desactivado exitosamente con ID: {}", id);
    }

    @Transactional
    public void activarCliente(Long id) {
        log.info("Activando cliente con ID: {}", id);

        Cliente cliente = clienteRepository.findById(id)
                .orElseThrow(() -> new ClienteNotFoundException("Cliente no encontrado con ID: " + id));

        cliente.setActivo(true);
        clienteRepository.save(cliente);

        log.info("Cliente activado exitosamente con ID: {}", id);
    }

    @Transactional
    public void eliminarCliente(Long id) {
        log.info("Eliminando cliente con ID: {}", id);

        Cliente cliente = clienteRepository.findById(id)
                .orElseThrow(() -> new ClienteNotFoundException("Cliente no encontrado con ID: " + id));

        cliente.setActivo(false);
        clienteRepository.save(cliente);
        
        log.info("Cliente eliminado (desactivado) exitosamente con ID: {}", id);
    }

    private ClienteResponseDTO mapToResponseDTO(Cliente cliente) {
        return ClienteResponseDTO.builder()
                .id(cliente.getId())
                .nombre(cliente.getNombre())
                .apellido(cliente.getApellido())
                .dni(cliente.getDni())
                .email(cliente.getEmail())
                .telefono(cliente.getTelefono())
                .direccion(cliente.getDireccion())
                .ciudad(cliente.getCiudad())
                .provincia(cliente.getProvincia())
                .codigoPostal(cliente.getCodigoPostal())
                .activo(cliente.getActivo())
                .fechaRegistro(cliente.getFechaRegistro())
                .fechaActualizacion(cliente.getFechaActualizacion())
                .build();
    }
}
