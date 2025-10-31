package com.logistica.rutas.repository;

import com.logistica.rutas.model.EstadoTramo;
import com.logistica.rutas.model.Tramo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TramoRepository extends JpaRepository<Tramo, Long> {
    
    List<Tramo> findByRutaId(Long rutaId);
    
    List<Tramo> findByCamionId(Long camionId);
    
    List<Tramo> findByEstado(EstadoTramo estado);
    
    @Query("SELECT t FROM Tramo t WHERE t.camionId = :camionId AND t.estado IN ('ASIGNADO', 'INICIADO')")
    List<Tramo> findTramosActivosPorCamion(Long camionId);
}
