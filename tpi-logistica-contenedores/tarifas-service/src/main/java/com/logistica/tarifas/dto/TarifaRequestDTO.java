package com.logistica.tarifas.dto;

import com.logistica.tarifas.model.TipoTarifa;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TarifaRequestDTO {
    
    @NotNull(message = "El tipo de tarifa es obligatorio")
    private TipoTarifa tipo;
    
    @NotBlank(message = "La descripción es obligatoria")
    @Size(max = 200, message = "La descripción no puede exceder 200 caracteres")
    private String descripcion;
    
    @NotNull(message = "El valor es obligatorio")
    @DecimalMin(value = "0.01", message = "El valor debe ser mayor a 0")
    private BigDecimal valor;
    
    @Size(max = 20, message = "La unidad no puede exceder 20 caracteres")
    private String unidad;
    
    @NotNull(message = "La fecha de vigencia desde es obligatoria")
    private LocalDate vigenciaDesde;
    
    private LocalDate vigenciaHasta;
}
