package com.logistica.depositos.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "DTO con información de depósito y distancia calculada")
public class DepositoCercanoDTO {

    @Schema(description = "ID del depósito", example = "1")
    private Long id;

    @Schema(description = "Nombre del depósito", example = "Depósito Central")
    private String nombre;

    @Schema(description = "Dirección del depósito", example = "Av. Corrientes 1234")
    private String direccion;

    @Schema(description = "Latitud", example = "-34.603722")
    private Double latitud;

    @Schema(description = "Longitud", example = "-58.381592")
    private Double longitud;

    @Schema(description = "Costo diario de estadía", example = "5000.00")
    private BigDecimal costoDiarioEstadia;

    @Schema(description = "Distancia aproximada en kilómetros", example = "12.5")
    private Double distanciaKm;
}
