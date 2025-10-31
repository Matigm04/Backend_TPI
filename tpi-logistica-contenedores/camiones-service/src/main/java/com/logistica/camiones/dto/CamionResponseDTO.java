package com.logistica.camiones.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CamionResponseDTO {
    
    private Long id;
    private String dominio;
    private String nombreTransportista;
    private String telefono;
    private BigDecimal capacidadPeso;
    private BigDecimal capacidadVolumen;
    private BigDecimal consumoCombustible;
    private BigDecimal costoPorKm;
    private Boolean disponible;
    private Boolean activo;
    private LocalDateTime fechaCreacion;
    private LocalDateTime fechaActualizacion;
}
