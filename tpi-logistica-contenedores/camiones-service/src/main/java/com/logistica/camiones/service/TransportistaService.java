package com.logistica.camiones.service;

import com.logistica.camiones.dto.TransportistaDTO;
import com.logistica.camiones.exception.ResourceNotFoundException;
import com.logistica.camiones.model.Transportista;
import com.logistica.camiones.repository.TransportistaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class TransportistaService {

    @Autowired
    private TransportistaRepository transportistaRepository;

    public List<TransportistaDTO> listarTodos() {
        return transportistaRepository.findByActivoTrue().stream()
                .map(this::convertirADTO)
                .collect(Collectors.toList());
    }

    public TransportistaDTO obtenerPorId(Long id) {
        Transportista transportista = transportistaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Transportista no encontrado con id: " + id));
        return convertirADTO(transportista);
    }

    public TransportistaDTO crear(TransportistaDTO dto) {
        Transportista transportista = new Transportista();
        transportista.setNombre(dto.getNombre());
        transportista.setApellido(dto.getApellido());
        transportista.setTelefono(dto.getTelefono());
        transportista.setActivo(true);

        Transportista guardado = transportistaRepository.save(transportista);
        return convertirADTO(guardado);
    }

    public TransportistaDTO actualizar(Long id, TransportistaDTO dto) {
        Transportista transportista = transportistaRepository.findByIdAndActivoTrue(id)
                .orElseThrow(() -> new ResourceNotFoundException("Transportista no encontrado con id: " + id));

        transportista.setNombre(dto.getNombre());
        transportista.setApellido(dto.getApellido());
        transportista.setTelefono(dto.getTelefono());

        Transportista actualizado = transportistaRepository.save(transportista);
        return convertirADTO(actualizado);
    }

    public void desactivar(Long id) {
        Transportista transportista = transportistaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Transportista no encontrado con id: " + id));
        transportista.setActivo(false);
        transportistaRepository.save(transportista);
    }

    public List<TransportistaDTO> buscarPorNombre(String nombre) {
        return transportistaRepository.findByNombreContainingIgnoreCaseOrApellidoContainingIgnoreCase(nombre, nombre).stream()
                .filter(t -> t.getActivo())
                .map(this::convertirADTO)
                .collect(Collectors.toList());
    }

    // Método interno para validar existencia (usado por CamionService)
    public Transportista obtenerEntidadPorId(Long id) {
        return transportistaRepository.findByIdAndActivoTrue(id)
                .orElseThrow(() -> new ResourceNotFoundException("Transportista no encontrado con id: " + id));
    }

    private TransportistaDTO convertirADTO(Transportista transportista) {
        TransportistaDTO dto = new TransportistaDTO();
        dto.setId(transportista.getId());
        dto.setNombre(transportista.getNombre());
        dto.setApellido(transportista.getApellido());
        dto.setTelefono(transportista.getTelefono());
        dto.setActivo(transportista.getActivo());
        return dto;
    }
}
