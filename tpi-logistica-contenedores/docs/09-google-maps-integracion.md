# Integración con Google Maps API

## Descripción

El sistema utiliza Google Maps Distance Matrix API para calcular distancias reales entre puntos geográficos, considerando las rutas de carretera disponibles. Esto proporciona cálculos más precisos que la fórmula de Haversine, que solo calcula distancia en línea recta.

## Configuración

### 1. Obtener API Key de Google Maps

1. Ir a [Google Cloud Console](https://console.cloud.google.com/)
2. Crear un nuevo proyecto o seleccionar uno existente
3. Habilitar la API "Distance Matrix API":
   - Ir a "APIs & Services" > "Library"
   - Buscar "Distance Matrix API"
   - Hacer clic en "Enable"
4. Crear credenciales:
   - Ir a "APIs & Services" > "Credentials"
   - Hacer clic en "Create Credentials" > "API Key"
   - Copiar la API Key generada
5. (Opcional pero recomendado) Restringir la API Key:
   - Hacer clic en la API Key creada
   - En "API restrictions", seleccionar "Restrict key"
   - Seleccionar solo "Distance Matrix API"
   - Guardar cambios

### 2. Configurar la API Key en el proyecto

#### Opción A: Variables de entorno (Recomendado para producción)

\`\`\`bash
export GOOGLE_MAPS_API_KEY=tu_api_key_aqui
export GOOGLE_MAPS_ENABLED=true
\`\`\`

#### Opción B: Archivo .env

\`\`\`env
GOOGLE_MAPS_API_KEY=tu_api_key_aqui
GOOGLE_MAPS_ENABLED=true
\`\`\`

#### Opción C: application.yml (Solo para desarrollo)

\`\`\`yaml
google:
  maps:
    api-key: tu_api_key_aqui
    enabled: true
\`\`\`

### 3. Actualizar docker-compose.yml

\`\`\`yaml
rutas-service:
  environment:
    - GOOGLE_MAPS_API_KEY=${GOOGLE_MAPS_API_KEY}
    - GOOGLE_MAPS_ENABLED=true
\`\`\`

## Uso

### Cálculo de distancia

El servicio `DistanciaService` automáticamente usa Google Maps API cuando está habilitada:

\`\`\`java
@Autowired
private DistanciaService distanciaService;

// Calcula distancia entre dos puntos
BigDecimal distancia = distanciaService.calcularDistancia(
    -34.6037, -58.3816,  // Buenos Aires
    -31.4201, -64.1888   // Córdoba
);
\`\`\`

### Fallback automático

Si Google Maps API falla o no está disponible, el sistema automáticamente usa la fórmula de Haversine como fallback:

\`\`\`
Google Maps API habilitada → Intenta usar Google Maps
    ↓ (si falla)
Fallback a Haversine → Cálculo en línea recta
\`\`\`

## API de Google Maps Distance Matrix

### Endpoint

\`\`\`
GET https://maps.googleapis.com/maps/api/distancematrix/json
\`\`\`

### Parámetros

| Parámetro | Descripción | Ejemplo |
|-----------|-------------|---------|
| origins | Coordenadas de origen | `-34.6037,-58.3816` |
| destinations | Coordenadas de destino | `-31.4201,-64.1888` |
| mode | Modo de transporte | `driving` |
| units | Sistema de unidades | `metric` |
| key | API Key | `tu_api_key` |

### Respuesta de ejemplo

\`\`\`json
{
  "destination_addresses": ["Córdoba, Argentina"],
  "origin_addresses": ["Buenos Aires, Argentina"],
  "rows": [
    {
      "elements": [
        {
          "distance": {
            "text": "702 km",
            "value": 702000
          },
          "duration": {
            "text": "8 hours 30 mins",
            "value": 30600
          },
          "status": "OK"
        }
      ]
    }
  ],
  "status": "OK"
}
\`\`\`

## Costos

Google Maps Distance Matrix API tiene los siguientes costos (verificar precios actuales):

- **Primeras 40,000 solicitudes/mes**: GRATIS
- **Solicitudes adicionales**: $5 USD por 1,000 solicitudes

### Optimización de costos

1. **Usar caché**: Guardar distancias calculadas previamente
2. **Batch requests**: Agrupar múltiples cálculos en una sola llamada
3. **Fallback a Haversine**: Para estimaciones rápidas usar Haversine

## Diferencias: Google Maps vs Haversine

| Aspecto | Google Maps | Haversine |
|---------|-------------|-----------|
| Precisión | Alta (rutas reales) | Media (línea recta) |
| Costo | Pago después de 40k req/mes | Gratis |
| Velocidad | Más lento (API externa) | Muy rápido (cálculo local) |
| Dependencia | Requiere internet | Sin dependencias |
| Uso recomendado | Producción, cálculos finales | Desarrollo, estimaciones |

## Ejemplo de uso en el sistema

### Cálculo de ruta con múltiples tramos

\`\`\`java
// Origen → Depósito 1
BigDecimal distancia1 = distanciaService.calcularDistancia(
    solicitud.getLatitudOrigen(), 
    solicitud.getLongitudOrigen(),
    deposito1.getLatitud(), 
    deposito1.getLongitud()
);

// Depósito 1 → Depósito 2
BigDecimal distancia2 = distanciaService.calcularDistancia(
    deposito1.getLatitud(), 
    deposito1.getLongitud(),
    deposito2.getLatitud(), 
    deposito2.getLongitud()
);

// Depósito 2 → Destino
BigDecimal distancia3 = distanciaService.calcularDistancia(
    deposito2.getLatitud(), 
    deposito2.getLongitud(),
    solicitud.getLatitudDestino(), 
    solicitud.getLongitudDestino()
);

BigDecimal distanciaTotal = distancia1.add(distancia2).add(distancia3);
\`\`\`

## Troubleshooting

### Error: "API key not valid"

- Verificar que la API Key esté correctamente configurada
- Verificar que Distance Matrix API esté habilitada en Google Cloud Console
- Verificar restricciones de la API Key

### Error: "OVER_QUERY_LIMIT"

- Has excedido el límite de solicitudes gratuitas
- Considerar habilitar facturación en Google Cloud
- Implementar caché para reducir llamadas

### Error: "REQUEST_DENIED"

- La API Key no tiene permisos para Distance Matrix API
- Verificar restricciones de la API Key

### Fallback a Haversine constantemente

- Verificar que `GOOGLE_MAPS_ENABLED=true`
- Verificar que la API Key esté configurada correctamente
- Revisar logs para ver el error específico

## Logs

El sistema genera logs detallados de las llamadas a Google Maps:

\`\`\`
INFO  - Distancia calculada con Google Maps: 702.00 km
WARN  - Error al usar Google Maps API, usando Haversine como fallback: API key not valid
DEBUG - Llamando a Google Maps API: https://maps.googleapis.com/maps/api/distancematrix/json?origins=-34.6037,-58.3816&destinations=-31.4201,-64.1888&mode=driving&units=metric&key=***
\`\`\`

## Referencias

- [Google Maps Distance Matrix API Documentation](https://developers.google.com/maps/documentation/distance-matrix)
- [Google Cloud Console](https://console.cloud.google.com/)
- [Pricing Calculator](https://mapsplatform.google.com/pricing/)
