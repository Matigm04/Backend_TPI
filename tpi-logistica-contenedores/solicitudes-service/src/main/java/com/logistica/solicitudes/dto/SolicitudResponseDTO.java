package com.logistica.solicitudes.dto;

import com.logistica.solicitudes.model.EstadoSolicitud;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SolicitudResponseDTO {
    private Long id;
    private String numero;
    private Long clienteId;
    private ContenedorResponseDTO contenedor;
    private String ubicacionOrigen;
    private String ubicacionDestino;
    private EstadoSolicitud estado;
    private BigDecimal costoEstimado;
    private Integer tiempoEstimadoHoras;
    private BigDecimal costoFinal;
    private Integer tiempoRealHoras;
    private Long rutaId;
    private Long tarifaId;
    private LocalDateTime fechaSolicitud;
    private LocalDate fechaProgramada;
    private LocalDateTime fechaEntregaEstimada;
    private LocalDateTime fechaEntregaReal;
    private String observaciones;
    private Boolean activo;
    private LocalDateTime fechaCreacion;
    private LocalDateTime fechaActualizacion;
}
