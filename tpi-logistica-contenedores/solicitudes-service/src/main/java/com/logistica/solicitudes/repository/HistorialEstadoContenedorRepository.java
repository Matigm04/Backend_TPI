package com.logistica.solicitudes.repository;

import com.logistica.solicitudes.model.HistorialEstadoContenedor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface HistorialEstadoContenedorRepository extends JpaRepository<HistorialEstadoContenedor, Long> {
    
    List<HistorialEstadoContenedor> findByContenedorIdOrderByFechaHoraDesc(Long contenedorId);
    
    List<HistorialEstadoContenedor> findByTramoIdOrderByFechaHoraDesc(Long tramoId);
    
    List<HistorialEstadoContenedor> findByDepositoIdOrderByFechaHoraDesc(Long depositoId);
}
