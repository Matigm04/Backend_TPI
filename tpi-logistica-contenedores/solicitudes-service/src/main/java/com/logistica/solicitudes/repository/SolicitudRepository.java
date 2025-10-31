package com.logistica.solicitudes.repository;

import com.logistica.solicitudes.model.EstadoSolicitud;
import com.logistica.solicitudes.model.Solicitud;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SolicitudRepository extends JpaRepository<Solicitud, Long> {
    
    Optional<Solicitud> findByNumero(String numero);
    
    List<Solicitud> findByClienteId(Long clienteId);
    
    List<Solicitud> findByEstado(EstadoSolicitud estado);
    
    List<Solicitud> findByClienteIdAndEstado(Long clienteId, EstadoSolicitud estado);
    
    boolean existsByNumero(String numero);
}
