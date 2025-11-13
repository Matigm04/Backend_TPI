package com.logistica.rutas.dto;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class TarifaDTO {
    private Long id;
    private String tipo;
    private BigDecimal valor;
    private String descripcion;
    private Boolean activa;
}
