package com.logistica.solicitudes.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "contenedores")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Contenedor {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 50)
    private String identificacion;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal peso; // en kilogramos

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal volumen; // en metros cúbicos

    @Column(precision = 5, scale = 2)
    private BigDecimal largoM;

    @Column(precision = 5, scale = 2)
    private BigDecimal anchoM;

    @Column(precision = 5, scale = 2)
    private BigDecimal altoM;

    @Column(length = 30)
    private String estado;

    @Column(columnDefinition = "TEXT")
    private String descripcion;

    @Column(name = "cliente_id")
    private Long clienteId;

    @Column(nullable = false, length = 500)
    private String direccionOrigen;

    @Column(nullable = false)
    private Double latitudOrigen;

    @Column(nullable = false)
    private Double longitudOrigen;

    @Column(nullable = false, length = 500)
    private String direccionDestino;

    @Column(nullable = false)
    private Double latitudDestino;

    @Column(nullable = false)
    private Double longitudDestino;

    @Column(nullable = false)
    private Boolean activo = true;

    @Column(name = "fecha_creacion", nullable = false, updatable = false)
    private LocalDateTime fechaCreacion;

    @Column(name = "fecha_actualizacion")
    private LocalDateTime fechaActualizacion;

    @PrePersist
    protected void onCreate() {
        fechaCreacion = LocalDateTime.now();
        fechaActualizacion = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        fechaActualizacion = LocalDateTime.now();
    }
}
