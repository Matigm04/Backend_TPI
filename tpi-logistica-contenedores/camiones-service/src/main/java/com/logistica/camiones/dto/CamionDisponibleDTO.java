package com.logistica.camiones.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CamionDisponibleDTO {
    
    private Long id;
    private String dominio;
    private String nombreTransportista;
    private BigDecimal capacidadPeso;
    private BigDecimal capacidadVolumen;
    private BigDecimal consumoCombustible;
    private BigDecimal costoPorKm;
}
