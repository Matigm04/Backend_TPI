package com.logistica.rutas.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RutaResponseDTO {
    private Long id;
    private Long solicitudId;
    private Integer cantidadTramos;
    private Integer cantidadDepositos;
    private BigDecimal distanciaTotalKm;
    private BigDecimal costoEstimado;
    private BigDecimal costoTotalReal;
    private Integer tiempoEstimadoHoras;
    private String estado;
    private Boolean activa;
    private List<TramoResponseDTO> tramos;
    private LocalDateTime fechaCreacion;
    private LocalDateTime fechaActualizacion;
}
