package com.logistica.solicitudes.repository;

import com.logistica.solicitudes.model.Contenedor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ContenedorRepository extends JpaRepository<Contenedor, Long> {
    
    Optional<Contenedor> findByIdentificacion(String identificacion);
    
    boolean existsByIdentificacion(String identificacion);
}
