package com.logistica.rutas.dto;

import com.logistica.rutas.model.EstadoTramo;
import com.logistica.rutas.model.TipoPunto;
import com.logistica.rutas.model.TipoTramo;
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
public class TramoResponseDTO {
    private Long id;
    private Integer orden;
    private TipoPunto origenTipo;
    private String origenDireccion;
    private TipoPunto destinoTipo;
    private String destinoDireccion;
    private TipoTramo tipoTramo;
    private EstadoTramo estado;
    private BigDecimal distanciaKm;
    private BigDecimal costoAproximado;
    private BigDecimal costoReal;
    private LocalDateTime fechaHoraInicio;
    private LocalDateTime fechaHoraFin;
    private Long camionId;
}
