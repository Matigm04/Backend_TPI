package com.logistica.camiones.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "camiones")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Camion {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false, unique = true, length = 20)
    private String dominio; // Patente del camión
    
    @Column(nullable = false, length = 100)
    private String nombreTransportista;
    
    @Column(nullable = false, length = 20)
    private String telefono;
    
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal capacidadPeso; // En toneladas
    
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal capacidadVolumen; // En metros cúbicos
    
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal consumoCombustible; // Litros por kilómetro
    
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal costoPorKm; // Costo base por kilómetro
    
    @Column(nullable = false)
    private Boolean disponible = true;
    
    @Column(nullable = false)
    private Boolean activo = true;
    
    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime fechaCreacion;
    
    @UpdateTimestamp
    @Column(nullable = false)
    private LocalDateTime fechaActualizacion;
}
