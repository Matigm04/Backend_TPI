package com.logistica.rutas.service;

import com.logistica.rutas.dto.DistanciaYTiempoDTO;
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
     * Calcula la distancia y tiempo entre dos puntos usando Google Maps API o Haversine
     */
    public DistanciaYTiempoDTO calcularDistanciaYTiempo(double lat1, double lon1, double lat2, double lon2) {
        if (googleMapsEnabled) {
            try {
                BigDecimal distancia = googleMapsService.calcularDistancia(lat1, lon1, lat2, lon2);
                Long tiempoMinutos = googleMapsService.calcularTiempoViaje(lat1, lon1, lat2, lon2);
                
                return DistanciaYTiempoDTO.builder()
                        .distanciaKm(distancia)
                        .tiempoMinutos(tiempoMinutos.intValue())
                        .build();
            } catch (Exception e) {
                log.warn("Error al usar Google Maps API, usando Haversine como fallback: {}", e.getMessage());
                return calcularConHaversine(lat1, lon1, lat2, lon2);
            }
        } else {
            return calcularConHaversine(lat1, lon1, lat2, lon2);
        }
    }

    /**
     * Calcula la distancia entre dos puntos usando la fórmula de Haversine
     * o Google Maps API si está habilitada
     * @deprecated Usar calcularDistanciaYTiempo() en su lugar
     */
    @Deprecated
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
     * Calcula distancia y tiempo usando Haversine y estimación simple
     */
    private DistanciaYTiempoDTO calcularConHaversine(double lat1, double lon1, double lat2, double lon2) {
        BigDecimal distancia = calcularDistanciaHaversine(lat1, lon1, lat2, lon2);
        
        // Estimar tiempo: 50 km/h promedio
        int tiempoMinutos = distancia.divide(BigDecimal.valueOf(50), 2, RoundingMode.HALF_UP)
                .multiply(BigDecimal.valueOf(60))
                .intValue();
        
        log.debug("Tiempo estimado con Haversine: {} minutos", tiempoMinutos);
        
        return DistanciaYTiempoDTO.builder()
                .distanciaKm(distancia)
                .tiempoMinutos(Math.max(tiempoMinutos, 30)) // Mínimo 30 minutos
                .build();
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
