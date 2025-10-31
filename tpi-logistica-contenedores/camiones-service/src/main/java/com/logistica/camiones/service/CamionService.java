package com.logistica.camiones.service;

import com.logistica.camiones.dto.CamionDisponibleDTO;
import com.logistica.camiones.dto.CamionRequestDTO;
import com.logistica.camiones.dto.CamionResponseDTO;
import com.logistica.camiones.exception.CamionNotFoundException;
import com.logistica.camiones.exception.DominioYaExisteException;
import com.logistica.camiones.model.Camion;
import com.logistica.camiones.repository.CamionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class CamionService {
    
    private final CamionRepository camionRepository;
    
    @Transactional
    public CamionResponseDTO crearCamion(CamionRequestDTO request) {
        log.info("Creando camión con dominio: {}", request.getDominio());
        
        if (camionRepository.findByDominio(request.getDominio()).isPresent()) {
            throw new DominioYaExisteException("Ya existe un camión con el dominio: " + request.getDominio());
        }
        
        Camion camion = new Camion();
        camion.setDominio(request.getDominio());
        camion.setNombreTransportista(request.getNombreTransportista());
        camion.setTelefono(request.getTelefono());
        camion.setCapacidadPeso(request.getCapacidadPeso());
        camion.setCapacidadVolumen(request.getCapacidadVolumen());
        camion.setConsumoCombustible(request.getConsumoCombustible());
        camion.setCostoPorKm(request.getCostoPorKm());
        camion.setDisponible(true);
        camion.setActivo(true);
        
        Camion saved = camionRepository.save(camion);
        log.info("Camión creado exitosamente con ID: {}", saved.getId());
        
        return mapToResponseDTO(saved);
    }
    
    @Transactional(readOnly = true)
    public List<CamionResponseDTO> listarTodos() {
        log.info("Listando todos los camiones activos");
        return camionRepository.findByActivoTrue().stream()
                .map(this::mapToResponseDTO)
                .collect(Collectors.toList());
    }
    
    @Transactional(readOnly = true)
    public CamionResponseDTO obtenerPorId(Long id) {
        log.info("Buscando camión con ID: {}", id);
        Camion camion = camionRepository.findById(id)
                .orElseThrow(() -> new CamionNotFoundException("Camión no encontrado con ID: " + id));
        return mapToResponseDTO(camion);
    }
    
    @Transactional(readOnly = true)
    public List<CamionDisponibleDTO> listarDisponibles() {
        log.info("Listando camiones disponibles");
        return camionRepository.findByDisponibleTrueAndActivoTrue().stream()
                .map(this::mapToDisponibleDTO)
                .collect(Collectors.toList());
    }
    
    @Transactional(readOnly = true)
    public List<CamionDisponibleDTO> buscarDisponiblesConCapacidad(BigDecimal peso, BigDecimal volumen) {
        log.info("Buscando camiones disponibles con capacidad: peso={}, volumen={}", peso, volumen);
        return camionRepository.findDisponiblesConCapacidad(peso, volumen).stream()
                .map(this::mapToDisponibleDTO)
                .collect(Collectors.toList());
    }
    
    @Transactional
    public CamionResponseDTO actualizarCamion(Long id, CamionRequestDTO request) {
        log.info("Actualizando camión con ID: {}", id);
        
        Camion camion = camionRepository.findById(id)
                .orElseThrow(() -> new CamionNotFoundException("Camión no encontrado con ID: " + id));
        
        // Verificar si el dominio ya existe en otro camión
        if (!camion.getDominio().equals(request.getDominio())) {
            if (camionRepository.findByDominio(request.getDominio()).isPresent()) {
                throw new DominioYaExisteException("Ya existe un camión con el dominio: " + request.getDominio());
            }
        }
        
        camion.setDominio(request.getDominio());
        camion.setNombreTransportista(request.getNombreTransportista());
        camion.setTelefono(request.getTelefono());
        camion.setCapacidadPeso(request.getCapacidadPeso());
        camion.setCapacidadVolumen(request.getCapacidadVolumen());
        camion.setConsumoCombustible(request.getConsumoCombustible());
        camion.setCostoPorKm(request.getCostoPorKm());
        
        Camion updated = camionRepository.save(camion);
        log.info("Camión actualizado exitosamente");
        
        return mapToResponseDTO(updated);
    }
    
    @Transactional
    public void eliminarCamion(Long id) {
        log.info("Eliminando (desactivando) camión con ID: {}", id);
        
        Camion camion = camionRepository.findById(id)
                .orElseThrow(() -> new CamionNotFoundException("Camión no encontrado con ID: " + id));
        
        camion.setActivo(false);
        camionRepository.save(camion);
        
        log.info("Camión desactivado exitosamente");
    }
    
    @Transactional
    public void asignarCamion(Long id) {
        log.info("Asignando camión con ID: {}", id);
        
        Camion camion = camionRepository.findById(id)
                .orElseThrow(() -> new CamionNotFoundException("Camión no encontrado con ID: " + id));
        
        if (!camion.getDisponible()) {
            throw new IllegalStateException("El camión ya está asignado");
        }
        
        camion.setDisponible(false);
        camionRepository.save(camion);
        
        log.info("Camión asignado exitosamente");
    }
    
    @Transactional
    public void liberarCamion(Long id) {
        log.info("Liberando camión con ID: {}", id);
        
        Camion camion = camionRepository.findById(id)
                .orElseThrow(() -> new CamionNotFoundException("Camión no encontrado con ID: " + id));
        
        camion.setDisponible(true);
        camionRepository.save(camion);
        
        log.info("Camión liberado exitosamente");
    }
    
    private CamionResponseDTO mapToResponseDTO(Camion camion) {
        CamionResponseDTO dto = new CamionResponseDTO();
        dto.setId(camion.getId());
        dto.setDominio(camion.getDominio());
        dto.setNombreTransportista(camion.getNombreTransportista());
        dto.setTelefono(camion.getTelefono());
        dto.setCapacidadPeso(camion.getCapacidadPeso());
        dto.setCapacidadVolumen(camion.getCapacidadVolumen());
        dto.setConsumoCombustible(camion.getConsumoCombustible());
        dto.setCostoPorKm(camion.getCostoPorKm());
        dto.setDisponible(camion.getDisponible());
        dto.setActivo(camion.getActivo());
        dto.setFechaCreacion(camion.getFechaCreacion());
        dto.setFechaActualizacion(camion.getFechaActualizacion());
        return dto;
    }
    
    private CamionDisponibleDTO mapToDisponibleDTO(Camion camion) {
        CamionDisponibleDTO dto = new CamionDisponibleDTO();
        dto.setId(camion.getId());
        dto.setDominio(camion.getDominio());
        dto.setNombreTransportista(camion.getNombreTransportista());
        dto.setCapacidadPeso(camion.getCapacidadPeso());
        dto.setCapacidadVolumen(camion.getCapacidadVolumen());
        dto.setConsumoCombustible(camion.getConsumoCombustible());
        dto.setCostoPorKm(camion.getCostoPorKm());
        return dto;
    }
}
