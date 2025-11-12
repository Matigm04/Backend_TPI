package com.logistica.rutas.dto;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class SolicitudDTO {
    private Long id;
    private String numero;
    private Long clienteId;
    private ContenedorDTO contenedor;
    private String estado;
}
