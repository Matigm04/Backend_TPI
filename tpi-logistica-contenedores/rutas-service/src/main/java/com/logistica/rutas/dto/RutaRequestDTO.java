package com.logistica.rutas.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RutaRequestDTO {

    @NotNull(message = "El ID de la solicitud es obligatorio")
    private Long solicitudId;

    private List<Long> depositosIds; // IDs de depósitos intermedios (opcional)
}
