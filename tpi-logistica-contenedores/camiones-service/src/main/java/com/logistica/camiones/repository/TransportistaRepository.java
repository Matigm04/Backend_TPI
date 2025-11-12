package com.logistica.camiones.repository;

import com.logistica.camiones.model.Transportista;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TransportistaRepository extends JpaRepository<Transportista, Long> {
    
    List<Transportista> findByActivoTrue();
    
    Optional<Transportista> findByIdAndActivoTrue(Long id);
    
    List<Transportista> findByNombreContainingIgnoreCaseOrApellidoContainingIgnoreCase(String nombre, String apellido);
}
