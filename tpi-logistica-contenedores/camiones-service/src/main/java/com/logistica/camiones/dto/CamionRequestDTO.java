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
    
    @NotBlank(message = "El nombre del transportista es obligatorio")
    @Size(max = 100, message = "El nombre no puede exceder 100 caracteres")
    private String nombreTransportista;
    
    @NotBlank(message = "El teléfono es obligatorio")
    @Pattern(regexp = "^[+]?[0-9]{10,20}$", message = "Formato de teléfono inválido")
    private String telefono;
    
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
