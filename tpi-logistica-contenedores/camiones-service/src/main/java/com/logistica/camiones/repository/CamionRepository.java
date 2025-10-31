package com.logistica.camiones.repository;

import com.logistica.camiones.model.Camion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Repository
public interface CamionRepository extends JpaRepository<Camion, Long> {
    
    Optional<Camion> findByDominio(String dominio);
    
    List<Camion> findByDisponibleTrueAndActivoTrue();
    
    @Query("SELECT c FROM Camion c WHERE c.disponible = true AND c.activo = true " +
           "AND c.capacidadPeso >= :peso AND c.capacidadVolumen >= :volumen")
    List<Camion> findDisponiblesConCapacidad(
        @Param("peso") BigDecimal peso, 
        @Param("volumen") BigDecimal volumen
    );
    
    List<Camion> findByActivoTrue();
}
