package com.logistica.rutas.service;

import com.logistica.rutas.dto.googlemaps.GoogleMapsDistanceResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Service
@Slf4j
public class GoogleMapsService {

    private final RestTemplate restTemplate;

    @Value("${google.maps.api-key}")
    private String apiKey;

    @Value("${google.maps.base-url:https://maps.googleapis.com/maps/api}")
    private String baseUrl;

    public GoogleMapsService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    /**
     * Calcula la distancia entre dos puntos usando Google Maps Distance Matrix API
     * 
     * @param lat1 Latitud del origen
     * @param lon1 Longitud del origen
     * @param lat2 Latitud del destino
     * @param lon2 Longitud del destino
     * @return Distancia en kilómetros
     */
    public BigDecimal calcularDistancia(double lat1, double lon1, double lat2, double lon2) {
        try {
            String origins = lat1 + "," + lon1;
            String destinations = lat2 + "," + lon2;

            String url = UriComponentsBuilder.fromHttpUrl(baseUrl + "/distancematrix/json")
                    .queryParam("origins", origins)
                    .queryParam("destinations", destinations)
                    .queryParam("mode", "driving")
                    .queryParam("units", "metric")
                    .queryParam("key", apiKey)
                    .toUriString();

            log.debug("Llamando a Google Maps API: {}", url.replace(apiKey, "***"));

            GoogleMapsDistanceResponse response = restTemplate.getForObject(url, GoogleMapsDistanceResponse.class);

            if (response != null && "OK".equals(response.getStatus())) {
                if (!response.getRows().isEmpty() && !response.getRows().get(0).getElements().isEmpty()) {
                    GoogleMapsDistanceResponse.Element element = response.getRows().get(0).getElements().get(0);
                    
                    if ("OK".equals(element.getStatus())) {
                        // Convertir de metros a kilómetros
                        long distanciaMetros = element.getDistance().getValue();
                        BigDecimal distanciaKm = BigDecimal.valueOf(distanciaMetros)
                                .divide(BigDecimal.valueOf(1000), 2, RoundingMode.HALF_UP);
                        
                        log.info("Distancia calculada con Google Maps: {} km", distanciaKm);
                        return distanciaKm;
                    } else {
                        log.warn("Google Maps API retornó status: {}", element.getStatus());
                        throw new RuntimeException("Error en respuesta de Google Maps: " + element.getStatus());
                    }
                }
            }
            
            log.error("Respuesta inválida de Google Maps API");
            throw new RuntimeException("Respuesta inválida de Google Maps API");
            
        } catch (Exception e) {
            log.error("Error al llamar a Google Maps API: {}", e.getMessage());
            throw new RuntimeException("Error al calcular distancia con Google Maps", e);
        }
    }

    /**
     * Calcula el tiempo estimado de viaje entre dos puntos
     * 
     * @param lat1 Latitud del origen
     * @param lon1 Longitud del origen
     * @param lat2 Latitud del destino
     * @param lon2 Longitud del destino
     * @return Tiempo en minutos
     */
    public Long calcularTiempoViaje(double lat1, double lon1, double lat2, double lon2) {
        try {
            String origins = lat1 + "," + lon1;
            String destinations = lat2 + "," + lon2;

            String url = UriComponentsBuilder.fromHttpUrl(baseUrl + "/distancematrix/json")
                    .queryParam("origins", origins)
                    .queryParam("destinations", destinations)
                    .queryParam("mode", "driving")
                    .queryParam("units", "metric")
                    .queryParam("key", apiKey)
                    .toUriString();

            GoogleMapsDistanceResponse response = restTemplate.getForObject(url, GoogleMapsDistanceResponse.class);

            if (response != null && "OK".equals(response.getStatus())) {
                if (!response.getRows().isEmpty() && !response.getRows().get(0).getElements().isEmpty()) {
                    GoogleMapsDistanceResponse.Element element = response.getRows().get(0).getElements().get(0);
                    
                    if ("OK".equals(element.getStatus())) {
                        // Convertir de segundos a minutos
                        long duracionSegundos = element.getDuration().getValue();
                        long duracionMinutos = duracionSegundos / 60;
                        
                        log.info("Tiempo de viaje calculado con Google Maps: {} minutos", duracionMinutos);
                        return duracionMinutos;
                    }
                }
            }
            
            throw new RuntimeException("Error al calcular tiempo de viaje con Google Maps");
            
        } catch (Exception e) {
            log.error("Error al calcular tiempo de viaje: {}", e.getMessage());
            throw new RuntimeException("Error al calcular tiempo de viaje con Google Maps", e);
        }
    }
}
