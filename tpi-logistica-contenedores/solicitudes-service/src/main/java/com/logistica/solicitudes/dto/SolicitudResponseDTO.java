package com.logistica.solicitudes.dto;

import com.logistica.solicitudes.model.EstadoSolicitud;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
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
    private EstadoSolicitud estado;
    private BigDecimal costoEstimado;
    private Integer tiempoEstimadoHoras;
    private BigDecimal costoFinal;
    private Integer tiempoRealHoras;
    private Long rutaId;
    private LocalDateTime fechaSolicitud;
    private LocalDateTime fechaEntregaEstimada;
    private LocalDateTime fechaEntregaReal;
    private LocalDateTime fechaCreacion;
}
