package com.logistica.tarifas.repository;

import com.logistica.tarifas.model.Tarifa;
import com.logistica.tarifas.model.TipoTarifa;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface TarifaRepository extends JpaRepository<Tarifa, Long> {
    
    List<Tarifa> findByActivoTrue();
    
    List<Tarifa> findByTipoAndActivoTrue(TipoTarifa tipo);
    
    @Query("SELECT t FROM Tarifa t WHERE t.tipo = :tipo AND t.activo = true " +
           "AND t.vigenciaDesde <= :fecha " +
           "AND (t.vigenciaHasta IS NULL OR t.vigenciaHasta >= :fecha)")
    Optional<Tarifa> findVigentePorTipo(
        @Param("tipo") TipoTarifa tipo, 
        @Param("fecha") LocalDate fecha
    );
    
    @Query("SELECT t FROM Tarifa t WHERE t.activo = true " +
           "AND t.vigenciaDesde <= :fecha " +
           "AND (t.vigenciaHasta IS NULL OR t.vigenciaHasta >= :fecha)")
    List<Tarifa> findAllVigentes(@Param("fecha") LocalDate fecha);
}
