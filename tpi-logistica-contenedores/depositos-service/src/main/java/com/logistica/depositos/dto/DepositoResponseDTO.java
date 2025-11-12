package com.logistica.depositos.dto;

import com.logistica.depositos.model.Deposito;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "DTO de respuesta con información del depósito")
public class DepositoResponseDTO {

    @Schema(description = "ID único del depósito", example = "1")
    private Long id;

    @Schema(description = "Nombre del depósito", example = "Depósito Central Buenos Aires")
    private String nombre;

    @Schema(description = "Dirección completa del depósito", example = "Av. Corrientes 1234, CABA")
    private String direccion;

    @Schema(description = "Latitud de la ubicación", example = "-34.603722")
    private Double latitud;

    @Schema(description = "Longitud de la ubicación", example = "-58.381592")
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

    @Schema(description = "Indica si el depósito está activo", example = "true")
    private Boolean activo;

    @Schema(description = "Fecha de creación del registro")
    private LocalDateTime fechaCreacion;

    @Schema(description = "Fecha de última actualización")
    private LocalDateTime fechaActualizacion;

    public static DepositoResponseDTO fromEntity(Deposito deposito) {
        DepositoResponseDTO dto = new DepositoResponseDTO();
        dto.setId(deposito.getId());
        dto.setNombre(deposito.getNombre());
        dto.setDireccion(deposito.getDireccion());
        dto.setLatitud(deposito.getLatitud());
        dto.setLongitud(deposito.getLongitud());
        dto.setCostoDiarioEstadia(deposito.getCostoDiarioEstadia());
        dto.setCapacidadMaxima(deposito.getCapacidadMaxima());
        dto.setContenedoresActuales(deposito.getContenedoresActuales());
        dto.setHorarioApertura(deposito.getHorarioApertura());
        dto.setHorarioCierre(deposito.getHorarioCierre());
        dto.setObservaciones(deposito.getObservaciones());
        dto.setActivo(deposito.getActivo());
        dto.setFechaCreacion(deposito.getFechaCreacion());
        dto.setFechaActualizacion(deposito.getFechaActualizacion());
        return dto;
    }
}
