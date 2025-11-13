package com.logistica.solicitudes.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ContenedorResponseDTO {
    private Long id;
    private String identificacion;
    private BigDecimal peso;
    private BigDecimal volumen;
    private BigDecimal largoM;
    private BigDecimal anchoM;
    private BigDecimal altoM;
    private String estado;
    private String descripcion;
    private Long clienteId;
    private String direccionOrigen;
    private Double latitudOrigen;
    private Double longitudOrigen;
    private String direccionDestino;
    private Double latitudDestino;
    private Double longitudDestino;
}
