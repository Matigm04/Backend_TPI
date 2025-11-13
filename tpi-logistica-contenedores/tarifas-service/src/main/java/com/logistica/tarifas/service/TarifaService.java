package com.logistica.tarifas.service;

import com.logistica.tarifas.dto.TarifaRequestDTO;
import com.logistica.tarifas.dto.TarifaResponseDTO;
import com.logistica.tarifas.exception.TarifaNotFoundException;
import com.logistica.tarifas.model.Tarifa;
import com.logistica.tarifas.model.TipoTarifa;
import com.logistica.tarifas.repository.TarifaRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class TarifaService {
    
    private final TarifaRepository tarifaRepository;
    
    @Transactional
    public TarifaResponseDTO crearTarifa(TarifaRequestDTO request) {
        log.info("Creando tarifa de tipo: {}", request.getTipo());
        
        Tarifa tarifa = new Tarifa();
        tarifa.setTipo(request.getTipo());
        tarifa.setDescripcion(request.getDescripcion());
        tarifa.setValor(request.getValor());
        tarifa.setUnidad(request.getUnidad());
        tarifa.setRangoPesoMinKg(request.getRangoPesoMinKg());
        tarifa.setRangoPesoMaxKg(request.getRangoPesoMaxKg());
        tarifa.setRangoVolumenMinM3(request.getRangoVolumenMinM3());
        tarifa.setRangoVolumenMaxM3(request.getRangoVolumenMaxM3());
        tarifa.setVigenciaDesde(request.getVigenciaDesde());
        tarifa.setVigenciaHasta(request.getVigenciaHasta());
        tarifa.setActivo(true);
        
        Tarifa saved = tarifaRepository.save(tarifa);
        log.info("Tarifa creada exitosamente con ID: {}", saved.getId());
        
        return mapToResponseDTO(saved);
    }
    
    @Transactional(readOnly = true)
    public List<TarifaResponseDTO> listarTodas() {
        log.info("Listando todas las tarifas activas");
        return tarifaRepository.findByActivoTrue().stream()
                .map(this::mapToResponseDTO)
                .collect(Collectors.toList());
    }
    
    @Transactional(readOnly = true)
    public TarifaResponseDTO obtenerPorId(Long id) {
        log.info("Buscando tarifa con ID: {}", id);
        Tarifa tarifa = tarifaRepository.findById(id)
                .orElseThrow(() -> new TarifaNotFoundException("Tarifa no encontrada con ID: " + id));
        return mapToResponseDTO(tarifa);
    }
    
    @Transactional(readOnly = true)
    public List<TarifaResponseDTO> listarPorTipo(TipoTarifa tipo) {
        log.info("Listando tarifas de tipo: {}", tipo);
        return tarifaRepository.findByTipoAndActivoTrue(tipo).stream()
                .map(this::mapToResponseDTO)
                .collect(Collectors.toList());
    }
    
    @Transactional(readOnly = true)
    public TarifaResponseDTO obtenerVigentePorTipo(TipoTarifa tipo) {
        log.info("Buscando tarifa vigente de tipo: {}", tipo);
        LocalDate hoy = LocalDate.now();
        Tarifa tarifa = tarifaRepository.findVigentePorTipo(tipo, hoy)
                .orElseThrow(() -> new TarifaNotFoundException(
                    "No se encontró tarifa vigente para el tipo: " + tipo));
        return mapToResponseDTO(tarifa);
    }
    
    @Transactional(readOnly = true)
    public List<TarifaResponseDTO> listarVigentes() {
        log.info("Listando todas las tarifas vigentes");
        LocalDate hoy = LocalDate.now();
        return tarifaRepository.findAllVigentes(hoy).stream()
                .map(this::mapToResponseDTO)
                .collect(Collectors.toList());
    }
    
    @Transactional
    public TarifaResponseDTO actualizarTarifa(Long id, TarifaRequestDTO request) {
        log.info("Actualizando tarifa con ID: {}", id);
        
        Tarifa tarifa = tarifaRepository.findById(id)
                .orElseThrow(() -> new TarifaNotFoundException("Tarifa no encontrada con ID: " + id));
        
        tarifa.setTipo(request.getTipo());
        tarifa.setDescripcion(request.getDescripcion());
        tarifa.setValor(request.getValor());
        tarifa.setUnidad(request.getUnidad());
        tarifa.setRangoPesoMinKg(request.getRangoPesoMinKg());
        tarifa.setRangoPesoMaxKg(request.getRangoPesoMaxKg());
        tarifa.setRangoVolumenMinM3(request.getRangoVolumenMinM3());
        tarifa.setRangoVolumenMaxM3(request.getRangoVolumenMaxM3());
        tarifa.setVigenciaDesde(request.getVigenciaDesde());
        tarifa.setVigenciaHasta(request.getVigenciaHasta());
        
        Tarifa updated = tarifaRepository.save(tarifa);
        log.info("Tarifa actualizada exitosamente");
        
        return mapToResponseDTO(updated);
    }
    
    @Transactional
    public void eliminarTarifa(Long id) {
        log.info("Eliminando (desactivando) tarifa con ID: {}", id);
        
        Tarifa tarifa = tarifaRepository.findById(id)
                .orElseThrow(() -> new TarifaNotFoundException("Tarifa no encontrada con ID: " + id));
        
        tarifa.setActivo(false);
        tarifaRepository.save(tarifa);
        
        log.info("Tarifa desactivada exitosamente");
    }
    
    private TarifaResponseDTO mapToResponseDTO(Tarifa tarifa) {
        TarifaResponseDTO dto = new TarifaResponseDTO();
        dto.setId(tarifa.getId());
        dto.setTipo(tarifa.getTipo());
        dto.setDescripcion(tarifa.getDescripcion());
        dto.setValor(tarifa.getValor());
        dto.setUnidad(tarifa.getUnidad());
        dto.setRangoPesoMinKg(tarifa.getRangoPesoMinKg());
        dto.setRangoPesoMaxKg(tarifa.getRangoPesoMaxKg());
        dto.setRangoVolumenMinM3(tarifa.getRangoVolumenMinM3());
        dto.setRangoVolumenMaxM3(tarifa.getRangoVolumenMaxM3());
        dto.setVigenciaDesde(tarifa.getVigenciaDesde());
        dto.setVigenciaHasta(tarifa.getVigenciaHasta());
        dto.setActivo(tarifa.getActivo());
        dto.setFechaCreacion(tarifa.getFechaCreacion());
        dto.setFechaActualizacion(tarifa.getFechaActualizacion());
        return dto;
    }
}
