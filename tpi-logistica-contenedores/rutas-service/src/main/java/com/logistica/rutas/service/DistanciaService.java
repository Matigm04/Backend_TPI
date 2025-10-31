package com.logistica.rutas.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Service
@Slf4j
public class DistanciaService {

    private final GoogleMapsService googleMapsService;

    @Value("${google.maps.enabled:false}")
    private boolean googleMapsEnabled;

    public DistanciaService(GoogleMapsService googleMapsService) {
        this.googleMapsService = googleMapsService;
    }

    /**
     * Calcula la distancia entre dos puntos usando la fórmula de Haversine
     * o Google Maps API si está habilitada
     */
    public BigDecimal calcularDistancia(double lat1, double lon1, double lat2, double lon2) {
        if (googleMapsEnabled) {
            try {
                return googleMapsService.calcularDistancia(lat1, lon1, lat2, lon2);
            } catch (Exception e) {
                log.warn("Error al usar Google Maps API, usando Haversine como fallback: {}", e.getMessage());
                return calcularDistanciaHaversine(lat1, lon1, lat2, lon2);
            }
        } else {
            return calcularDistanciaHaversine(lat1, lon1, lat2, lon2);
        }
    }

    /**
     * Calcula la distancia usando la fórmula de Haversine
     */
    private BigDecimal calcularDistanciaHaversine(double lat1, double lon1, double lat2, double lon2) {
        final int RADIO_TIERRA_KM = 6371;

        double latDistance = Math.toRadians(lat2 - lat1);
        double lonDistance = Math.toRadians(lon2 - lon1);

        double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(lonDistance / 2) * Math.sin(lonDistance / 2);

        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        double distancia = RADIO_TIERRA_KM * c;

        log.debug("Distancia calculada con Haversine: {} km", distancia);
        return BigDecimal.valueOf(distancia).setScale(2, RoundingMode.HALF_UP);
    }
}
