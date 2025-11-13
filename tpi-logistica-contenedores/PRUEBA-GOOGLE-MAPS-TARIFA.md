# Prueba de Google Maps API y Cálculo con Tarifa Real

## ✅ Cambios Implementados

1. **Google Maps Distance Matrix API** integrada
   - API Key: `AIzaSyAUp0j1WFgacoQYTKhtPI-CF6Ld7a7jHSg`
   - Calcula distancia real por carreteras (no línea recta)
   - Fallback a Haversine si falla

2. **Cálculo de costo con tarifa real**
   - Obtiene tarifa desde `tarifas-service` usando `tarifaId` de la solicitud
   - Fórmula: `costo = distanciaKm × tarifa.precioPorKm`
   - Fallback a $100/km si no puede obtener la tarifa

3. **JWT propagación configurada**
   - `solicitudes-service` → `rutas-service`: ✅
   - `rutas-service` → `tarifas-service`: ✅

## 🧪 Cómo Probar

### Opción 1: Usando Postman (Recomendado)

#### Paso 1: Obtener Token de Keycloak

**Nota:** Si Keycloak no está configurado aún, salta al paso 2 con el token que ya tienes guardado.

```
POST http://localhost:8180/realms/logistica-realm/protocol/openid-connect/token
Content-Type: application/x-www-form-urlencoded

Body (x-www-form-urlencoded):
- client_id: logistica-client
- username: operador
- password: operador123
- grant_type: password
```

Guardar el `access_token` de la respuesta.

#### Paso 2: Crear Nueva Solicitud con Coordenadas de Prueba

```
POST http://localhost:8080/api/solicitudes
Authorization: Bearer {TU_TOKEN_AQUI}
Content-Type: application/json

Body:
{
  "clienteId": 5,
  "ubicacionOrigen": "Av. Padre Antonio María Claret 6300, Córdoba",
  "ubicacionDestino": "Vicente Fatone 3056, Córdoba",
  "fechaProgramada": "2025-12-20",
  "observaciones": "Prueba Google Maps API y tarifa real",
  "tarifaId": 2,
  "contenedor": {
    "identificacion": "CONT-MAPS-TEST-001",
    "peso": 450.00,
    "volumen": 12.00,
    "largoM": 5.00,
    "anchoM": 2.40,
    "altoM": 2.50,
    "estado": "DISPONIBLE",
    "descripcion": "Contenedor prueba Google Maps",
    "clienteId": 5,
    "direccionOrigen": "Av. Padre Antonio María Claret 6300, Córdoba",
    "latitudOrigen": -31.342515689097226,
    "longitudOrigen": -64.23571071051997,
    "direccionDestino": "Vicente Fatone 3056, Córdoba",
    "latitudDestino": -31.361078237572773,
    "longitudDestino": -64.21225621565107
  }
}
```

**Nota:** Estas coordenadas son las que probaste en Google Maps API que retornaron:
- Distancia: **4.2 km** (4181 metros)
- Tiempo: **8 minutos** (498 segundos)

#### Paso 3: Consultar la Ruta Creada

De la respuesta del Paso 2, tomar el `rutaId` y consultar:

```
GET http://localhost:8080/api/rutas/solicitud/{SOLICITUD_ID}
Authorization: Bearer {TU_TOKEN_AQUI}
```

### Opción 2: Si ya tienes Postman configurado

Usa la colección existente pero modifica:
1. El "Paso 2" para usar las nuevas coordenadas de arriba
2. Verifica que el token sea válido

## 🔍 Qué Esperar en la Respuesta

### Con Google Maps funcionando:

```json
{
  "id": 3,
  "solicitudId": 3,
  "distanciaTotalKm": 4.18,    // ← Distancia real de Google Maps (4181m = 4.18km)
  "costoEstimado": 6002.24,     // ← 4.18 × 1435.99 (tarifa ID 2)
  "tiempoEstimadoHoras": 1,     // ← Calculado con 60 km/h
  "tramos": [{
    "distanciaKm": 4.18,
    "costoAproximado": 6002.24,
    "estado": "ESTIMADO"
  }]
}
```

### Si Google Maps falla (fallback a Haversine):

```json
{
  "distanciaTotalKm": 2.50,     // ← Distancia en línea recta (Haversine)
  "costoEstimado": 3589.98      // ← 2.50 × 1435.99
}
```

## 📊 Verificar en Logs

### Ver logs de rutas-service:

```powershell
docker logs rutas-service --tail 50 | Select-String -Pattern "Google Maps|Distancia|Costo|Tarifa"
```

**Logs esperados con Google Maps:**
```
INFO: Llamando a Google Maps API: https://maps.googleapis.com/maps/api/distancematrix/json?...
INFO: Distancia calculada con Google Maps: 4.18 km
INFO: Costo calculado con tarifa ID 2: 4.18 km × $1435.99/km = $6002.24
```

**Logs con fallback a Haversine:**
```
WARN: Error al usar Google Maps API, usando Haversine como fallback
DEBUG: Distancia calculada con Haversine: 2.50 km
```

## ❌ Solución de Problemas

### Problema: "Error 401 Unauthorized"
- **Causa:** Token expirado o inválido
- **Solución:** Obtener nuevo token en el Paso 1

### Problema: "Error 403 Forbidden"  
- **Causa:** Usuario no tiene rol OPERADOR
- **Solución:** Usar usuario `operador` o asignar rol en Keycloak

### Problema: Costo sigue siendo $910 (distancia × $100)
- **Causa:** Estás consultando una ruta antigua (ID 1 o 2)
- **Solución:** Crear una **nueva solicitud**, no consultar las existentes

### Problema: Distancia es línea recta (Haversine) en lugar de Google Maps
- **Causas posibles:**
  1. API Key inválida o con cuota excedida
  2. No hay conexión a internet desde el contenedor
  3. Google Maps retornó error

- **Verificar:**
  ```powershell
  docker logs rutas-service --tail 100 | Select-String "Google Maps"
  ```

- **Solución:**
  - Verificar que `GOOGLE_MAPS_ENABLED: true` en docker-compose.yml
  - Verificar que la API Key es correcta
  - Verificar límites de la API en Google Cloud Console

## 🎯 Diferencias Esperadas

| Concepto | Código Antiguo | Código Nuevo |
|----------|---------------|--------------|
| **Distancia** | Haversine (línea recta) | Google Maps (ruta real) |
| **Costo por km** | $100 hardcodeado | Obtenido de tarifas-service |
| **Ejemplo ruta** | 9.10 km × $100 = $910 | 4.18 km × $1435.99 = $6,002 |
| **Propagación JWT** | No funcionaba | ✅ Configurado |

## ✅ Confirmación de Éxito

La prueba es exitosa cuando veas:

1. ✅ Solicitud creada con status 201
2. ✅ `rutaId` en la respuesta de la solicitud
3. ✅ Al consultar la ruta:
   - `distanciaTotalKm` coincide con Google Maps (~4.18 km)
   - `costoEstimado` = distancia × precio de tarifa ID 2
   - Logs muestran "Distancia calculada con Google Maps"
   - Logs muestran "Costo calculado con tarifa ID 2"

## 📝 Notas Importantes

- Las rutas **existentes** (ID 1, 2) NO se recalculan automáticamente
- Debes crear una **nueva solicitud** para ver los cambios
- El token JWT se propaga automáticamente entre servicios
- Google Maps tiene límites de cuota gratuita (verificar en consola de Google)
