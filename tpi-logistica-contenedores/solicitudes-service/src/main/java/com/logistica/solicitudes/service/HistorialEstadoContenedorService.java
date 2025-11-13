package com.logistica.solicitudes.service;

import com.logistica.solicitudes.dto.HistorialEstadoContenedorRequestDTO;
import com.logistica.solicitudes.dto.HistorialEstadoContenedorResponseDTO;
import com.logistica.solicitudes.exception.ResourceNotFoundException;
import com.logistica.solicitudes.model.Contenedor;
import com.logistica.solicitudes.model.Deposito;
import com.logistica.solicitudes.model.HistorialEstadoContenedor;
import com.logistica.solicitudes.model.Tramo;
import com.logistica.solicitudes.repository.ContenedorRepository;
import com.logistica.solicitudes.repository.DepositoRepository;
import com.logistica.solicitudes.repository.HistorialEstadoContenedorRepository;
import com.logistica.solicitudes.repository.TramoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class HistorialEstadoContenedorService {

    private final HistorialEstadoContenedorRepository historialRepository;
    private final ContenedorRepository contenedorRepository;
    private final TramoRepository tramoRepository;
    private final DepositoRepository depositoRepository;

    @Transactional
    public HistorialEstadoContenedorResponseDTO registrarCambioEstado(HistorialEstadoContenedorRequestDTO requestDTO) {
        Contenedor contenedor = contenedorRepository.findById(requestDTO.getContenedorId())
                .orElseThrow(() -> new ResourceNotFoundException("Contenedor no encontrado con ID: " + requestDTO.getContenedorId()));

        HistorialEstadoContenedor historial = HistorialEstadoContenedor.builder()
                .contenedor(contenedor)
                .estadoAnterior(requestDTO.getEstadoAnterior())
                .estadoNuevo(requestDTO.getEstadoNuevo())
                .ubicacion(requestDTO.getUbicacion())
                .observaciones(requestDTO.getObservaciones())
                .usuarioRegistro(requestDTO.getUsuarioRegistro())
                .build();

        if (requestDTO.getTramoId() != null) {
            Tramo tramo = tramoRepository.findById(requestDTO.getTramoId())
                    .orElseThrow(() -> new ResourceNotFoundException("Tramo no encontrado con ID: " + requestDTO.getTramoId()));
            historial.setTramo(tramo);
        }

        if (requestDTO.getDepositoId() != null) {
            Deposito deposito = depositoRepository.findById(requestDTO.getDepositoId())
                    .orElseThrow(() -> new ResourceNotFoundException("Depósito no encontrado con ID: " + requestDTO.getDepositoId()));
            historial.setDeposito(deposito);
        }

        HistorialEstadoContenedor saved = historialRepository.save(historial);
        return mapToResponseDTO(saved);
    }

    @Transactional(readOnly = true)
    public List<HistorialEstadoContenedorResponseDTO> obtenerHistorialPorContenedor(Long contenedorId) {
        if (!contenedorRepository.existsById(contenedorId)) {
            throw new ResourceNotFoundException("Contenedor no encontrado con ID: " + contenedorId);
        }
        return historialRepository.findByContenedorIdOrderByFechaHoraDesc(contenedorId)
                .stream()
                .map(this::mapToResponseDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<HistorialEstadoContenedorResponseDTO> obtenerHistorialPorTramo(Long tramoId) {
        if (!tramoRepository.existsById(tramoId)) {
            throw new ResourceNotFoundException("Tramo no encontrado con ID: " + tramoId);
        }
        return historialRepository.findByTramoIdOrderByFechaHoraDesc(tramoId)
                .stream()
                .map(this::mapToResponseDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<HistorialEstadoContenedorResponseDTO> obtenerHistorialPorDeposito(Long depositoId) {
        if (!depositoRepository.existsById(depositoId)) {
            throw new ResourceNotFoundException("Depósito no encontrado con ID: " + depositoId);
        }
        return historialRepository.findByDepositoIdOrderByFechaHoraDesc(depositoId)
                .stream()
                .map(this::mapToResponseDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<HistorialEstadoContenedorResponseDTO> listarTodos() {
        return historialRepository.findAll()
                .stream()
                .map(this::mapToResponseDTO)
                .collect(Collectors.toList());
    }

    private HistorialEstadoContenedorResponseDTO mapToResponseDTO(HistorialEstadoContenedor historial) {
        return HistorialEstadoContenedorResponseDTO.builder()
                .id(historial.getId())
                .contenedorId(historial.getContenedor().getId())
                .estadoAnterior(historial.getEstadoAnterior())
                .estadoNuevo(historial.getEstadoNuevo())
                .ubicacion(historial.getUbicacion())
                .tramoId(historial.getTramo() != null ? historial.getTramo().getId() : null)
                .fechaHora(historial.getFechaHora())
                .observaciones(historial.getObservaciones())
                .usuarioRegistro(historial.getUsuarioRegistro())
                .depositoId(historial.getDeposito() != null ? historial.getDeposito().getId() : null)
                .build();
    }
}
