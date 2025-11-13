package com.logistica.solicitudes.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SolicitudRequestDTO {

    @NotNull(message = "El ID del cliente es obligatorio")
    private Long clienteId;

    @NotNull(message = "Los datos del contenedor son obligatorios")
    @Valid
    private ContenedorDTO contenedor;

    @NotBlank(message = "La ubicación de origen es obligatoria")
    private String ubicacionOrigen;

    @NotBlank(message = "La ubicación de destino es obligatoria")
    private String ubicacionDestino;

    private LocalDate fechaProgramada;

    @Size(max = 1000, message = "Las observaciones no pueden exceder 1000 caracteres")
    private String observaciones;

    private Long tarifaId;
}
