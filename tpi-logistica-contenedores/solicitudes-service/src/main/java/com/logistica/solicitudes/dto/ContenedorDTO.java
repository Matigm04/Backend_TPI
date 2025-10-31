package com.logistica.solicitudes.dto;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ContenedorDTO {

    @NotBlank(message = "La identificación del contenedor es obligatoria")
    @Size(max = 50, message = "La identificación no puede exceder 50 caracteres")
    private String identificacion;

    @NotNull(message = "El peso es obligatorio")
    @DecimalMin(value = "0.01", message = "El peso debe ser mayor a 0")
    private BigDecimal peso;

    @NotNull(message = "El volumen es obligatorio")
    @DecimalMin(value = "0.01", message = "El volumen debe ser mayor a 0")
    private BigDecimal volumen;

    @NotBlank(message = "La dirección de origen es obligatoria")
    @Size(max = 500, message = "La dirección de origen no puede exceder 500 caracteres")
    private String direccionOrigen;

    @NotNull(message = "La latitud de origen es obligatoria")
    @DecimalMin(value = "-90.0", message = "La latitud debe estar entre -90 y 90")
    @DecimalMax(value = "90.0", message = "La latitud debe estar entre -90 y 90")
    private Double latitudOrigen;

    @NotNull(message = "La longitud de origen es obligatoria")
    @DecimalMin(value = "-180.0", message = "La longitud debe estar entre -180 y 180")
    @DecimalMax(value = "180.0", message = "La longitud debe estar entre -180 y 180")
    private Double longitudOrigen;

    @NotBlank(message = "La dirección de destino es obligatoria")
    @Size(max = 500, message = "La dirección de destino no puede exceder 500 caracteres")
    private String direccionDestino;

    @NotNull(message = "La latitud de destino es obligatoria")
    @DecimalMin(value = "-90.0", message = "La latitud debe estar entre -90 y 90")
    @DecimalMax(value = "90.0", message = "La latitud debe estar entre -90 y 90")
    private Double latitudDestino;

    @NotNull(message = "La longitud de destino es obligatoria")
    @DecimalMin(value = "-180.0", message = "La longitud debe estar entre -180 y 180")
    @DecimalMax(value = "180.0", message = "La longitud debe estar entre -180 y 180")
    private Double longitudDestino;
}
