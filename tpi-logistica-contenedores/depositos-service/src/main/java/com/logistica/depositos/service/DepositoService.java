package com.logistica.depositos.service;

import com.logistica.depositos.dto.DepositoCercanoDTO;
import com.logistica.depositos.dto.DepositoRequestDTO;
import com.logistica.depositos.dto.DepositoResponseDTO;
import com.logistica.depositos.exception.DepositoNotFoundException;
import com.logistica.depositos.model.Deposito;
import com.logistica.depositos.repository.DepositoRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class DepositoService {

    private final DepositoRepository depositoRepository;

    @Transactional
    public DepositoResponseDTO crearDeposito(DepositoRequestDTO requestDTO) {
        log.info("Creando nuevo depósito: {}", requestDTO.getNombre());
        
        Deposito deposito = new Deposito();
        deposito.setNombre(requestDTO.getNombre());
        deposito.setDireccion(requestDTO.getDireccion());
        deposito.setLatitud(requestDTO.getLatitud());
        deposito.setLongitud(requestDTO.getLongitud());
        deposito.setCostoDiarioEstadia(requestDTO.getCostoDiarioEstadia());
        deposito.setCapacidadMaxima(requestDTO.getCapacidadMaxima());
        deposito.setContenedoresActuales(requestDTO.getContenedoresActuales() != null ? requestDTO.getContenedoresActuales() : 0);
        deposito.setHorarioApertura(requestDTO.getHorarioApertura());
        deposito.setHorarioCierre(requestDTO.getHorarioCierre());
        deposito.setObservaciones(requestDTO.getObservaciones());
        deposito.setActivo(requestDTO.getActivo() != null ? requestDTO.getActivo() : true);

        Deposito depositoGuardado = depositoRepository.save(deposito);
        log.info("Depósito creado exitosamente con ID: {}", depositoGuardado.getId());
        
        return DepositoResponseDTO.fromEntity(depositoGuardado);
    }

    @Transactional(readOnly = true)
    public List<DepositoResponseDTO> listarTodos() {
        log.info("Listando todos los depósitos activos");
        return depositoRepository.findByActivoTrue().stream()
                .map(DepositoResponseDTO::fromEntity)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<DepositoResponseDTO> listarActivos() {
        log.info("Listando depósitos activos");
        return depositoRepository.findByActivoTrue().stream()
                .map(DepositoResponseDTO::fromEntity)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public DepositoResponseDTO obtenerPorId(Long id) {
        log.info("Buscando depósito con ID: {}", id);
        Deposito deposito = depositoRepository.findById(id)
                .orElseThrow(() -> new DepositoNotFoundException("Depósito no encontrado con ID: " + id));
        return DepositoResponseDTO.fromEntity(deposito);
    }

    @Transactional
    public DepositoResponseDTO actualizarDeposito(Long id, DepositoRequestDTO requestDTO) {
        log.info("Actualizando depósito con ID: {}", id);
        
        Deposito deposito = depositoRepository.findById(id)
                .orElseThrow(() -> new DepositoNotFoundException("Depósito no encontrado con ID: " + id));

        deposito.setNombre(requestDTO.getNombre());
        deposito.setDireccion(requestDTO.getDireccion());
        deposito.setLatitud(requestDTO.getLatitud());
        deposito.setLongitud(requestDTO.getLongitud());
        deposito.setCostoDiarioEstadia(requestDTO.getCostoDiarioEstadia());
        deposito.setCapacidadMaxima(requestDTO.getCapacidadMaxima());
        deposito.setContenedoresActuales(requestDTO.getContenedoresActuales());
        deposito.setHorarioApertura(requestDTO.getHorarioApertura());
        deposito.setHorarioCierre(requestDTO.getHorarioCierre());
        deposito.setObservaciones(requestDTO.getObservaciones());
        deposito.setActivo(requestDTO.getActivo());

        Deposito depositoActualizado = depositoRepository.save(deposito);
        log.info("Depósito actualizado exitosamente: {}", id);
        
        return DepositoResponseDTO.fromEntity(depositoActualizado);
    }

    @Transactional
    public void eliminarDeposito(Long id) {
        log.info("Eliminando (desactivando) depósito con ID: {}", id);
        
        Deposito deposito = depositoRepository.findById(id)
                .orElseThrow(() -> new DepositoNotFoundException("Depósito no encontrado con ID: " + id));

        deposito.setActivo(false);
        depositoRepository.save(deposito);
        
        log.info("Depósito desactivado exitosamente: {}", id);
    }

    @Transactional(readOnly = true)
    public List<DepositoCercanoDTO> buscarDepositosCercanos(Double latitud, Double longitud, Double radioKm) {
        log.info("Buscando depósitos cercanos a lat: {}, lon: {}, radio: {} km", latitud, longitud, radioKm);
        
        List<Deposito> depositos = depositoRepository.findDepositosCercanos(latitud, longitud, radioKm);
        
        return depositos.stream()
                .map(deposito -> {
                    DepositoCercanoDTO dto = new DepositoCercanoDTO();
                    dto.setId(deposito.getId());
                    dto.setNombre(deposito.getNombre());
                    dto.setDireccion(deposito.getDireccion());
                    dto.setLatitud(deposito.getLatitud());
                    dto.setLongitud(deposito.getLongitud());
                    dto.setCostoDiarioEstadia(deposito.getCostoDiarioEstadia());
                    dto.setCapacidadMaxima(deposito.getCapacidadMaxima());
                    dto.setContenedoresActuales(deposito.getContenedoresActuales());
                    dto.setHorarioApertura(deposito.getHorarioApertura());
                    dto.setHorarioCierre(deposito.getHorarioCierre());
                    dto.setObservaciones(deposito.getObservaciones());
                    dto.setDistanciaKm(calcularDistancia(latitud, longitud, deposito.getLatitud(), deposito.getLongitud()));
                    return dto;
                })
                .collect(Collectors.toList());
    }

    private Double calcularDistancia(Double lat1, Double lon1, Double lat2, Double lon2) {
        // Fórmula de Haversine para calcular distancia entre dos puntos geográficos
        final int RADIO_TIERRA_KM = 6371;
        
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);
        
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                   Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
                   Math.sin(dLon / 2) * Math.sin(dLon / 2);
        
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        
        return RADIO_TIERRA_KM * c;
    }
}
