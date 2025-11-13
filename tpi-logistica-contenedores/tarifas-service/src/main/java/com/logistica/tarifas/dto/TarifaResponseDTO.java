package com.logistica.tarifas.dto;

import com.logistica.tarifas.model.TipoTarifa;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TarifaResponseDTO {
    
    private Long id;
    private TipoTarifa tipo;
    private String descripcion;
    private BigDecimal valor;
    private String unidad;
    private BigDecimal rangoPesoMinKg;
    private BigDecimal rangoPesoMaxKg;
    private BigDecimal rangoVolumenMinM3;
    private BigDecimal rangoVolumenMaxM3;
    private LocalDate vigenciaDesde;
    private LocalDate vigenciaHasta;
    private Boolean activo;
    private LocalDateTime fechaCreacion;
    private LocalDateTime fechaActualizacion;
}
