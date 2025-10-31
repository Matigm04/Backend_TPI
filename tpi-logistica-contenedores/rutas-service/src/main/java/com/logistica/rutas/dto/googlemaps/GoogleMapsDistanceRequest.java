package com.logistica.rutas.dto.googlemaps;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GoogleMapsDistanceRequest {
    private String origins;
    private String destinations;
    private String mode;
    private String units;
}
