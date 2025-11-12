package com.logistica.depositos.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalTime;

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

    @Schema(description = "Capacidad máxima de contenedores", example = "50")
    private Integer capacidadMaxima;

    @Schema(description = "Cantidad actual de contenedores", example = "10")
    private Integer contenedoresActuales;

    @Schema(description = "Hora de apertura del depósito", example = "08:00:00")
    private LocalTime horarioApertura;

    @Schema(description = "Hora de cierre del depósito", example = "18:00:00")
    private LocalTime horarioCierre;

    @Schema(description = "Observaciones adicionales", example = "Requiere cita previa")
    private String observaciones;

    @Schema(description = "Distancia aproximada en kilómetros", example = "12.5")
    private Double distanciaKm;
}
