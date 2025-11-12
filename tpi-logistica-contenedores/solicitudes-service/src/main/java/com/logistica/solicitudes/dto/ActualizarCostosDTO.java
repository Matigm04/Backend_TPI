package com.logistica.solicitudes.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ActualizarCostosDTO {
    private BigDecimal costoEstimado;
    private Integer tiempoEstimadoHoras;
    private BigDecimal costoFinal;
    private Integer tiempoRealHoras;
    private Long rutaId;
}
