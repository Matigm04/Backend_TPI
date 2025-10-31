package com.logistica.rutas.repository;

import com.logistica.rutas.model.Ruta;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RutaRepository extends JpaRepository<Ruta, Long> {
    
    Optional<Ruta> findBySolicitudIdAndActivaTrue(Long solicitudId);
    
    List<Ruta> findBySolicitudId(Long solicitudId);
    
    List<Ruta> findByActivaTrue();
}
