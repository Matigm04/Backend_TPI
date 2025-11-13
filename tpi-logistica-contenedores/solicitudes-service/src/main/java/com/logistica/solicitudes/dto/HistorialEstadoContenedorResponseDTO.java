package com.logistica.solicitudes.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class HistorialEstadoContenedorResponseDTO {

    private Long id;
    private Long contenedorId;
    private String estadoAnterior;
    private String estadoNuevo;
    private String ubicacion;
    private Long tramoId;
    private LocalDateTime fechaHora;
    private String observaciones;
    private String usuarioRegistro;
    private Long depositoId;
}
