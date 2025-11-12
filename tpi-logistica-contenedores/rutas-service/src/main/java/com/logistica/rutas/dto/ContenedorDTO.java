package com.logistica.rutas.dto;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class ContenedorDTO {
    private Long id;
    private String identificacion;
    private BigDecimal peso;
    private BigDecimal volumen;
    private String direccionOrigen;
    private Double latitudOrigen;
    private Double longitudOrigen;
    private String direccionDestino;
    private Double latitudDestino;
    private Double longitudDestino;
}
