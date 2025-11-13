package com.logistica.solicitudes.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "tramos")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Tramo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "ruta_id")
    private Long rutaId;

    @Column(nullable = false)
    private Integer orden;

    @Column(name = "origen_tipo", length = 20)
    private String origenTipo;

    @Column(name = "origen_direccion", length = 500)
    private String origenDireccion;

    @Column(name = "origen_latitud")
    private Double origenLatitud;

    @Column(name = "origen_longitud")
    private Double origenLongitud;

    @Column(name = "destino_tipo", length = 20)
    private String destinoTipo;

    @Column(name = "destino_direccion", length = 500)
    private String destinoDireccion;

    @Column(name = "destino_latitud")
    private Double destinoLatitud;

    @Column(name = "destino_longitud")
    private Double destinoLongitud;

    @Column(name = "tipo_tramo", length = 30)
    private String tipoTramo;

    @Column(length = 30)
    private String estado;

    @Column(name = "camion_id")
    private Long camionId;

    @Column(name = "deposito_id")
    private Long depositoId;

    @Column(name = "fecha_hora_inicio")
    private LocalDateTime fechaHoraInicio;

    @Column(name = "fecha_hora_fin")
    private LocalDateTime fechaHoraFin;

    @Column(name = "fecha_creacion")
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
