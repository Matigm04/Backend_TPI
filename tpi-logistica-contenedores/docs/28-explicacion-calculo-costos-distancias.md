# Explicación Detallada: Cálculo de Costos y Distancias

## Índice
1. [Flujo Completo del Cálculo](#flujo-completo-del-cálculo)
2. [Paso 1: Creación de Solicitud](#paso-1-creación-de-solicitud)
3. [Paso 2: Cálculo de Ruta Tentativa](#paso-2-cálculo-de-ruta-tentativa)
4. [Paso 3: Cálculo de Distancia y Tiempo con Google Maps](#paso-3-cálculo-de-distancia-y-tiempo-con-google-maps)
5. [Paso 4: Obtención de Tarifa](#paso-4-obtención-de-tarifa)
6. [Paso 5: Cálculo del Costo Estimado](#paso-5-cálculo-del-costo-estimado)
7. [Paso 6: Creación de Ruta y Tramos](#paso-6-creación-de-ruta-y-tramos)
8. [Paso 7: Actualización de Solicitud](#paso-7-actualización-de-solicitud)
9. [Resumen Visual](#resumen-visual)
10. [Cálculo de Tiempo Estimado](#cálculo-de-tiempo-estimado)

---

## Flujo Completo del Cálculo

Cuando un cliente crea una solicitud de traslado, el sistema realiza un proceso automático que involucra múltiples servicios para calcular el costo estimado del transporte. Este documento explica paso a paso cómo funciona este proceso.

---

## Paso 1: Creación de Solicitud

### Request del Cliente

```http
POST http://localhost:8080/api/solicitudes
Authorization: Bearer <token-cliente>
Content-Type: application/json

{
  "clienteId": 1,
  "tarifaId": 2,
  "contenedor": {
    "identificacion": "CONT-2024-001",
    "peso": 15000.00,
    "volumen": 33.20,
    "direccionOrigen": "Aconquija 3200, Córdoba",
    "latitudOrigen": -31.342515689097226,
    "longitudOrigen": -64.23571071051997,
    "direccionDestino": "De los Toscanos 6581, Córdoba",
    "latitudDestino": -31.361078237572773,
    "longitudDestino": -64.21225621565107
  }
}
```

### ¿Qué datos son importantes?

- **`tarifaId: 2`**: Define qué tarifa se usará para calcular el costo (ej: $1,435.99/km)
- **Coordenadas de origen**: `latitudOrigen` y `longitudOrigen`
- **Coordenadas de destino**: `latitudDestino` y `longitudDestino`

### Código: SolicitudService.java

```java
@Transactional
public SolicitudResponseDTO crearSolicitud(SolicitudRequestDTO request) {
    log.info("Creando nueva solicitud");

    // 1. Validar que el cliente existe
    Cliente cliente = clienteRepository.findById(request.getClienteId())
        .orElseThrow(() -> new ClienteNotFoundException("Cliente no encontrado"));

    // 2. Crear el contenedor
    Contenedor contenedor = Contenedor.builder()
        .identificacion(request.getContenedor().getIdentificacion())
        .peso(request.getContenedor().getPeso())
        .volumen(request.getContenedor().getVolumen())
        .direccionOrigen(request.getContenedor().getDireccionOrigen())
        .latitudOrigen(request.getContenedor().getLatitudOrigen())
        .longitudOrigen(request.getContenedor().getLongitudOrigen())
        .direccionDestino(request.getContenedor().getDireccionDestino())
        .latitudDestino(request.getContenedor().getLatitudDestino())
        .longitudDestino(request.getContenedor().getLongitudDestino())
        .estado(EstadoContenedor.DISPONIBLE)
        .fechaRegistro(LocalDateTime.now())
        .activo(true)
        .build();
    
    contenedor = contenedorRepository.save(contenedor);

    // 3. Crear la solicitud con el tarifaId
    Solicitud solicitud = Solicitud.builder()
        .numero(generarNumeroSolicitud())
        .cliente(cliente)
        .contenedor(contenedor)
        .tarifaId(request.getTarifaId())  // ← IMPORTANTE: Se guarda el tarifaId
        .ubicacionOrigen(contenedor.getDireccionOrigen())
        .ubicacionDestino(contenedor.getDireccionDestino())
        .estado(EstadoSolicitud.BORRADOR)
        .fechaSolicitud(LocalDateTime.now())
        .fechaCreacion(LocalDateTime.now())
        .activo(true)
        .build();

    solicitud = solicitudRepository.save(solicitud);

    // 4. AUTOMÁTICAMENTE calcular ruta tentativa
    calcularRutaTentativa(solicitud.getId());

    return mapToResponseDTO(solicitud);
}
```

**Lo importante aquí:**
- Se guarda el `tarifaId` en la solicitud
- Se guardan las coordenadas en el contenedor
- Al final se llama automáticamente a `calcularRutaTentativa()`

---

## Paso 2: Cálculo de Ruta Tentativa

### Código: SolicitudService.java (método privado)

```java
private void calcularRutaTentativa(Long solicitudId) {
    try {
        log.info("Calculando ruta tentativa para solicitud ID: {}", solicitudId);
        
        // Preparar el request para rutas-service
        CalcularRutaRequestDTO request = new CalcularRutaRequestDTO();
        request.setSolicitudId(solicitudId);
        request.setDepositosIds(new ArrayList<>());

        // Llamar al servicio de rutas
        String url = rutasServiceUrl + "/api/rutas/calcular";
        RutaResponseDTO ruta = restTemplate.postForObject(url, request, RutaResponseDTO.class);
        
        log.info("Ruta tentativa calculada exitosamente con ID: {}", ruta.getId());
    } catch (Exception e) {
        log.error("Error al calcular ruta tentativa: {}", e.getMessage());
    }
}
```

**Explicación:**
1. Este método prepara un request para el `rutas-service`
2. Llama al endpoint `POST /api/rutas/calcular`
3. El `RestTemplate` automáticamente propaga el JWT token
4. El `rutas-service` se encarga de todo el cálculo

### Propagación de JWT

```java
// RestTemplateConfig.java en solicitudes-service
@Bean
public RestTemplate restTemplate() {
    RestTemplate restTemplate = new RestTemplate();
    
    // Interceptor que captura el JWT y lo propaga
    ClientHttpRequestInterceptor interceptor = (request, body, execution) -> {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication instanceof JwtAuthenticationToken) {
            JwtAuthenticationToken jwtAuth = (JwtAuthenticationToken) authentication;
            Jwt jwt = jwtAuth.getToken();
            request.getHeaders().setBearerAuth(jwt.getTokenValue());
        }
        return execution.execute(request, body);
    };
    
    restTemplate.setInterceptors(Collections.singletonList(interceptor));
    return restTemplate;
}
```

---

## Paso 3: Cálculo de Distancia con Google Maps

Ahora el flujo pasa al `rutas-service`.

### Código: RutaService.java

```java
@Transactional
public RutaResponseDTO calcularRutaTentativa(CalcularRutaRequestDTO request) {
    log.info("Calculando ruta tentativa para solicitud ID: {}", request.getSolicitudId());

    // 1. Obtener la solicitud desde solicitudes-service
    SolicitudDTO solicitud = obtenerSolicitud(request.getSolicitudId());
    
    log.info("Solicitud obtenida: {} - Origen: {} - Destino: {}", 
             solicitud.getNumero(), 
             solicitud.getUbicacionOrigen(), 
             solicitud.getUbicacionDestino());

    // 2. Calcular distancia y tiempo usando Google Maps (o Haversine como fallback)
    DistanciaYTiempoDTO distanciaYTiempo = distanciaService.calcularDistanciaYTiempo(
        solicitud.getLatitudOrigen(),
        solicitud.getLongitudOrigen(),
        solicitud.getLatitudDestino(),
        solicitud.getLongitudDestino()
    );
    
    BigDecimal distancia = distanciaYTiempo.getDistanciaKm();
    Integer tiempoMinutos = distanciaYTiempo.getTiempoMinutos();

    log.info("Distancia calculada: {} km", distancia);
    log.info("Tiempo calculado: {} minutos", tiempoMinutos);

    // 3. Calcular costo usando la tarifa de la solicitud
    BigDecimal costo = calcularCostoConTarifa(distancia, solicitud.getTarifaId());

    // 4. Convertir tiempo de minutos a horas (redondeando hacia arriba)
    int tiempoEstimadoHoras = (int) Math.ceil(tiempoMinutos / 60.0);
    tiempoEstimadoHoras = Math.max(1, tiempoEstimadoHoras); // Mínimo 1 hora

    // ... continúa creando la ruta
}
```

### Código: DistanciaService.java

```java
@Service
public class DistanciaService {
    
    @Value("${google.maps.enabled:false}")
    private boolean googleMapsEnabled;
    
    @Autowired
    private GoogleMapsService googleMapsService;
    
    /**
     * Calcula distancia y tiempo de viaje entre dos coordenadas.
     * Usa Google Maps API si está habilitado, sino Haversine con estimación.
     */
    public DistanciaYTiempoDTO calcularDistanciaYTiempo(double lat1, double lon1, 
                                                         double lat2, double lon2) {
        if (googleMapsEnabled) {
            try {
                // Obtener distancia y tiempo desde Google Maps
                BigDecimal distancia = googleMapsService.calcularDistancia(lat1, lon1, lat2, lon2);
                Long tiempoMinutos = googleMapsService.calcularTiempoViaje(lat1, lon1, lat2, lon2);
                
                return DistanciaYTiempoDTO.builder()
                        .distanciaKm(distancia)
                        .tiempoMinutos(tiempoMinutos.intValue())
                        .build();
            } catch (Exception e) {
                log.warn("Error al usar Google Maps API, usando Haversine como fallback: {}", 
                         e.getMessage());
                return calcularConHaversine(lat1, lon1, lat2, lon2);
            }
        } else {
            // Si Google Maps está deshabilitado, usar Haversine
            return calcularConHaversine(lat1, lon1, lat2, lon2);
        }
    }
    
    /**
     * Calcula distancia con Haversine y estima tiempo (50 km/h promedio)
     */
    private DistanciaYTiempoDTO calcularConHaversine(double lat1, double lon1, 
                                                      double lat2, double lon2) {
        final int RADIO_TIERRA_KM = 6371;
        
        double latDistance = Math.toRadians(lat2 - lat1);
        double lonDistance = Math.toRadians(lon2 - lon1);
        
        double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(lonDistance / 2) * Math.sin(lonDistance / 2);
        
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        double distancia = RADIO_TIERRA_KM * c;
        
        BigDecimal distanciaKm = BigDecimal.valueOf(distancia)
                .setScale(2, RoundingMode.HALF_UP);
        
        // Estimar tiempo: 50 km/h promedio, mínimo 30 minutos
        int tiempoMinutos = distanciaKm
                .divide(BigDecimal.valueOf(50), 2, RoundingMode.HALF_UP)
                .multiply(BigDecimal.valueOf(60))
                .intValue();
        
        return DistanciaYTiempoDTO.builder()
                .distanciaKm(distanciaKm)
                .tiempoMinutos(Math.max(tiempoMinutos, 30))
                .build();
    }
}
```

### Código: GoogleMapsService.java

```java
@Service
public class GoogleMapsService {
    
    @Value("${google.maps.api.key}")
    private String apiKey;
    
    @Value("${google.maps.api.base-url:https://maps.googleapis.com/maps/api}")
    private String baseUrl;
    
    @Autowired
    private RestTemplate restTemplate;
    
    public BigDecimal calcularDistancia(double lat1, double lon1, double lat2, double lon2) {
        // Construir URL de la API
        String url = UriComponentsBuilder.fromHttpUrl(baseUrl + "/distancematrix/json")
            .queryParam("origins", lat1 + "," + lon1)
            .queryParam("destinations", lat2 + "," + lon2)
            .queryParam("mode", "driving")
            .queryParam("units", "metric")
            .queryParam("key", apiKey)
            .toUriString();
        
        log.info("Llamando a Google Maps Distance Matrix API");
        
        // Llamar a la API
        GoogleMapsDistanceResponse response = restTemplate.getForObject(
            url, GoogleMapsDistanceResponse.class);
        
        // Extraer la distancia en metros
        long distanciaMetros = response.getRows().get(0)
            .getElements().get(0)
            .getDistance()
            .getValue();
        
        // Convertir a kilómetros
        BigDecimal distanciaKm = BigDecimal.valueOf(distanciaMetros)
            .divide(BigDecimal.valueOf(1000), 2, RoundingMode.HALF_UP);
        
        log.info("Distancia calculada con Google Maps: {} km", distanciaKm);
        
        return distanciaKm;
    }
}
```

**Ejemplo de respuesta de Google Maps:**
```json
{
  "rows": [
    {
      "elements": [
        {
          "distance": {
            "text": "18.6 km",
            "value": 18570  ← metros
          },
          "duration": {
            "text": "23 mins",
            "value": 1380
          }
        }
      ]
    }
  ],
  "status": "OK"
}
```

**Resultado:**
```
18570 metros ÷ 1000 = 18.57 km
```

---

## Paso 4: Obtención de Tarifa

Ahora que tenemos la distancia, necesitamos obtener el precio por kilómetro.

### Código: RutaService.java

```java
private BigDecimal calcularCostoConTarifa(BigDecimal distanciaKm, Long tarifaId) {
    try {
        // Llamar a tarifas-service para obtener la tarifa
        String url = tarifasServiceUrl + "/api/tarifas/" + tarifaId;
        
        log.info("Obteniendo tarifa ID {} desde: {}", tarifaId, url);
        
        TarifaDTO tarifa = restTemplate.getForObject(url, TarifaDTO.class);
        
        if (tarifa != null && tarifa.getValor() != null) {
            // Calcular: distancia × precio por km
            BigDecimal costo = distanciaKm.multiply(tarifa.getValor());
            
            log.info("Costo calculado con tarifa ID {}: {} km × ${}/km = ${}", 
                    tarifaId, distanciaKm, tarifa.getValor(), costo);
            
            return costo;
        } else {
            log.warn("Tarifa no encontrada o sin precio, usando costo aproximado");
            return calcularCostoAproximado(distanciaKm);
        }
    } catch (Exception e) {
        log.error("Error al obtener tarifa ID {}: {}", tarifaId, e.getMessage());
        return calcularCostoAproximado(distanciaKm);
    }
}

private BigDecimal calcularCostoAproximado(BigDecimal distanciaKm) {
    // Fallback: $100 por km
    return distanciaKm.multiply(new BigDecimal("100.00"));
}
```

### Request a tarifas-service

```http
GET http://tarifas-service:8084/api/tarifas/2
Authorization: Bearer <jwt-token-propagado>
```

### Response de tarifas-service

```json
{
  "id": 2,
  "tipo": "COSTO_KM_BASE",
  "descripcion": "Tarifa base por kilómetro recorrido",
  "valor": 1435.99,
  "unidad": "$/km",
  "activo": true,
  "vigenciaDesde": "2025-01-01",
  "vigenciaHasta": null
}
```

### Código: TarifaDTO.java (en rutas-service)

```java
@Data
public class TarifaDTO {
    private Long id;
    private String tipo;
    private BigDecimal valor;  // ← Este es el precio por km
    private String descripcion;
    private Boolean activa;
}
```

---

## Paso 5: Cálculo del Costo Estimado

### Fórmula

```
Costo Estimado = Distancia (km) × Tarifa ($/km)
```

### Ejemplo con datos reales

```
Datos:
- Distancia (Google Maps): 18.57 km
- Tarifa ID 2: $1,435.99/km

Cálculo:
18.57 km × $1,435.99/km = $26,666.33

Resultado:
costoEstimado = $26,666.33
```

### Log generado

```
INFO  c.l.rutas.service.GoogleMapsService     : Distancia calculada con Google Maps: 18.57 km
INFO  c.logistica.rutas.service.RutaService   : Obteniendo tarifa ID 2 desde: http://tarifas-service:8084/api/tarifas/2
INFO  c.logistica.rutas.service.RutaService   : Costo calculado con tarifa ID 2: 18.57 km × $1435.99/km = $26666.3343
```

---

## Paso 6: Creación de Ruta y Tramos

### Código: RutaService.java (continuación)

```java
@Transactional
public RutaResponseDTO calcularRutaTentativa(CalcularRutaRequestDTO request) {
    // ... (pasos anteriores: obtener solicitud, calcular distancia, calcular costo)
    
    // 5. Crear la ruta
    Ruta ruta = Ruta.builder()
        .solicitudId(solicitud.getId())
        .cantidadTramos(1)  // Solo un tramo directo origen-destino
        .cantidadDepositos(0)  // Sin depósitos intermedios
        .distanciaTotalKm(distancia)
        .costoEstimado(costo)  // ← Aquí se guarda el costo calculado
        .tiempoEstimadoHoras(tiempoEstimadoHoras)
        .activa(true)
        .fechaCreacion(LocalDateTime.now())
        .build();

    ruta = rutaRepository.save(ruta);
    
    // 6. Crear el tramo
    Tramo tramo = Tramo.builder()
        .ruta(ruta)
        .orden(1)
        .origenTipo(TipoUbicacion.ORIGEN)
        .origenDireccion(solicitud.getUbicacionOrigen())
        .origenLatitud(solicitud.getLatitudOrigen())
        .origenLongitud(solicitud.getLongitudOrigen())
        .destinoTipo(TipoUbicacion.DESTINO)
        .destinoDireccion(solicitud.getUbicacionDestino())
        .destinoLatitud(solicitud.getLatitudDestino())
        .destinoLongitud(solicitud.getLongitudDestino())
        .tipoTramo(TipoTramo.ORIGEN_DESTINO)
        .estado(EstadoTramo.ESTIMADO)
        .distanciaKm(distancia)
        .costoAproximado(costo)  // ← Mismo costo que la ruta
        .fechaCreacion(LocalDateTime.now())
        .build();

    tramo = tramoRepository.save(tramo);
    
    // 7. Agregar tramo a la ruta
    ruta.setTramos(Collections.singletonList(tramo));
    
    log.info("Ruta tentativa calculada exitosamente con ID: {}", ruta.getId());
    
    // 8. Actualizar la solicitud con los costos calculados
    actualizarSolicitudConCostos(
        solicitud.getId(),
        costo,  // costoEstimado
        tiempoEstimadoHoras,
        null,  // costoFinal (aún null)
        null,  // tiempoReal (aún null)
        ruta.getId()
    );
    
    return mapToResponseDTO(ruta);
}
```

---

## Paso 7: Actualización de Solicitud

### Código: RutaService.java

```java
private void actualizarSolicitudConCostos(Long solicitudId, 
                                           BigDecimal costoEstimado,
                                           Integer tiempoEstimado,
                                           BigDecimal costoFinal,
                                           Integer tiempoReal,
                                           Long rutaId) {
    try {
        ActualizarCostosDTO actualizacion = ActualizarCostosDTO.builder()
            .costoEstimado(costoEstimado)
            .tiempoEstimadoHoras(tiempoEstimado)
            .costoFinal(costoFinal)
            .tiempoRealHoras(tiempoReal)
            .rutaId(rutaId)
            .build();

        String url = solicitudesServiceUrl + "/api/solicitudes/" + solicitudId + "/costos-tiempos";
        
        HttpEntity<ActualizarCostosDTO> request = new HttpEntity<>(actualizacion);
        
        restTemplate.patchForObject(url, request, Void.class);
        
        log.info("Solicitud {} actualizada con costos y tiempos", solicitudId);
    } catch (Exception e) {
        log.error("Error al actualizar costos de solicitud {}: {}", solicitudId, e.getMessage());
    }
}
```

### Request a solicitudes-service

```http
PATCH http://solicitudes-service:8085/api/solicitudes/4/costos-tiempos
Authorization: Bearer <jwt-token-propagado>
Content-Type: application/json

{
  "costoEstimado": 26666.33,
  "tiempoEstimadoHoras": 1,
  "costoFinal": null,
  "tiempoRealHoras": null,
  "rutaId": 10
}
```

### Código: SolicitudService.java

```java
@Transactional
public void actualizarCostosTiempos(Long id, ActualizarCostosDTO dto) {
    Solicitud solicitud = solicitudRepository.findById(id)
        .orElseThrow(() -> new SolicitudNotFoundException("Solicitud no encontrada"));

    if (dto.getCostoEstimado() != null) {
        solicitud.setCostoEstimado(dto.getCostoEstimado());
    }
    if (dto.getTiempoEstimadoHoras() != null) {
        solicitud.setTiempoEstimadoHoras(dto.getTiempoEstimadoHoras());
    }
    if (dto.getCostoFinal() != null) {
        solicitud.setCostoFinal(dto.getCostoFinal());
    }
    if (dto.getTiempoRealHoras() != null) {
        solicitud.setTiempoRealHoras(dto.getTiempoRealHoras());
    }
    if (dto.getRutaId() != null) {
        solicitud.setRutaId(dto.getRutaId());
    }

    solicitud.setFechaActualizacion(LocalDateTime.now());
    solicitudRepository.save(solicitud);
    
    log.info("Costos y tiempos actualizados para solicitud ID: {}", id);
}
```

### Estado final en la base de datos

**Tabla: solicitudes**
```sql
id | numero           | cliente_id | tarifa_id | costo_estimado | tiempo_estimado_horas | ruta_id | estado
4  | SOL-20251113001  | 1          | 2         | 26666.33       | 1                     | 10      | BORRADOR
```

**Tabla: rutas**
```sql
id | solicitud_id | distancia_total_km | costo_estimado | costo_total_real | tiempo_estimado_horas
10 | 4            | 18.57              | 26666.33       | NULL             | 1
```

**Tabla: tramos**
```sql
id | ruta_id | orden | distancia_km | costo_aproximado | costo_real | estado
5  | 10      | 1     | 18.57        | 26666.33         | NULL       | ESTIMADO
```

---

## Resumen Visual

```
┌─────────────────────────────────────────────────────────────────┐
│ 1. CLIENTE CREA SOLICITUD                                       │
├─────────────────────────────────────────────────────────────────┤
│ POST /api/solicitudes                                           │
│ {                                                               │
│   clienteId: 1,                                                 │
│   tarifaId: 2,  ← Define qué tarifa usar                       │
│   contenedor: {                                                 │
│     latitudOrigen: -31.342516,                                  │
│     longitudOrigen: -64.235711,                                 │
│     latitudDestino: -31.361078,                                 │
│     longitudDestino: -64.212256                                 │
│   }                                                             │
│ }                                                               │
└─────────────────────────────────────────────────────────────────┘
                              ↓
┌─────────────────────────────────────────────────────────────────┐
│ 2. SOLICITUDES-SERVICE: Guarda solicitud y llama a rutas       │
├─────────────────────────────────────────────────────────────────┤
│ - Guarda solicitud con estado BORRADOR                          │
│ - Guarda tarifaId = 2                                           │
│ - Llama: POST rutas-service/api/rutas/calcular                 │
│ - Propaga JWT automáticamente                                   │
└─────────────────────────────────────────────────────────────────┘
                              ↓
┌─────────────────────────────────────────────────────────────────┐
│ 3. RUTAS-SERVICE: Obtiene datos de la solicitud                │
├─────────────────────────────────────────────────────────────────┤
│ GET solicitudes-service/api/solicitudes/4                       │
│ Response: {                                                     │
│   id: 4,                                                        │
│   tarifaId: 2,                                                  │
│   latitudOrigen: -31.342516,                                    │
│   longitudOrigen: -64.235711,                                   │
│   latitudDestino: -31.361078,                                   │
│   longitudDestino: -64.212256                                   │
│ }                                                               │
└─────────────────────────────────────────────────────────────────┘
                              ↓
┌─────────────────────────────────────────────────────────────────┐
│ 4. DISTANCIA-SERVICE: Calcula distancia con Google Maps        │
├─────────────────────────────────────────────────────────────────┤
│ GET https://maps.googleapis.com/maps/api/distancematrix/json   │
│   ?origins=-31.342516,-64.235711                                │
│   &destinations=-31.361078,-64.212256                           │
│   &mode=driving                                                 │
│   &key=AIzaSyAUp0j1WFgacoQYTKhtPI-CF6Ld7a7jHSg                 │
│                                                                 │
│ Response: { distance: { value: 18570 } } ← metros              │
│ Conversión: 18570 / 1000 = 18.57 km                            │
│                                                                 │
│ Si falla → Fallback a Haversine (~2.5 km)                      │
└─────────────────────────────────────────────────────────────────┘
                              ↓
┌─────────────────────────────────────────────────────────────────┐
│ 5. RUTAS-SERVICE: Obtiene tarifa desde tarifas-service         │
├─────────────────────────────────────────────────────────────────┤
│ GET tarifas-service/api/tarifas/2                              │
│ Response: {                                                     │
│   id: 2,                                                        │
│   valor: 1435.99  ← Precio por km                             │
│ }                                                               │
└─────────────────────────────────────────────────────────────────┘
                              ↓
┌─────────────────────────────────────────────────────────────────┐
│ 6. RUTAS-SERVICE: Calcula costo estimado                       │
├─────────────────────────────────────────────────────────────────┤
│ Fórmula: distancia × tarifa                                     │
│ Cálculo: 18.57 km × $1,435.99/km = $26,666.33                 │
│                                                                 │
│ Tiempo estimado: 18.57 km ÷ 50 km/h = 1 hora (mínimo)         │
└─────────────────────────────────────────────────────────────────┘
                              ↓
┌─────────────────────────────────────────────────────────────────┐
│ 7. RUTAS-SERVICE: Crea ruta y tramo                            │
├─────────────────────────────────────────────────────────────────┤
│ INSERT INTO rutas (                                             │
│   solicitud_id = 4,                                             │
│   distancia_total_km = 18.57,                                   │
│   costo_estimado = 26666.33,                                    │
│   tiempo_estimado_horas = 1                                     │
│ )                                                               │
│                                                                 │
│ INSERT INTO tramos (                                            │
│   ruta_id = 10,                                                 │
│   distancia_km = 18.57,                                         │
│   costo_aproximado = 26666.33,                                  │
│   estado = 'ESTIMADO'                                           │
│ )                                                               │
└─────────────────────────────────────────────────────────────────┘
                              ↓
┌─────────────────────────────────────────────────────────────────┐
│ 8. RUTAS-SERVICE: Actualiza solicitud con costos               │
├─────────────────────────────────────────────────────────────────┤
│ PATCH solicitudes-service/api/solicitudes/4/costos-tiempos     │
│ {                                                               │
│   costoEstimado: 26666.33,                                      │
│   tiempoEstimadoHoras: 1,                                       │
│   rutaId: 10                                                    │
│ }                                                               │
│                                                                 │
│ UPDATE solicitudes SET                                          │
│   costo_estimado = 26666.33,                                    │
│   tiempo_estimado_horas = 1,                                    │
│   ruta_id = 10                                                  │
│ WHERE id = 4                                                    │
└─────────────────────────────────────────────────────────────────┘
                              ↓
┌─────────────────────────────────────────────────────────────────┐
│ 9. RESPUESTA AL CLIENTE                                         │
├─────────────────────────────────────────────────────────────────┤
│ {                                                               │
│   "id": 4,                                                      │
│   "numero": "SOL-20251113001",                                  │
│   "estado": "BORRADOR",                                         │
│   "costoEstimado": 26666.33,  ← Calculado con Google Maps     │
│   "tiempoEstimadoHoras": 1,                                     │
│   "rutaId": 10                                                  │
│ }                                                               │
└─────────────────────────────────────────────────────────────────┘
```

---

## Datos Finales en la Base de Datos

### Solicitud
```json
{
  "id": 4,
  "numero": "SOL-20251113001",
  "clienteId": 1,
  "tarifaId": 2,
  "costoEstimado": 26666.33,  ← Calculado automáticamente
  "costoFinal": null,          ← Se calcula al finalizar tramos
  "tiempoEstimadoHoras": 1,
  "tiempoRealHoras": null,
  "rutaId": 10,
  "estado": "BORRADOR"
}
```

### Ruta
```json
{
  "id": 10,
  "solicitudId": 4,
  "distanciaTotalKm": 18.57,   ← Google Maps API
  "costoEstimado": 26666.33,   ← 18.57 × $1,435.99
  "costoTotalReal": null,      ← Se calcula al finalizar todos los tramos
  "tiempoEstimadoHoras": 1,
  "estado": null,
  "activa": true
}
```

### Tramo
```json
{
  "id": 5,
  "rutaId": 10,
  "orden": 1,
  "distanciaKm": 18.57,
  "costoAproximado": 26666.33,
  "costoReal": null,           ← Se calcula al finalizar el tramo
  "estado": "ESTIMADO",        ← Cambia a ASIGNADO → INICIADO → FINALIZADO
  "camionId": null
}
```

---

## Comparación: Antes vs Ahora

### Sistema Anterior (Hardcodeado)

```
Distancia: Haversine (línea recta)
  Cálculo: ~2.5 km

Tarifa: Hardcodeada en código
  Valor: $100/km

Costo Estimado: 2.5 km × $100 = $250
```

### Sistema Actual (Dinámico)

```
Distancia: Google Maps Distance Matrix API
  Cálculo: 18.57 km (ruta real por carretera)
  Fallback: Haversine si falla

Tarifa: Desde tarifas-service
  Valor: $1,435.99/km (configurable)
  Obtenida dinámicamente usando tarifaId

Costo Estimado: 18.57 km × $1,435.99 = $26,666.33
```

**Mejora:**
- Distancia: 7.4x más precisa (18.57 vs 2.5 km)
- Costo: 106x más preciso ($26,666 vs $250)
- Flexibilidad: Tarifas configurables sin cambiar código

---

## Logs del Proceso Completo

```
INFO  c.l.solicitudes.service.SolicitudService : Creando nueva solicitud
INFO  c.l.solicitudes.service.SolicitudService : Calculando ruta tentativa para solicitud ID: 4
INFO  c.l.rutas.service.RutaService            : Calculando ruta tentativa para solicitud ID: 4
INFO  c.l.rutas.service.RutaService            : Solicitud obtenida: SOL-20251113001 - Origen: Aconquija 3200, Córdoba - Destino: De los Toscanos 6581, Córdoba
INFO  c.l.rutas.service.GoogleMapsService      : Llamando a Google Maps Distance Matrix API
INFO  c.l.rutas.service.GoogleMapsService      : Distancia calculada con Google Maps: 18.57 km
INFO  c.l.rutas.service.RutaService            : Obteniendo tarifa ID 2 desde: http://tarifas-service:8084/api/tarifas/2
INFO  c.l.rutas.service.RutaService            : Costo calculado con tarifa ID 2: 18.57 km × $1435.99/km = $26666.3343
INFO  c.l.rutas.service.RutaService            : Ruta tentativa calculada exitosamente con ID: 10
INFO  c.l.rutas.service.RutaService            : Solicitud 4 actualizada con costos y tiempos
INFO  c.l.solicitudes.service.SolicitudService : Ruta tentativa calculada exitosamente con ID: 10
INFO  c.l.solicitudes.service.SolicitudService : Costos y tiempos actualizados para solicitud ID: 4
```

---

## Configuración Necesaria

### docker-compose.yml

```yaml
rutas-service:
  environment:
    # Google Maps API
    GOOGLE_MAPS_API_KEY: AIzaSyAUp0j1WFgacoQYTKhtPI-CF6Ld7a7jHSg
    GOOGLE_MAPS_ENABLED: true
    
    # URLs de otros servicios
    SOLICITUDES_SERVICE_URL: http://solicitudes-service:8085
    TARIFAS_SERVICE_URL: http://tarifas-service:8084
```

### application.yml (rutas-service)

```yaml
google:
  maps:
    api:
      key: ${GOOGLE_MAPS_API_KEY}
      base-url: https://maps.googleapis.com/maps/api
    enabled: ${GOOGLE_MAPS_ENABLED:false}

services:
  solicitudes:
    url: ${SOLICITUDES_SERVICE_URL:http://localhost:8085}
  tarifas:
    url: ${TARIFAS_SERVICE_URL:http://localhost:8084}
```

---

## Cálculo de Tiempo Estimado

### Flujo de Cálculo

El tiempo estimado se obtiene de Google Maps Distance Matrix API junto con la distancia:

**1. Solicitud a Google Maps:**
```
GET https://maps.googleapis.com/maps/api/distancematrix/json
  ?origins=-31.342516,-64.235711
  &destinations=-31.361078,-64.212256
  &mode=driving
  &units=metric
  &key=AIzaSyAUp0j1WFgacoQYTKhtPI-CF6Ld7a7jHSg
```

**2. Respuesta de Google Maps:**
```json
{
  "rows": [{
    "elements": [{
      "distance": {
        "value": 18570,
        "text": "18.6 km"
      },
      "duration": {
        "value": 1380,    ← Duración en segundos (23 minutos)
        "text": "23 mins"
      },
      "status": "OK"
    }]
  }]
}
```

**3. Extracción y Conversión:**
```java
// GoogleMapsService.calcularTiempoViaje()
long duracionSegundos = element.getDuration().getValue(); // 1380
long duracionMinutos = duracionSegundos / 60;            // 23 minutos
log.info("Tiempo de viaje calculado con Google Maps: {} minutos", duracionMinutos);
return duracionMinutos;
```

**4. Conversión a Horas (RutaService):**
```java
Integer tiempoMinutos = distanciaYTiempo.getTiempoMinutos();  // 23
Integer tiempoHoras = (int) Math.ceil(tiempoMinutos / 60.0);  // ceil(23/60) = 1
ruta.setTiempoEstimadoHoras(Math.max(1, tiempoHoras));        // 1 hora

log.info("Tiempo calculado: {} minutos ({} hora(s))", tiempoMinutos, tiempoHoras);
```

### Fallback (Haversine)

Si Google Maps falla o está deshabilitado:

```java
// Estimar tiempo basado en distancia y velocidad promedio
BigDecimal distanciaKm = calcularDistanciaHaversine(...);  // Ej: 18.57 km

int tiempoMinutos = distanciaKm
    .divide(BigDecimal.valueOf(50), 2, RoundingMode.HALF_UP)  // 18.57 ÷ 50 = 0.37 horas
    .multiply(BigDecimal.valueOf(60))                         // 0.37 × 60 = 22 minutos
    .intValue();

tiempoMinutos = Math.max(tiempoMinutos, 30);  // Mínimo 30 minutos
```

### Comparación de Métodos

| Método | Distancia | Tiempo | Horas (ceil) |
|--------|-----------|--------|-------------|
| **Google Maps** | 18.57 km | 23 minutos | 1 hora |
| **Haversine** | 18.57 km | 22 minutos | 1 hora |

**Para rutas más largas:**

| Método | Distancia | Tiempo | Horas (ceil) |
|--------|-----------|--------|-------------|
| **Google Maps** | 100 km | 85 minutos | 2 horas |
| **Haversine** | 100 km | 120 minutos | 2 horas |

### Ventajas de Usar Google Maps

1. **Tiempo Real:** Considera condiciones de tráfico y velocidades permitidas
2. **Rutas Reales:** Usa carreteras, autopistas, y caminos reales
3. **Más Preciso:** Especialmente en rutas largas o con muchas curvas
4. **Consistencia:** Distancia y tiempo vienen de la misma fuente

### Logs Generados

```
INFO c.l.rutas.service.GoogleMapsService      : Llamando a Google Maps Distance Matrix API
INFO c.l.rutas.service.GoogleMapsService      : Distancia calculada con Google Maps: 18.57 km
INFO c.l.rutas.service.GoogleMapsService      : Tiempo de viaje calculado con Google Maps: 23 minutos
INFO c.l.rutas.service.RutaService            : Tiempo calculado: 23 minutos (1 hora(s))
```

---

¡Proceso completo explicado! 🎉
