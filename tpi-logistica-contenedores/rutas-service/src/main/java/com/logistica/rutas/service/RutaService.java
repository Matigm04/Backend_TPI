package com.logistica.rutas.service;

import com.logistica.rutas.dto.ActualizarCostosDTO;
import com.logistica.rutas.dto.AsignarCamionDTO;
import com.logistica.rutas.dto.ContenedorDTO;
import com.logistica.rutas.dto.DistanciaYTiempoDTO;
import com.logistica.rutas.dto.RutaRequestDTO;
import com.logistica.rutas.dto.RutaResponseDTO;
import com.logistica.rutas.dto.SolicitudDTO;
import com.logistica.rutas.dto.TarifaDTO;
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

    @Value("${services.tarifas.url}")
    private String tarifasServiceUrl;

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
            DistanciaYTiempoDTO distanciaYTiempo = distanciaService.calcularDistanciaYTiempo(
                latOrigen, lonOrigen, latDestino, lonDestino);
            
            BigDecimal distancia = distanciaYTiempo.getDistanciaKm();
            BigDecimal costo = calcularCostoConTarifa(distancia, solicitud.getTarifaId());

            Tramo tramo = crearTramo(1, TipoPunto.ORIGEN, null, dirOrigen, latOrigen, lonOrigen,
                                     TipoPunto.DESTINO, null, dirDestino, latDestino, lonDestino,
                                     TipoTramo.ORIGEN_DESTINO, distancia, costo);
            tramos.add(tramo);
            ruta.addTramo(tramo);

            distanciaTotal = distancia;
            costoTotal = costo;
            
            // Convertir tiempo de minutos a horas (redondeando hacia arriba)
            Integer tiempoMinutos = distanciaYTiempo.getTiempoMinutos();
            Integer tiempoHoras = (int) Math.ceil(tiempoMinutos / 60.0);
            ruta.setTiempoEstimadoHoras(Math.max(1, tiempoHoras)); // Mínimo 1 hora
            
            log.info("Tiempo calculado: {} minutos ({} hora(s))", tiempoMinutos, tiempoHoras);
        } else {
            // Ruta con depósitos intermedios
            // TODO: Implementar lógica completa con depósitos
            log.warn("Rutas con depósitos aún no implementadas completamente");
        }

        ruta.setCantidadTramos(tramos.size());
        ruta.setDistanciaTotalKm(distanciaTotal);
        ruta.setCostoEstimado(costoTotal);
        
        // Si no se estableció el tiempo en el flujo anterior (rutas con depósitos), calcularlo
        if (ruta.getTiempoEstimadoHoras() == null) {
            Integer tiempoEstimado = calcularTiempoEstimado(distanciaTotal);
            ruta.setTiempoEstimadoHoras(tiempoEstimado);
        }
        
        Integer tiempoEstimado = ruta.getTiempoEstimadoHoras();

        Ruta rutaGuardada = rutaRepository.save(ruta);
        log.info("Ruta tentativa calculada exitosamente con ID: {}", rutaGuardada.getId());

        // Actualizar la solicitud con los costos y tiempos estimados
        actualizarSolicitudConCostos(request.getSolicitudId(), costoTotal, tiempoEstimado, null, null, rutaGuardada.getId());

        return mapToResponseDTO(rutaGuardada);
    }

    @Transactional
    public TramoResponseDTO asignarCamionATramo(Long tramoId, AsignarCamionDTO request) {
        log.info("Asignando camión {} al tramo {}", request.getCamionId(), tramoId);

        Tramo tramo = tramoRepository.findById(tramoId)
            .orElseThrow(() -> new TramoNotFoundException("Tramo no encontrado con ID: " + tramoId));

        // Validar que el camión esté disponible
        validarCamionDisponible(request.getCamionId());

        tramo.setCamionId(request.getCamionId());
        tramo.setEstado(EstadoTramo.ASIGNADO);

        Tramo tramoActualizado = tramoRepository.save(tramo);
        log.info("Camión asignado exitosamente al tramo");

        return mapTramoToResponseDTO(tramoActualizado);
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
            // Recalcular costo total real de la ruta sumando todos los tramos finalizados
            BigDecimal costoTotalReal = ruta.getTramos().stream()
                .filter(t -> t.getCostoReal() != null)
                .map(Tramo::getCostoReal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
            
            if (costoTotalReal.compareTo(BigDecimal.ZERO) > 0) {
                ruta.setCostoTotalReal(costoTotalReal);
                rutaRepository.save(ruta);
                log.info("Costo total real de ruta {} actualizado a: ${}", ruta.getId(), costoTotalReal);
            }
            
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
        return rutaRepository.findByActivaTrue().stream()
            .map(this::mapToResponseDTO)
            .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public RutaResponseDTO obtenerPorSolicitudId(Long solicitudId) {
        List<Ruta> rutas = rutaRepository.findBySolicitudId(solicitudId);
        
        if (rutas.isEmpty()) {
            throw new RutaNotFoundException("No se encontró ruta para la solicitud con ID: " + solicitudId);
        }
        
        // Filtrar solo rutas activas y tomar la última creada
        Ruta ruta = rutas.stream()
            .filter(Ruta::getActiva)
            .reduce((first, second) -> second) // Tomar la última
            .orElseThrow(() -> new RutaNotFoundException("No se encontró ruta activa para la solicitud con ID: " + solicitudId));
        
        return mapToResponseDTO(ruta);
    }

    @Transactional(readOnly = true)
    public List<TramoResponseDTO> listarTramosPorCamion(Long camionId) {
        return tramoRepository.findByCamionId(camionId).stream()
            .map(this::mapTramoToResponseDTO)
            .collect(Collectors.toList());
    }

    @Transactional
    public void desactivarRuta(Long id) {
        Ruta ruta = rutaRepository.findById(id)
            .orElseThrow(() -> new RutaNotFoundException("Ruta no encontrada con ID: " + id));
        
        // Verificar que la ruta no tenga tramos en proceso
        boolean tieneTramoEnProceso = ruta.getTramos().stream()
            .anyMatch(tramo -> tramo.getEstado() == EstadoTramo.INICIADO);
        
        if (tieneTramoEnProceso) {
            throw new IllegalStateException("No se puede desactivar una ruta con tramos en proceso");
        }
        
        ruta.setActiva(false);
        rutaRepository.save(ruta);
        log.info("Ruta ID: {} desactivada exitosamente", id);
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

    private BigDecimal calcularCostoConTarifa(BigDecimal distanciaKm, Long tarifaId) {
        try {
            String url = tarifasServiceUrl + "/api/tarifas/" + tarifaId;
            TarifaDTO tarifa = restTemplate.getForObject(url, TarifaDTO.class);
            
            if (tarifa != null && tarifa.getValor() != null) {
                BigDecimal costo = distanciaKm.multiply(tarifa.getValor());
                log.info("Costo calculado con tarifa ID {}: {} km × ${}/km = ${}",
                        tarifaId, distanciaKm, tarifa.getValor(), costo);
                return costo;
            } else {
                log.warn("Tarifa no encontrada o sin precio, usando costo aproximado");
                return calcularCostoAproximado(distanciaKm);
            }
        } catch (Exception e) {
            log.error("Error al obtener tarifa ID {}: {}", tarifaId, e.getMessage());
            return calcularCostoAproximado(distanciaKm);
        }
    }

    /**
     * Calcula tiempo estimado basado solo en distancia (fallback)
     * Este método solo se usa cuando no se puede obtener el tiempo de Google Maps
     * @deprecated Usar DistanciaService.calcularDistanciaYTiempo() que obtiene el tiempo real de Google Maps
     */
    @Deprecated
    private Integer calcularTiempoEstimado(BigDecimal distanciaKm) {
        // Velocidad promedio: 50 km/h, mínimo 1 hora
        return Math.max(1, distanciaKm.divide(new BigDecimal("50"), 0, BigDecimal.ROUND_UP).intValue());
    }

    private RutaResponseDTO mapToResponseDTO(Ruta ruta) {
        return RutaResponseDTO.builder()
            .id(ruta.getId())
            .solicitudId(ruta.getSolicitudId())
            .cantidadTramos(ruta.getCantidadTramos())
            .cantidadDepositos(ruta.getCantidadDepositos())
            .distanciaTotalKm(ruta.getDistanciaTotalKm())
            .costoEstimado(ruta.getCostoEstimado())
            .costoTotalReal(ruta.getCostoTotalReal())
            .tiempoEstimadoHoras(ruta.getTiempoEstimadoHoras())
            .estado(ruta.getEstado())
            .activa(ruta.getActiva())
            .tramos(ruta.getTramos().stream()
                .map(this::mapTramoToResponseDTO)
                .collect(Collectors.toList()))
            .fechaCreacion(ruta.getFechaCreacion())
            .fechaActualizacion(ruta.getFechaActualizacion())
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
            .fechaHoraInicioEstimada(tramo.getFechaHoraInicioEstimada())
            .fechaHoraFinEstimada(tramo.getFechaHoraFinEstimada())
            .fechaHoraInicio(tramo.getFechaHoraInicio())
            .fechaHoraFin(tramo.getFechaHoraFin())
            .observaciones(tramo.getObservaciones())
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
