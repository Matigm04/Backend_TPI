package com.logistica.camiones.dto;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CamionRequestDTO {
    
    @NotBlank(message = "El dominio es obligatorio")
    @Size(max = 20, message = "El dominio no puede exceder 20 caracteres")
    private String dominio;
    
    @NotBlank(message = "La marca es obligatoria")
    @Size(max = 50, message = "La marca no puede exceder 50 caracteres")
    private String marca;
    
    @NotBlank(message = "El modelo es obligatorio")
    @Size(max = 50, message = "El modelo no puede exceder 50 caracteres")
    private String modelo;
    
    @NotNull(message = "El año es obligatorio")
    @Min(value = 1900, message = "El año debe ser mayor o igual a 1900")
    @Max(value = 2100, message = "El año debe ser menor o igual a 2100")
    private Integer año;
    
    @NotNull(message = "El ID del transportista es obligatorio")
    private Long transportistaId;
    
    @NotNull(message = "La capacidad de peso es obligatoria")
    @DecimalMin(value = "0.1", message = "La capacidad de peso debe ser mayor a 0")
    private BigDecimal capacidadPeso;
    
    @NotNull(message = "La capacidad de volumen es obligatoria")
    @DecimalMin(value = "0.1", message = "La capacidad de volumen debe ser mayor a 0")
    private BigDecimal capacidadVolumen;
    
    @NotNull(message = "El consumo de combustible es obligatorio")
    @DecimalMin(value = "0.01", message = "El consumo debe ser mayor a 0")
    private BigDecimal consumoCombustible;
    
    @NotNull(message = "El costo por km es obligatorio")
    @DecimalMin(value = "0.01", message = "El costo por km debe ser mayor a 0")
    private BigDecimal costoPorKm;
}
