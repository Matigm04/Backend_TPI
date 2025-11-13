package com.logistica.solicitudes.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class HistorialEstadoContenedorRequestDTO {

    @NotNull(message = "El ID del contenedor es obligatorio")
    private Long contenedorId;

    @Size(max = 30, message = "El estado anterior no puede superar los 30 caracteres")
    private String estadoAnterior;

    @NotBlank(message = "El estado nuevo es obligatorio")
    @Size(max = 30, message = "El estado nuevo no puede superar los 30 caracteres")
    private String estadoNuevo;

    @Size(max = 500, message = "La ubicación no puede superar los 500 caracteres")
    private String ubicacion;

    private Long tramoId;

    @Size(max = 1000, message = "Las observaciones no pueden superar los 1000 caracteres")
    private String observaciones;

    @Size(max = 100, message = "El usuario registro no puede superar los 100 caracteres")
    private String usuarioRegistro;

    private Long depositoId;
}
