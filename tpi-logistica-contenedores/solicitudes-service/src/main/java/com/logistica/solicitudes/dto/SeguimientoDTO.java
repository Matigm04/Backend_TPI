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
public class SeguimientoDTO {
    private String numeroSolicitud;
    private String identificacionContenedor;
    private EstadoSolicitud estado;
    private String ubicacionActual;
    private BigDecimal costoEstimado;
    private Integer tiempoEstimadoHoras;
    private LocalDateTime fechaEntregaEstimada;
    private String mensaje;
}
