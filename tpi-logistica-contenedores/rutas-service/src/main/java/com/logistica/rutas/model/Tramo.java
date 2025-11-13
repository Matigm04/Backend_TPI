package com.logistica.rutas.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "tramos")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Tramo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ruta_id", nullable = false)
    private Ruta ruta;

    @Column(name = "orden", nullable = false)
    private Integer orden;

    @Enumerated(EnumType.STRING)
    @Column(name = "origen_tipo", nullable = false, length = 20)
    private TipoPunto origenTipo;

    @Column(name = "origen_id")
    private Long origenId;

    @Column(name = "origen_direccion", nullable = false, length = 500)
    private String origenDireccion;

    @Column(name = "origen_latitud", nullable = false)
    private Double origenLatitud;

    @Column(name = "origen_longitud", nullable = false)
    private Double origenLongitud;

    @Enumerated(EnumType.STRING)
    @Column(name = "destino_tipo", nullable = false, length = 20)
    private TipoPunto destinoTipo;

    @Column(name = "destino_id")
    private Long destinoId;

    @Column(name = "destino_direccion", nullable = false, length = 500)
    private String destinoDireccion;

    @Column(name = "destino_latitud", nullable = false)
    private Double destinoLatitud;

    @Column(name = "destino_longitud", nullable = false)
    private Double destinoLongitud;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_tramo", nullable = false, length = 30)
    private TipoTramo tipoTramo;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private EstadoTramo estado;

    @Column(name = "distancia_km", precision = 10, scale = 2)
    private BigDecimal distanciaKm;

    @Column(name = "costo_aproximado", precision = 10, scale = 2)
    private BigDecimal costoAproximado;

    @Column(name = "costo_real", precision = 10, scale = 2)
    private BigDecimal costoReal;

    @Column(name = "fecha_hora_inicio_estimada")
    private LocalDateTime fechaHoraInicioEstimada;

    @Column(name = "fecha_hora_fin_estimada")
    private LocalDateTime fechaHoraFinEstimada;

    @Column(name = "fecha_hora_inicio")
    private LocalDateTime fechaHoraInicio;

    @Column(name = "fecha_hora_fin")
    private LocalDateTime fechaHoraFin;

    @Column(columnDefinition = "TEXT")
    private String observaciones;

    @Column(name = "camion_id")
    private Long camionId;

    @Column(name = "fecha_creacion", nullable = false, updatable = false)
    private LocalDateTime fechaCreacion;

    @Column(name = "fecha_actualizacion")
    private LocalDateTime fechaActualizacion;

    @PrePersist
    protected void onCreate() {
        fechaCreacion = LocalDateTime.now();
        fechaActualizacion = LocalDateTime.now();
        if (estado == null) {
            estado = EstadoTramo.ESTIMADO;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        fechaActualizacion = LocalDateTime.now();
    }
}
