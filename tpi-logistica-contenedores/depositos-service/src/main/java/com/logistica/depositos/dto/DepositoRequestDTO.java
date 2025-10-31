package com.logistica.depositos.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "DTO para crear o actualizar un depósito")
public class DepositoRequestDTO {

    @NotBlank(message = "El nombre es obligatorio")
    @Size(max = 100, message = "El nombre no puede exceder 100 caracteres")
    @Schema(description = "Nombre del depósito", example = "Depósito Central Buenos Aires")
    private String nombre;

    @NotBlank(message = "La dirección es obligatoria")
    @Size(max = 255, message = "La dirección no puede exceder 255 caracteres")
    @Schema(description = "Dirección completa del depósito", example = "Av. Corrientes 1234, CABA")
    private String direccion;

    @NotNull(message = "La latitud es obligatoria")
    @DecimalMin(value = "-90.0", message = "La latitud debe estar entre -90 y 90")
    @DecimalMax(value = "90.0", message = "La latitud debe estar entre -90 y 90")
    @Schema(description = "Latitud de la ubicación", example = "-34.603722")
    private Double latitud;

    @NotNull(message = "La longitud es obligatoria")
    @DecimalMin(value = "-180.0", message = "La longitud debe estar entre -180 y 180")
    @DecimalMax(value = "180.0", message = "La longitud debe estar entre -180 y 180")
    @Schema(description = "Longitud de la ubicación", example = "-58.381592")
    private Double longitud;

    @NotNull(message = "El costo diario de estadía es obligatorio")
    @DecimalMin(value = "0.0", inclusive = false, message = "El costo debe ser mayor a 0")
    @Schema(description = "Costo diario de estadía en el depósito", example = "5000.00")
    private BigDecimal costoDiarioEstadia;

    @Min(value = 1, message = "La capacidad máxima debe ser al menos 1")
    @Schema(description = "Capacidad máxima de contenedores", example = "50")
    private Integer capacidadMaxima;

    @Schema(description = "Indica si el depósito está activo", example = "true")
    private Boolean activo = true;
}
