package com.logistica.solicitudes.service;

import com.logistica.solicitudes.dto.*;
import com.logistica.solicitudes.exception.ClienteNotFoundException;
import com.logistica.solicitudes.exception.ContenedorDuplicadoException;
import com.logistica.solicitudes.exception.SolicitudNotFoundException;
import com.logistica.solicitudes.model.Contenedor;
import com.logistica.solicitudes.model.EstadoSolicitud;
import com.logistica.solicitudes.model.Solicitud;
import com.logistica.solicitudes.repository.ContenedorRepository;
import com.logistica.solicitudes.repository.SolicitudRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class SolicitudService {

    private final SolicitudRepository solicitudRepository;
    private final ContenedorRepository contenedorRepository;
    private final RestTemplate restTemplate;

    @Value("${services.clientes.url}")
    private String clientesServiceUrl;

    @Transactional
    public SolicitudResponseDTO crearSolicitud(SolicitudRequestDTO request) {
        log.info("Creando nueva solicitud para cliente ID: {}", request.getClienteId());

        // Validar que el cliente existe
        validarCliente(request.getClienteId());

        // Validar que el contenedor no existe
        if (contenedorRepository.existsByIdentificacion(request.getContenedor().getIdentificacion())) {
            throw new ContenedorDuplicadoException(
                "Ya existe un contenedor con la identificación: " + request.getContenedor().getIdentificacion()
            );
        }

        // Crear contenedor
        Contenedor contenedor = Contenedor.builder()
            .identificacion(request.getContenedor().getIdentificacion())
            .peso(request.getContenedor().getPeso())
            .volumen(request.getContenedor().getVolumen())
            .direccionOrigen(request.getContenedor().getDireccionOrigen())
            .latitudOrigen(request.getContenedor().getLatitudOrigen())
            .longitudOrigen(request.getContenedor().getLongitudOrigen())
            .direccionDestino(request.getContenedor().getDireccionDestino())
            .latitudDestino(request.getContenedor().getLatitudDestino())
            .longitudDestino(request.getContenedor().getLongitudDestino())
            .activo(true)
            .build();

        // Crear solicitud
        Solicitud solicitud = Solicitud.builder()
            .numero(generarNumeroSolicitud())
            .clienteId(request.getClienteId())
            .contenedor(contenedor)
            .estado(EstadoSolicitud.BORRADOR)
            .fechaSolicitud(LocalDateTime.now())
            .build();

        Solicitud solicitudGuardada = solicitudRepository.save(solicitud);
        log.info("Solicitud creada exitosamente con número: {}", solicitudGuardada.getNumero());

        return mapToResponseDTO(solicitudGuardada);
    }

    @Transactional(readOnly = true)
    public SolicitudResponseDTO obtenerPorId(Long id) {
        Solicitud solicitud = solicitudRepository.findById(id)
            .orElseThrow(() -> new SolicitudNotFoundException("Solicitud no encontrada con ID: " + id));
        return mapToResponseDTO(solicitud);
    }

    @Transactional(readOnly = true)
    public SolicitudResponseDTO obtenerPorNumero(String numero) {
        Solicitud solicitud = solicitudRepository.findByNumero(numero)
            .orElseThrow(() -> new SolicitudNotFoundException("Solicitud no encontrada con número: " + numero));
        return mapToResponseDTO(solicitud);
    }

    @Transactional(readOnly = true)
    public List<SolicitudResponseDTO> listarTodas() {
        return solicitudRepository.findAll().stream()
            .map(this::mapToResponseDTO)
            .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<SolicitudResponseDTO> listarPorCliente(Long clienteId) {
        return solicitudRepository.findByClienteId(clienteId).stream()
            .map(this::mapToResponseDTO)
            .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<SolicitudResponseDTO> listarPorEstado(EstadoSolicitud estado) {
        return solicitudRepository.findByEstado(estado).stream()
            .map(this::mapToResponseDTO)
            .collect(Collectors.toList());
    }

    @Transactional
    public SolicitudResponseDTO actualizarEstado(Long id, EstadoSolicitud nuevoEstado) {
        Solicitud solicitud = solicitudRepository.findById(id)
            .orElseThrow(() -> new SolicitudNotFoundException("Solicitud no encontrada con ID: " + id));

        log.info("Actualizando estado de solicitud {} de {} a {}", 
            solicitud.getNumero(), solicitud.getEstado(), nuevoEstado);

        solicitud.setEstado(nuevoEstado);

        if (nuevoEstado == EstadoSolicitud.ENTREGADA && solicitud.getFechaEntregaReal() == null) {
            solicitud.setFechaEntregaReal(LocalDateTime.now());
        }

        Solicitud solicitudActualizada = solicitudRepository.save(solicitud);
        return mapToResponseDTO(solicitudActualizada);
    }

    @Transactional(readOnly = true)
    public SeguimientoDTO obtenerSeguimiento(String numero) {
        Solicitud solicitud = solicitudRepository.findByNumero(numero)
            .orElseThrow(() -> new SolicitudNotFoundException("Solicitud no encontrada con número: " + numero));

        String mensaje = generarMensajeSeguimiento(solicitud);
        String ubicacionActual = determinarUbicacionActual(solicitud);

        return SeguimientoDTO.builder()
            .numeroSolicitud(solicitud.getNumero())
            .identificacionContenedor(solicitud.getContenedor().getIdentificacion())
            .estado(solicitud.getEstado())
            .ubicacionActual(ubicacionActual)
            .costoEstimado(solicitud.getCostoEstimado())
            .tiempoEstimadoHoras(solicitud.getTiempoEstimadoHoras())
            .fechaEntregaEstimada(solicitud.getFechaEntregaEstimada())
            .mensaje(mensaje)
            .build();
    }

    private void validarCliente(Long clienteId) {
        try {
            String url = clientesServiceUrl + "/api/clientes/" + clienteId;
            restTemplate.getForObject(url, Object.class);
            log.info("Cliente validado exitosamente: {}", clienteId);
        } catch (Exception e) {
            log.error("Error al validar cliente: {}", clienteId, e);
            throw new ClienteNotFoundException("Cliente no encontrado con ID: " + clienteId);
        }
    }

    private String generarNumeroSolicitud() {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        return "SOL-" + timestamp;
    }

    private String generarMensajeSeguimiento(Solicitud solicitud) {
        return switch (solicitud.getEstado()) {
            case BORRADOR -> "Su solicitud ha sido registrada y está pendiente de programación.";
            case PROGRAMADA -> "Su solicitud ha sido programada. El contenedor será retirado próximamente.";
            case EN_TRANSITO -> "Su contenedor está en tránsito hacia el destino.";
            case ENTREGADA -> "Su contenedor ha sido entregado exitosamente.";
            case CANCELADA -> "Su solicitud ha sido cancelada.";
        };
    }

    private String determinarUbicacionActual(Solicitud solicitud) {
        return switch (solicitud.getEstado()) {
            case BORRADOR, PROGRAMADA -> solicitud.getContenedor().getDireccionOrigen();
            case EN_TRANSITO -> "En tránsito";
            case ENTREGADA -> solicitud.getContenedor().getDireccionDestino();
            case CANCELADA -> "N/A";
        };
    }

    private SolicitudResponseDTO mapToResponseDTO(Solicitud solicitud) {
        return SolicitudResponseDTO.builder()
            .id(solicitud.getId())
            .numero(solicitud.getNumero())
            .clienteId(solicitud.getClienteId())
            .contenedor(mapContenedorToResponseDTO(solicitud.getContenedor()))
            .estado(solicitud.getEstado())
            .costoEstimado(solicitud.getCostoEstimado())
            .tiempoEstimadoHoras(solicitud.getTiempoEstimadoHoras())
            .costoFinal(solicitud.getCostoFinal())
            .tiempoRealHoras(solicitud.getTiempoRealHoras())
            .rutaId(solicitud.getRutaId())
            .fechaSolicitud(solicitud.getFechaSolicitud())
            .fechaEntregaEstimada(solicitud.getFechaEntregaEstimada())
            .fechaEntregaReal(solicitud.getFechaEntregaReal())
            .fechaCreacion(solicitud.getFechaCreacion())
            .build();
    }

    private ContenedorResponseDTO mapContenedorToResponseDTO(Contenedor contenedor) {
        return ContenedorResponseDTO.builder()
            .id(contenedor.getId())
            .identificacion(contenedor.getIdentificacion())
            .peso(contenedor.getPeso())
            .volumen(contenedor.getVolumen())
            .direccionOrigen(contenedor.getDireccionOrigen())
            .latitudOrigen(contenedor.getLatitudOrigen())
            .longitudOrigen(contenedor.getLongitudOrigen())
            .direccionDestino(contenedor.getDireccionDestino())
            .latitudDestino(contenedor.getLatitudDestino())
            .longitudDestino(contenedor.getLongitudDestino())
            .build();
    }
}
