package com.logistica.rutas.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AsignarCamionDTO {

    @NotNull(message = "El ID del camión es obligatorio")
    private Long camionId;
}
