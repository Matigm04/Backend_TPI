package com.logistica.depositos.repository;

import com.logistica.depositos.model.Deposito;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DepositoRepository extends JpaRepository<Deposito, Long> {

    List<Deposito> findByActivoTrue();

    Optional<Deposito> findByIdAndActivoTrue(Long id);

    @Query("SELECT d FROM Deposito d WHERE d.activo = true " +
           "AND (6371 * acos(cos(radians(:latitud)) * cos(radians(d.latitud)) * " +
           "cos(radians(d.longitud) - radians(:longitud)) + " +
           "sin(radians(:latitud)) * sin(radians(d.latitud)))) <= :radioKm " +
           "ORDER BY (6371 * acos(cos(radians(:latitud)) * cos(radians(d.latitud)) * " +
           "cos(radians(d.longitud) - radians(:longitud)) + " +
           "sin(radians(:latitud)) * sin(radians(d.latitud))))")
    List<Deposito> findDepositosCercanos(
        @Param("latitud") Double latitud,
        @Param("longitud") Double longitud,
        @Param("radioKm") Double radioKm
    );
}
