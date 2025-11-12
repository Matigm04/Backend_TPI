package com.logistica.rutas.service;

import com.logistica.rutas.dto.ActualizarCostosDTO;
import com.logistica.rutas.dto.AsignarCamionDTO;
import com.logistica.rutas.dto.ContenedorDTO;
import com.logistica.rutas.dto.RutaRequestDTO;
import com.logistica.rutas.dto.RutaResponseDTO;
import com.logistica.rutas.dto.SolicitudDTO;
import com.logistica.rutas.dto.TramoResponseDTO;
import com.logistica.rutas.exception.CamionNoDisponibleException;
import com.logistica.rutas.exception.RutaNotFoundException;
import com.logistica.rutas.exception.TramoNotFoundException;
import com.logistica.rutas.model.*;
import com.logistica.rutas.repository.RutaRepository;
import com.logistica.rutas.repository.TramoRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class RutaService {

    private final RutaRepository rutaRepository;
    private final TramoRepository tramoRepository;
    private final RestTemplate restTemplate;
    private final DistanciaService distanciaService;

    @Value("${services.solicitudes.url}")
    private String solicitudesServiceUrl;

    @Value("${services.depositos.url}")
    private String depositosServiceUrl;

    @Value("${services.camiones.url}")
    private String camionesServiceUrl;

    @Transactional
    public RutaResponseDTO calcularRutaTentativa(RutaRequestDTO request) {
        log.info("Calculando ruta tentativa para solicitud ID: {}", request.getSolicitudId());

        // Obtener datos de la solicitud
        SolicitudDTO solicitud = obtenerSolicitud(request.getSolicitudId());
        ContenedorDTO contenedor = solicitud.getContenedor();
        
        // Extraer coordenadas de origen y destino del contenedor
        double latOrigen = contenedor.getLatitudOrigen();
        double lonOrigen = contenedor.getLongitudOrigen();
        String dirOrigen = contenedor.getDireccionOrigen();
        
        double latDestino = contenedor.getLatitudDestino();
        double lonDestino = contenedor.getLongitudDestino();
        String dirDestino = contenedor.getDireccionDestino();

        Ruta ruta = Ruta.builder()
            .solicitudId(request.getSolicitudId())
            .cantidadDepositos(request.getDepositosIds() != null ? request.getDepositosIds().size() : 0)
            .activa(true)
            .build();

        List<Tramo> tramos = new ArrayList<>();
        BigDecimal distanciaTotal = BigDecimal.ZERO;
        BigDecimal costoTotal = BigDecimal.ZERO;

        if (request.getDepositosIds() == null || request.getDepositosIds().isEmpty()) {
            // Ruta directa origen -> destino
            BigDecimal distancia = distanciaService.calcularDistancia(latOrigen, lonOrigen, latDestino, lonDestino);
            BigDecimal costo = calcularCostoAproximado(distancia);

            Tramo tramo = crearTramo(1, TipoPunto.ORIGEN, null, dirOrigen, latOrigen, lonOrigen,
                                     TipoPunto.DESTINO, null, dirDestino, latDestino, lonDestino,
                                     TipoTramo.ORIGEN_DESTINO, distancia, costo);
            tramos.add(tramo);
            ruta.addTramo(tramo);

            distanciaTotal = distancia;
            costoTotal = costo;
        } else {
            // Ruta con depósitos intermedios
            // TODO: Implementar lógica completa con depósitos
            log.warn("Rutas con depósitos aún no implementadas completamente");
        }

        ruta.setCantidadTramos(tramos.size());
        ruta.setDistanciaTotalKm(distanciaTotal);
        ruta.setCostoEstimado(costoTotal);
        Integer tiempoEstimado = calcularTiempoEstimado(distanciaTotal);
        ruta.setTiempoEstimadoHoras(tiempoEstimado);

        Ruta rutaGuardada = rutaRepository.save(ruta);
        log.info("Ruta tentativa calculada exitosamente con ID: {}", rutaGuardada.getId());

        // Actualizar la solicitud con los costos y tiempos estimados
        actualizarSolicitudConCostos(request.getSolicitudId(), costoTotal, tiempoEstimado, null, null, rutaGuardada.getId());

        return mapToResponseDTO(rutaGuardada);
    }

    @Transactional
    public RutaResponseDTO asignarCamionATramo(Long tramoId, AsignarCamionDTO request) {
        log.info("Asignando camión {} al tramo {}", request.getCamionId(), tramoId);

        Tramo tramo = tramoRepository.findById(tramoId)
            .orElseThrow(() -> new TramoNotFoundException("Tramo no encontrado con ID: " + tramoId));

        // Validar que el camión esté disponible
        validarCamionDisponible(request.getCamionId());

        tramo.setCamionId(request.getCamionId());
        tramo.setEstado(EstadoTramo.ASIGNADO);

        tramoRepository.save(tramo);
        log.info("Camión asignado exitosamente al tramo");

        return mapToResponseDTO(tramo.getRuta());
    }

    @Transactional
    public TramoResponseDTO iniciarTramo(Long tramoId) {
        log.info("Iniciando tramo ID: {}", tramoId);

        Tramo tramo = tramoRepository.findById(tramoId)
            .orElseThrow(() -> new TramoNotFoundException("Tramo no encontrado con ID: " + tramoId));

        if (tramo.getCamionId() == null) {
            throw new IllegalStateException("No se puede iniciar un tramo sin camión asignado");
        }

        tramo.setEstado(EstadoTramo.INICIADO);
        tramo.setFechaHoraInicio(LocalDateTime.now());

        Tramo tramoActualizado = tramoRepository.save(tramo);
        log.info("Tramo iniciado exitosamente");

        return mapTramoToResponseDTO(tramoActualizado);
    }

    @Transactional
    public TramoResponseDTO finalizarTramo(Long tramoId) {
        log.info("Finalizando tramo ID: {}", tramoId);

        Tramo tramo = tramoRepository.findById(tramoId)
            .orElseThrow(() -> new TramoNotFoundException("Tramo no encontrado con ID: " + tramoId));

        if (tramo.getEstado() != EstadoTramo.INICIADO) {
            throw new IllegalStateException("Solo se pueden finalizar tramos que estén iniciados");
        }

        tramo.setEstado(EstadoTramo.FINALIZADO);
        tramo.setFechaHoraFin(LocalDateTime.now());
        tramo.setCostoReal(tramo.getCostoAproximado()); // TODO: Calcular costo real

        Tramo tramoActualizado = tramoRepository.save(tramo);
        log.info("Tramo finalizado exitosamente");

        // Calcular tiempo real en horas
        Integer tiempoRealHoras = null;
        if (tramo.getFechaHoraInicio() != null && tramo.getFechaHoraFin() != null) {
            long minutos = java.time.Duration.between(tramo.getFechaHoraInicio(), tramo.getFechaHoraFin()).toMinutes();
            tiempoRealHoras = (int) Math.ceil(minutos / 60.0);
        }

        // Obtener la ruta para actualizar costos finales
        Ruta ruta = tramoActualizado.getRuta();
        if (ruta != null) {
            // Actualizar la solicitud con costos finales y tiempo real
            actualizarSolicitudConCostos(ruta.getSolicitudId(), null, null, 
                                         tramo.getCostoReal(), tiempoRealHoras, null);
        }

        return mapTramoToResponseDTO(tramoActualizado);
    }

    @Transactional(readOnly = true)
    public RutaResponseDTO obtenerPorId(Long id) {
        Ruta ruta = rutaRepository.findById(id)
            .orElseThrow(() -> new RutaNotFoundException("Ruta no encontrada con ID: " + id));
        return mapToResponseDTO(ruta);
    }

    @Transactional(readOnly = true)
    public List<RutaResponseDTO> listarTodas() {
        return rutaRepository.findAll().stream()
            .map(this::mapToResponseDTO)
            .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<TramoResponseDTO> listarTramosPorCamion(Long camionId) {
        return tramoRepository.findByCamionId(camionId).stream()
            .map(this::mapTramoToResponseDTO)
            .collect(Collectors.toList());
    }

    private SolicitudDTO obtenerSolicitud(Long solicitudId) {
        try {
            String url = solicitudesServiceUrl + "/api/solicitudes/" + solicitudId;
            SolicitudDTO solicitud = restTemplate.getForObject(url, SolicitudDTO.class);
            if (solicitud == null || solicitud.getContenedor() == null) {
                throw new RuntimeException("Solicitud o contenedor no encontrado");
            }
            log.info("Solicitud obtenida: {} - Origen: {} - Destino: {}", 
                    solicitud.getNumero(), 
                    solicitud.getContenedor().getDireccionOrigen(),
                    solicitud.getContenedor().getDireccionDestino());
            return solicitud;
        } catch (Exception e) {
            log.error("Error al obtener solicitud: {}", solicitudId, e);
            throw new RuntimeException("Solicitud no encontrada con ID: " + solicitudId);
        }
    }

    private void validarCamionDisponible(Long camionId) {
        try {
            String url = camionesServiceUrl + "/api/camiones/" + camionId;
            Object camion = restTemplate.getForObject(url, Object.class);
            // TODO: Validar que el camión esté disponible
        } catch (Exception e) {
            log.error("Error al validar camión: {}", camionId, e);
            throw new CamionNoDisponibleException("Camión no disponible con ID: " + camionId);
        }
    }

    private Tramo crearTramo(int orden, TipoPunto origenTipo, Long origenId, String origenDir, 
                            double origenLat, double origenLon, TipoPunto destinoTipo, Long destinoId,
                            String destinoDir, double destinoLat, double destinoLon, TipoTramo tipoTramo,
                            BigDecimal distancia, BigDecimal costo) {
        return Tramo.builder()
            .orden(orden)
            .origenTipo(origenTipo)
            .origenId(origenId)
            .origenDireccion(origenDir)
            .origenLatitud(origenLat)
            .origenLongitud(origenLon)
            .destinoTipo(destinoTipo)
            .destinoId(destinoId)
            .destinoDireccion(destinoDir)
            .destinoLatitud(destinoLat)
            .destinoLongitud(destinoLon)
            .tipoTramo(tipoTramo)
            .estado(EstadoTramo.ESTIMADO)
            .distanciaKm(distancia)
            .costoAproximado(costo)
            .build();
    }

    private BigDecimal calcularCostoAproximado(BigDecimal distanciaKm) {
        // Costo base por km (ejemplo: $100 por km)
        BigDecimal costoPorKm = new BigDecimal("100.00");
        return distanciaKm.multiply(costoPorKm);
    }

    private Integer calcularTiempoEstimado(BigDecimal distanciaKm) {
        // Velocidad promedio: 60 km/h
        return distanciaKm.divide(new BigDecimal("60"), 0, BigDecimal.ROUND_UP).intValue();
    }

    private RutaResponseDTO mapToResponseDTO(Ruta ruta) {
        return RutaResponseDTO.builder()
            .id(ruta.getId())
            .solicitudId(ruta.getSolicitudId())
            .cantidadTramos(ruta.getCantidadTramos())
            .cantidadDepositos(ruta.getCantidadDepositos())
            .distanciaTotalKm(ruta.getDistanciaTotalKm())
            .costoEstimado(ruta.getCostoEstimado())
            .tiempoEstimadoHoras(ruta.getTiempoEstimadoHoras())
            .activa(ruta.getActiva())
            .tramos(ruta.getTramos().stream()
                .map(this::mapTramoToResponseDTO)
                .collect(Collectors.toList()))
            .fechaCreacion(ruta.getFechaCreacion())
            .build();
    }

    private TramoResponseDTO mapTramoToResponseDTO(Tramo tramo) {
        return TramoResponseDTO.builder()
            .id(tramo.getId())
            .orden(tramo.getOrden())
            .origenTipo(tramo.getOrigenTipo())
            .origenDireccion(tramo.getOrigenDireccion())
            .destinoTipo(tramo.getDestinoTipo())
            .destinoDireccion(tramo.getDestinoDireccion())
            .tipoTramo(tramo.getTipoTramo())
            .estado(tramo.getEstado())
            .distanciaKm(tramo.getDistanciaKm())
            .costoAproximado(tramo.getCostoAproximado())
            .costoReal(tramo.getCostoReal())
            .fechaHoraInicio(tramo.getFechaHoraInicio())
            .fechaHoraFin(tramo.getFechaHoraFin())
            .camionId(tramo.getCamionId())
            .build();
    }

    private void actualizarSolicitudConCostos(Long solicitudId, BigDecimal costoEstimado, 
                                               Integer tiempoEstimadoHoras, BigDecimal costoFinal, 
                                               Integer tiempoRealHoras, Long rutaId) {
        try {
            String url = solicitudesServiceUrl + "/api/solicitudes/" + solicitudId + "/costos-tiempos";
            
            ActualizarCostosDTO actualizacion = ActualizarCostosDTO.builder()
                .costoEstimado(costoEstimado)
                .tiempoEstimadoHoras(tiempoEstimadoHoras)
                .costoFinal(costoFinal)
                .tiempoRealHoras(tiempoRealHoras)
                .rutaId(rutaId)
                .build();
            
            // Usar exchange() en lugar de patchForObject() porque RestTemplate no soporta PATCH por defecto
            org.springframework.http.HttpEntity<ActualizarCostosDTO> request = 
                new org.springframework.http.HttpEntity<>(actualizacion);
            
            restTemplate.exchange(url, org.springframework.http.HttpMethod.PATCH, request, Object.class);
            
            log.info("Solicitud {} actualizada con costos: estimado={}, final={}, tiempoEstimado={}, tiempoReal={}", 
                     solicitudId, costoEstimado, costoFinal, tiempoEstimadoHoras, tiempoRealHoras);
        } catch (Exception e) {
            log.error("Error al actualizar costos de solicitud {}: {}", solicitudId, e.getMessage());
            // No lanzamos excepción para no afectar el flujo principal
        }
    }
}
