# Guía Completa de Testing - Todos los Endpoints

## Índice
1. [Preparación](#preparación)
2. [Clientes Service](#clientes-service)
3. [Depósitos Service](#depositos-service)
4. [Camiones Service](#camiones-service)
5. [Tarifas Service](#tarifas-service)
6. [Solicitudes Service](#solicitudes-service)
7. [Rutas Service](#rutas-service)
8. [Caso de Prueba Completo End-to-End](#caso-de-prueba-completo-end-to-end)

---

## Preparación

### Obtener Tokens JWT

Necesitarás tokens de diferentes roles para probar los endpoints:

#### Token de Operador
```
POST http://localhost:8180/realms/logistica-realm/protocol/openid-connect/token
Content-Type: application/x-www-form-urlencoded

client_id=logistica-api
client_secret=<tu-client-secret>
username=operador1
password=operador123
grant_type=password
```

#### Token de Cliente
```
POST http://localhost:8180/realms/logistica-realm/protocol/openid-connect/token
Content-Type: application/x-www-form-urlencoded

client_id=logistica-api
client_secret=<tu-client-secret>
username=cliente1
password=cliente123
grant_type=password
```

#### Token de Transportista
```
POST http://localhost:8180/realms/logistica-realm/protocol/openid-connect/token
Content-Type: application/x-www-form-urlencoded

client_id=logistica-api
client_secret=<tu-client-secret>
username=transportista1
password=transportista123
grant_type=password
```

---

## Clientes Service

**Base URL:** `http://localhost:8080/api/clientes`

### 1. Listar Todos los Clientes
**Rol requerido:** OPERADOR

```
GET http://localhost:8080/api/clientes
Authorization: Bearer <token-operador>
```

**Respuesta esperada (200 OK):**
```json
[
  {
    "id": 1,
    "nombre": "Juan",
    "apellido": "Pérez",
    "dni": "12345678",
    "email": "juan.perez@email.com",
    "telefono": "+54 351 123-4567",
    "direccion": "Av. Colón 1234, Córdoba",
    "ciudad": "Córdoba",
    "provincia": "Córdoba",
    "codigoPostal": "5000",
    "activo": true
  }
]
```

### 2. Obtener Cliente por ID
**Rol requerido:** OPERADOR, CLIENTE (solo su propio ID)

```
GET http://localhost:8080/api/clientes/1
Authorization: Bearer <token-operador>
```

**Respuesta esperada (200 OK):** Objeto cliente

### 3. Crear Cliente
**Rol requerido:** OPERADOR

```
POST http://localhost:8080/api/clientes
Authorization: Bearer <token-operador>
Content-Type: application/json

{
  "nombre": "María",
  "apellido": "González",
  "dni": "87654321",
  "email": "maria.gonzalez@email.com",
  "telefono": "+54 351 555-6666",
  "direccion": "Av. Vélez Sarsfield 500, Córdoba",
  "ciudad": "Córdoba",
  "provincia": "Córdoba",
  "codigoPostal": "5000"
}
```

**Respuesta esperada (201 Created):**
```json
{
  "id": 5,
  "nombre": "María",
  "apellido": "González",
  "dni": "87654321",
  "email": "maria.gonzalez@email.com",
  "telefono": "+54 351 555-6666",
  "direccion": "Av. Vélez Sarsfield 500, Córdoba",
  "ciudad": "Córdoba",
  "provincia": "Córdoba",
  "codigoPostal": "5000",
  "activo": true
}
```

**📝 Validaciones:**
- `dni`: Solo dígitos, 7 u 8 caracteres (sin guiones ni puntos)
- `nombre` y `apellido`: Entre 2 y 100 caracteres
- `email`: Formato válido, máximo 150 caracteres
- `telefono`: 8-20 caracteres, puede incluir +, -, espacios, paréntesis
- `direccion`, `ciudad`, `provincia`, `codigoPostal`: Opcionales

### 4. Actualizar Cliente
**Rol requerido:** OPERADOR

```
PUT http://localhost:8080/api/clientes/5
Authorization: Bearer <token-operador>
Content-Type: application/json

{
  "nombre": "María Fernanda",
  "apellido": "González",
  "dni": "87654321",
  "email": "mariaf.gonzalez@email.com",
  "telefono": "+54 351 555-7777",
  "direccion": "Av. Vélez Sarsfield 500, Piso 2, Córdoba",
  "ciudad": "Córdoba",
  "provincia": "Córdoba",
  "codigoPostal": "5000"
}
```

**Respuesta esperada (200 OK):** Cliente actualizado

### 5. Desactivar Cliente
**Rol requerido:** OPERADOR

```
DELETE http://localhost:8080/api/clientes/5
Authorization: Bearer <token-operador>
```

**Respuesta esperada (204 No Content)**

---

## Depósitos Service

**Base URL:** `http://localhost:8080/api/depositos`

### 1. Listar Todos los Depósitos
**Rol requerido:** OPERADOR, TRANSPORTISTA

```
GET http://localhost:8080/api/depositos
Authorization: Bearer <token-operador>
```

**Respuesta esperada (200 OK):**
```json
[
  {
    "id": 1,
    "nombre": "Depósito Central Córdoba",
    "direccion": "Av. Circunvalación Km 5, Córdoba",
    "latitud": -31.3937,
    "longitud": -64.2324,
    "capacidadMaxima": 100,
    "espacioDisponible": 75,
    "activo": true
  }
]
```

### 2. Obtener Depósito por ID
**Rol requerido:** OPERADOR, TRANSPORTISTA

```
GET http://localhost:8080/api/depositos/1
Authorization: Bearer <token-operador>
```

**Respuesta esperada (200 OK):** Objeto depósito

### 3. Buscar Depósitos Cercanos
**Rol requerido:** OPERADOR, TRANSPORTISTA

```
GET http://localhost:8080/api/depositos/cercanos?latitud=-31.403771&longitud=-64.163894&radio=20
Authorization: Bearer <token-operador>
```

**Parámetros:**
- `latitud`: Latitud del punto de referencia (requerido)
- `longitud`: Longitud del punto de referencia (requerido)
- `radio`: Radio de búsqueda en km (requerido)

**Respuesta esperada (200 OK):**
```json
[
  {
    "id": 1,
    "nombre": "Depósito Central Córdoba",
    "direccion": "Av. Circunvalación Km 5, Córdoba",
    "latitud": -31.3937,
    "longitud": -64.2324,
    "capacidadMaxima": 100,
    "espacioDisponible": 75,
    "distanciaKm": 3.45,
    "activo": true
  }
]
```

### 4. Crear Depósito
**Rol requerido:** OPERADOR

```
POST http://localhost:8080/api/depositos
Authorization: Bearer <token-operador>
Content-Type: application/json

{
  "nombre": "Depósito Norte",
  "direccion": "Ruta 9 Km 10, Córdoba",
  "latitud": -31.350000,
  "longitud": -64.200000,
  "costoDiarioEstadia": 3000.00,
  "capacidadMaxima": 80
}
```

**Respuesta esperada (201 Created):** Depósito creado

**📝 Validaciones:**
- `nombre`: Obligatorio, máximo 100 caracteres
- `direccion`: Obligatoria, máximo 255 caracteres
- `latitud`: Obligatoria, entre -90 y 90
- `longitud`: Obligatoria, entre -180 y 180
- `costoDiarioEstadia`: Obligatorio, mayor a 0
- `capacidadMaxima`: Opcional, mínimo 1 si se provee

### 5. Actualizar Depósito
**Rol requerido:** OPERADOR

```
PUT http://localhost:8080/api/depositos/1
Authorization: Bearer <token-operador>
Content-Type: application/json

{
  "nombre": "Depósito Central Córdoba - Actualizado",
  "direccion": "Av. Circunvalación Km 5.5, Córdoba",
  "latitud": -31.3937,
  "longitud": -64.2324,
  "costoDiarioEstadia": 5500.00,
  "capacidadMaxima": 120
}
```

**Respuesta esperada (200 OK):** Depósito actualizado

### 6. Desactivar Depósito
**Rol requerido:** OPERADOR

```
DELETE http://localhost:8080/api/depositos/1
Authorization: Bearer <token-operador>
```

**Respuesta esperada (204 No Content)**

---

## Camiones Service

**Base URL:** `http://localhost:8080/api/camiones`

### 1. Listar Todos los Camiones
**Rol requerido:** OPERADOR, TRANSPORTISTA

```
GET http://localhost:8080/api/camiones
Authorization: Bearer <token-operador>
```

**Respuesta esperada (200 OK):**
```json
[
  {
    "id": 1,
    "dominio": "AA123BB",
    "nombreTransportista": "Carlos Rodríguez",
    "telefono": "+541112345678",
    "capacidadPeso": 15.00,
    "capacidadVolumen": 40.00,
    "consumoCombustible": 0.35,
    "costoPorKm": 150.00,
    "disponible": true,
    "activo": true
  }
]
```

### 2. Obtener Camión por ID
**Rol requerido:** OPERADOR, TRANSPORTISTA

```
GET http://localhost:8080/api/camiones/1
Authorization: Bearer <token-operador>
```

**Respuesta esperada (200 OK):** Objeto camión

### 3. Listar Camiones Disponibles
**Rol requerido:** OPERADOR

```
GET http://localhost:8080/api/camiones/disponibles
Authorization: Bearer <token-operador>
```

**Respuesta esperada (200 OK):** Lista de camiones con estado DISPONIBLE

### 4. Crear Camión
**Rol requerido:** OPERADOR

```
POST http://localhost:8080/api/camiones
Authorization: Bearer <token-operador>
Content-Type: application/json

{
  "dominio": "ZZ999YY",
  "nombreTransportista": "Jorge Martínez",
  "telefono": "+541198765432",
  "capacidadPeso": 18.00,
  "capacidadVolumen": 50.00,
  "consumoCombustible": 0.40,
  "costoPorKm": 180.00
}
```

**Respuesta esperada (201 Created):**
```json
{
  "id": 5,
  "dominio": "ZZ999YY",
  "nombreTransportista": "Jorge Martínez",
  "telefono": "+541198765432",
  "capacidadPeso": 18.00,
  "capacidadVolumen": 50.00,
  "consumoCombustible": 0.40,
  "costoPorKm": 180.00,
  "disponible": true,
  "activo": true
}
```

**📝 Validaciones:**
- `dominio`: Obligatorio, máximo 20 caracteres
- `nombreTransportista`: Obligatorio, máximo 100 caracteres
- `telefono`: Obligatorio, 10-20 dígitos (puede incluir +)
- `capacidadPeso`: Obligatorio, mayor a 0 (en toneladas)
- `capacidadVolumen`: Obligatorio, mayor a 0 (en metros cúbicos)
- `consumoCombustible`: Obligatorio, mayor a 0 (litros por km)
- `costoPorKm`: Obligatorio, mayor a 0

### 5. Actualizar Camión
**Rol requerido:** OPERADOR

```
PUT http://localhost:8080/api/camiones/5
Authorization: Bearer <token-operador>
Content-Type: application/json

{
  "dominio": "ZZ999YY",
  "nombreTransportista": "Jorge Martínez Pérez",
  "telefono": "+541198765432",
  "capacidadPeso": 20.00,
  "capacidadVolumen": 55.00,
  "consumoCombustible": 0.38,
  "costoPorKm": 175.00
}
```

**Respuesta esperada (200 OK):** Camión actualizado

### 6. Cambiar Estado del Camión
**Rol requerido:** OPERADOR

**⚠️ NOTA:** Este endpoint NO existe en el modelo actual. Los camiones solo tienen el campo `disponible` (boolean) que se gestiona automáticamente. Para cambiar la disponibilidad, actualiza el camión completo con PUT.

### 7. Desactivar Camión
**Rol requerido:** OPERADOR

```
DELETE http://localhost:8080/api/camiones/5
Authorization: Bearer <token-operador>
```

**Respuesta esperada (204 No Content)**

---

## Tarifas Service

**Base URL:** `http://localhost:8080/api/tarifas`

### 1. Listar Todas las Tarifas
**Rol requerido:** OPERADOR

```
GET http://localhost:8080/api/tarifas
Authorization: Bearer <token-operador>
```

**Respuesta esperada (200 OK):**
```json
[
  {
    "id": 1,
    "tipo": "COSTO_KM_BASE",
    "descripcion": "Tarifa base por kilómetro recorrido",
    "valor": 100.00,
    "unidad": "$/km",
    "vigenciaDesde": "2025-01-01",
    "vigenciaHasta": null,
    "activo": true
  }
]
```

### 2. Obtener Tarifa por ID
**Rol requerido:** OPERADOR

```
GET http://localhost:8080/api/tarifas/1
Authorization: Bearer <token-operador>
```

**Respuesta esperada (200 OK):** Objeto tarifa

### 3. Obtener Tarifas Vigentes
**Rol requerido:** OPERADOR, CLIENTE

```
GET http://localhost:8080/api/tarifas/vigentes
Authorization: Bearer <token-operador>
```

**Respuesta esperada (200 OK):** Lista de tarifas activas sin fecha de fin

### 4. Crear Tarifa
**Rol requerido:** OPERADOR

```
POST http://localhost:8080/api/tarifas
Authorization: Bearer <token-operador>
Content-Type: application/json

{
  "tipo": "COSTO_COMBUSTIBLE_LITRO",
  "descripcion": "Precio del combustible por litro",
  "valor": 850.00,
  "unidad": "$/litro",
  "vigenciaDesde": "2025-02-01"
}
```

**Respuesta esperada (201 Created):**
```json
{
  "id": 3,
  "tipo": "COSTO_COMBUSTIBLE_LITRO",
  "descripcion": "Precio del combustible por litro",
  "valor": 850.00,
  "unidad": "$/litro",
  "vigenciaDesde": "2025-02-01",
  "vigenciaHasta": null,
  "activo": true
}
```

**📝 Tipos de Tarifa válidos:**
- `COSTO_KM_BASE`: Costo por kilómetro base
- `COSTO_COMBUSTIBLE_LITRO`: Precio del litro de combustible
- `CARGO_GESTION_FIJO`: Cargo fijo de gestión por tramo
- `ESTADIA_DEPOSITO`: Costo de estadía por día en depósito

**📝 Validaciones:**
- `tipo`: Obligatorio (enum)
- `descripcion`: Obligatoria, máximo 200 caracteres
- `valor`: Obligatorio, mayor a 0
- `unidad`: Opcional, máximo 20 caracteres
- `vigenciaDesde`: Obligatoria (formato: YYYY-MM-DD)
- `vigenciaHasta`: Opcional (formato: YYYY-MM-DD)

### 5. Actualizar Tarifa
**Rol requerido:** OPERADOR

```
PUT http://localhost:8080/api/tarifas/3
Authorization: Bearer <token-operador>
Content-Type: application/json

{
  "tipo": "COSTO_COMBUSTIBLE_LITRO",
  "descripcion": "Precio del combustible por litro (actualizado)",
  "valor": 900.00,
  "unidad": "$/litro",
  "vigenciaDesde": "2025-02-01"
}
```

**Respuesta esperada (200 OK):** Tarifa actualizada

### 6. Desactivar Tarifa
**Rol requerido:** OPERADOR

```
DELETE http://localhost:8080/api/tarifas/3
Authorization: Bearer <token-operador>
```

**Respuesta esperada (204 No Content)**

---

## Solicitudes Service

**Base URL:** `http://localhost:8080/api/solicitudes`

### 1. Listar Todas las Solicitudes
**Rol requerido:** OPERADOR

```
GET http://localhost:8080/api/solicitudes
Authorization: Bearer <token-operador>
```

**Respuesta esperada (200 OK):**
```json
[
  {
    "id": 1,
    "numero": "SOL-20251112001234",
    "estado": "PENDIENTE",
    "clienteId": 1,
    "contenedor": {
      "id": 1,
      "identificacion": "CONT-001",
      "peso": 5000.00,
      "volumen": 15.00,
      "direccionOrigen": "Juan de Garay 1755, Córdoba",
      "latitudOrigen": -31.403771,
      "longitudOrigen": -64.163894,
      "direccionDestino": "De los Toscanos 6581, Córdoba",
      "latitudDestino": -31.340196,
      "longitudDestino": -64.224319
    },
    "costoEstimado": null,
    "tiempoEstimadoHoras": null,
    "costoFinal": null,
    "tiempoRealHoras": null,
    "rutaId": null,
    "fechaCreacion": "2025-11-12T10:30:00"
  }
]
```

### 2. Obtener Solicitud por ID
**Rol requerido:** OPERADOR, CLIENTE (solo sus solicitudes)

```
GET http://localhost:8080/api/solicitudes/1
Authorization: Bearer <token-operador>
```

**Respuesta esperada (200 OK):** Objeto solicitud completo

### 3. Listar Solicitudes por Cliente
**Rol requerido:** OPERADOR, CLIENTE (solo sus propias solicitudes)

```
GET http://localhost:8080/api/solicitudes/cliente/1
Authorization: Bearer <token-operador>
```

**Respuesta esperada (200 OK):** Lista de solicitudes del cliente

### 4. Crear Solicitud
**Rol requerido:** OPERADOR, CLIENTE

```
POST http://localhost:8080/api/solicitudes
Authorization: Bearer <token-operador>
Content-Type: application/json

{
  "clienteId": 1,
  "contenedor": {
    "identificacion": "CONT-TEST-006",
    "peso": 5000.00,
    "volumen": 15.00,
    "direccionOrigen": "Juan de Garay 1755, Córdoba",
    "latitudOrigen": -31.403771,
    "longitudOrigen": -64.163894,
    "direccionDestino": "De los Toscanos 6581, Córdoba",
    "latitudDestino": -31.340196,
    "longitudDestino": -64.224319
  }
}
```

**Respuesta esperada (201 Created):**
```json
{
  "id": 6,
  "numero": "SOL-20251112153045",
  "estado": "PENDIENTE",
  "clienteId": 1,
  "contenedor": {
    "id": 6,
    "identificacion": "CONT-TEST-006",
    "peso": 5000.00,
    "volumen": 15.00,
    "direccionOrigen": "Juan de Garay 1755, Córdoba",
    "latitudOrigen": -31.403771,
    "longitudOrigen": -64.163894,
    "direccionDestino": "De los Toscanos 6581, Córdoba",
    "latitudDestino": -31.340196,
    "longitudDestino": -64.224319
  },
  "costoEstimado": null,
  "tiempoEstimadoHoras": null,
  "costoFinal": null,
  "tiempoRealHoras": null,
  "rutaId": null,
  "fechaCreacion": "2025-11-12T15:30:45"
}
```

### 5. Actualizar Solicitud
**Rol requerido:** OPERADOR

```
PUT http://localhost:8080/api/solicitudes/6
Authorization: Bearer <token-operador>
Content-Type: application/json

{
  "clienteId": 1,
  "contenedor": {
    "identificacion": "CONT-TEST-006-UPDATED",
    "peso": 6000,
    "volumen": 18,
    "direccionOrigen": "Juan de Garay 1755, Córdoba",
    "latitudOrigen": -31.403771,
    "longitudOrigen": -64.163894,
    "direccionDestino": "De los Toscanos 6581, Córdoba",
    "latitudDestino": -31.340196,
    "longitudDestino": -64.224319
  }
}
```

**Respuesta esperada (200 OK):** Solicitud actualizada

### 6. Cambiar Estado de Solicitud
**Rol requerido:** OPERADOR

```
PATCH http://localhost:8080/api/solicitudes/6/estado
Authorization: Bearer <token-operador>
Content-Type: application/json

{
  "estado": "CANCELADA"
}
```

**Estados válidos:** `PENDIENTE`, `EN_PROCESO`, `COMPLETADA`, `CANCELADA`

**Respuesta esperada (200 OK):** Solicitud con estado actualizado

### 7. Actualizar Costos y Tiempos (Endpoint de Sincronización)
**Rol requerido:** OPERADOR (uso interno del sistema)

```
PATCH http://localhost:8080/api/solicitudes/6/costos-tiempos
Authorization: Bearer <token-operador>
Content-Type: application/json

{
  "costoEstimado": 950.00,
  "tiempoEstimadoHoras": 1,
  "rutaId": 15
}
```

**Respuesta esperada (200 OK):** Solicitud con campos actualizados

**📝 Nota:** Este endpoint es llamado automáticamente por `rutas-service` cuando se calcula o finaliza una ruta.

---

## Rutas Service

**Base URL:** `http://localhost:8080/api/rutas`

### 1. Listar Todas las Rutas
**Rol requerido:** OPERADOR

```
GET http://localhost:8080/api/rutas
Authorization: Bearer <token-operador>
```

**Respuesta esperada (200 OK):**
```json
[
  {
    "id": 1,
    "solicitudId": 1,
    "cantidadTramos": 1,
    "cantidadDepositos": 0,
    "distanciaTotalKm": 9.50,
    "costoEstimado": 950.00,
    "tiempoEstimadoHoras": 1,
    "activa": true,
    "tramos": [
      {
        "id": 1,
        "orden": 1,
        "origenTipo": "ORIGEN",
        "destinoTipo": "DESTINO",
        "estado": "ESTIMADO",
        "distanciaKm": 9.50,
        "costoAproximado": 950.00,
        "camionId": null
      }
    ],
    "fechaCreacion": "2025-11-12T10:35:00"
  }
]
```

### 2. Obtener Ruta por ID
**Rol requerido:** OPERADOR, TRANSPORTISTA

```
GET http://localhost:8080/api/rutas/1
Authorization: Bearer <token-operador>
```

**Respuesta esperada (200 OK):** Objeto ruta completo con tramos

### 3. Obtener Ruta por Solicitud
**Rol requerido:** OPERADOR, CLIENTE (solo sus solicitudes)

```
GET http://localhost:8080/api/rutas/solicitud/1
Authorization: Bearer <token-operador>
```

**Respuesta esperada (200 OK):** Ruta asociada a la solicitud

### 4. Calcular Ruta Tentativa
**Rol requerido:** OPERADOR

```
POST http://localhost:8080/api/rutas/calcular
Authorization: Bearer <token-operador>
Content-Type: application/json

{
  "solicitudId": 6
}
```

**Respuesta esperada (201 Created):**
```json
{
  "id": 15,
  "solicitudId": 6,
  "cantidadTramos": 1,
  "cantidadDepositos": 0,
  "distanciaTotalKm": 9.50,
  "costoEstimado": 950.00,
  "tiempoEstimadoHoras": 1,
  "activa": true,
  "tramos": [
    {
      "id": 30,
      "orden": 1,
      "origenTipo": "ORIGEN",
      "origenDireccion": "Juan de Garay 1755, Córdoba",
      "origenLatitud": -31.403771,
      "origenLongitud": -64.163894,
      "destinoTipo": "DESTINO",
      "destinoDireccion": "De los Toscanos 6581, Córdoba",
      "destinoLatitud": -31.340196,
      "destinoLongitud": -64.224319,
      "tipoTramo": "ORIGEN_DESTINO",
      "estado": "ESTIMADO",
      "distanciaKm": 9.50,
      "costoAproximado": 950.00,
      "tiempoEstimadoHoras": 1,
      "camionId": null,
      "fechaHoraInicio": null,
      "fechaHoraFin": null
    }
  ],
  "fechaCreacion": "2025-11-12T15:35:00"
}
```

**📝 Nota:** Después de calcular la ruta, el sistema automáticamente actualiza la solicitud con `costoEstimado`, `tiempoEstimadoHoras` y `rutaId`.

### 5. Asignar Camión a un Tramo
**Rol requerido:** OPERADOR

```
POST http://localhost:8080/api/rutas/tramos/30/asignar-camion
Authorization: Bearer <token-operador>
Content-Type: application/json

{
  "camionId": 1
}
```

**Respuesta esperada (200 OK):**
```json
{
  "id": 15,
  "solicitudId": 6,
  "tramos": [
    {
      "id": 30,
      "estado": "ASIGNADO",
      "camionId": 1,
      ...
    }
  ]
}
```

**📝 Nota:** El estado del tramo cambia de `ESTIMADO` a `ASIGNADO` y el camión queda reservado.

### 6. Iniciar Tramo
**Rol requerido:** TRANSPORTISTA

```
POST http://localhost:8080/api/rutas/tramos/30/iniciar
Authorization: Bearer <token-transportista>
```

**Respuesta esperada (200 OK):**
```json
{
  "id": 30,
  "orden": 1,
  "estado": "INICIADO",
  "camionId": 1,
  "fechaHoraInicio": "2025-11-12T16:00:00",
  "fechaHoraFin": null,
  "distanciaKm": 9.50,
  "costoReal": null,
  ...
}
```

**📝 Nota:** El estado del tramo cambia de `ASIGNADO` a `INICIADO` y se registra la fecha/hora de inicio.

### 7. Finalizar Tramo
**Rol requerido:** TRANSPORTISTA

```
POST http://localhost:8080/api/rutas/tramos/30/finalizar
Authorization: Bearer <token-transportista>
```

**Respuesta esperada (200 OK):**
```json
{
  "id": 30,
  "orden": 1,
  "estado": "FINALIZADO",
  "camionId": 1,
  "fechaHoraInicio": "2025-11-12T16:00:00",
  "fechaHoraFin": "2025-11-12T17:15:00",
  "distanciaKm": 9.50,
  "costoReal": 950.00,
  "tiempoRealHoras": 1,
  ...
}
```

**📝 Nota:** 
- El estado del tramo cambia de `INICIADO` a `FINALIZADO`
- Se registra la fecha/hora de fin
- Se calcula el costo real y tiempo real
- El sistema automáticamente actualiza la solicitud con `costoFinal` y `tiempoRealHoras`

### 8. Desactivar Ruta
**Rol requerido:** OPERADOR

```
DELETE http://localhost:8080/api/rutas/15
Authorization: Bearer <token-operador>
```

**Respuesta esperada (204 No Content)**

---

## Caso de Prueba Completo End-to-End

### Escenario: Flujo Completo de Traslado de Contenedor

**Descripción:** Un cliente solicita el traslado de un contenedor desde Juan de Garay 1755 hasta De los Toscanos 6581 en Córdoba. El operador procesa la solicitud, calcula la ruta, asigna un camión, y el transportista ejecuta el traslado.

**Actores:**
- **Operador:** `operador1` (gestiona solicitudes y rutas)
- **Transportista:** `transportista1` (ejecuta el traslado)
- **Cliente:** `cliente1` (solicita el servicio)

---

### Preparación

#### 1. Obtener Tokens de Autenticación

**Token Operador:**
```
POST http://localhost:8180/realms/logistica-realm/protocol/openid-connect/token
Content-Type: application/x-www-form-urlencoded

client_id=logistica-api
client_secret=Tu9kPvMQyL6jYdBGZCN8xR2sHfWnJmKp
username=operador1
password=operador123
grant_type=password
```

**Respuesta:**
```json
{
  "access_token": "eyJhbGciOiJSUzI1NiIsInR5cCIgOiAiSldUIiwia2lkIiA6ICI...",
  "expires_in": 300,
  "token_type": "Bearer"
}
```

**📝 Guarda el `access_token` como `TOKEN_OPERADOR`**

**Token Transportista:**
```
POST http://localhost:8180/realms/logistica-realm/protocol/openid-connect/token
Content-Type: application/x-www-form-urlencoded

client_id=logistica-api
client_secret=Tu9kPvMQyL6jYdBGZCN8xR2sHfWnJmKp
username=transportista1
password=transportista123
grant_type=password
```

**📝 Guarda el `access_token` como `TOKEN_TRANSPORTISTA`**

---

### Paso 1: Verificar Recursos Disponibles

#### 1.1 Verificar Clientes

```
GET http://localhost:8080/api/clientes
Authorization: Bearer TOKEN_OPERADOR
```

**Resultado esperado:**
```json
[
  {
    "id": 1,
    "nombre": "Juan",
    "apellido": "Pérez",
    "email": "juan.perez@email.com",
    "activo": true
  }
]
```

**✅ Validación:** Cliente con `id: 1` existe y está activo.

#### 1.2 Verificar Camiones Disponibles

```
GET http://localhost:8080/api/camiones/disponibles
Authorization: Bearer TOKEN_OPERADOR
```

**Resultado esperado:**
```json
[
  {
    "id": 1,
    "patente": "AA123BB",
    "marca": "Mercedes-Benz",
    "modelo": "Actros 2651",
    "capacidadCarga": 15000,
    "estado": "DISPONIBLE",
    "activo": true
  }
]
```

**✅ Validación:** Camión con `id: 1` está disponible y su capacidad (15000 kg) es suficiente para nuestro contenedor (5000 kg).

#### 1.3 Verificar Tarifas Vigentes

```
GET http://localhost:8080/api/tarifas/vigentes
Authorization: Bearer TOKEN_OPERADOR
```

**Resultado esperado:**
```json
[
  {
    "id": 1,
    "tipoServicio": "ESTANDAR",
    "costoPorKm": 100.00,
    "costoFijoBase": 0.00,
    "fechaVigenciaInicio": "2025-01-01T00:00:00",
    "activa": true
  }
]
```

**✅ Validación:** Tarifa estándar activa: $100 por km.

---

### Paso 2: Crear Solicitud de Traslado

```
POST http://localhost:8080/api/solicitudes
Authorization: Bearer TOKEN_OPERADOR
Content-Type: application/json

{
  "clienteId": 1,
  "contenedor": {
    "identificacion": "CONT-E2E-TEST-001",
    "peso": 5000.00,
    "volumen": 15.00,
    "direccionOrigen": "Juan de Garay 1755, Córdoba",
    "latitudOrigen": -31.403771,
    "longitudOrigen": -64.163894,
    "direccionDestino": "De los Toscanos 6581, Córdoba",
    "latitudDestino": -31.340196,
    "longitudDestino": -64.224319
  }
}
```

**Resultado esperado (201 Created):**
```json
{
  "id": 10,
  "numero": "SOL-20251112160530",
  "estado": "PENDIENTE",
  "clienteId": 1,
  "contenedor": {
    "id": 10,
    "identificacion": "CONT-E2E-TEST-001",
    "peso": 5000,
    "volumen": 15,
    "direccionOrigen": "Juan de Garay 1755, Córdoba",
    "latitudOrigen": -31.403771,
    "longitudOrigen": -64.163894,
    "direccionDestino": "De los Toscanos 6581, Córdoba",
    "latitudDestino": -31.340196,
    "longitudDestino": -64.224319
  },
  "costoEstimado": null,
  "tiempoEstimadoHoras": null,
  "costoFinal": null,
  "tiempoRealHoras": null,
  "rutaId": null,
  "fechaCreacion": "2025-11-12T16:05:30"
}
```

**✅ Validaciones:**
- Solicitud creada con ID: `10`
- Estado inicial: `PENDIENTE`
- Campos de costo y ruta en `null` (aún no calculados)
- Contenedor asociado correctamente

**📝 Guarda el `id: 10` como `SOLICITUD_ID`**

---

### Paso 3: Calcular Ruta Tentativa

```
POST http://localhost:8080/api/rutas/calcular
Authorization: Bearer TOKEN_OPERADOR
Content-Type: application/json

{
  "solicitudId": 10
}
```

**Resultado esperado (201 Created):**
```json
{
  "id": 20,
  "solicitudId": 10,
  "cantidadTramos": 1,
  "cantidadDepositos": 0,
  "distanciaTotalKm": 9.50,
  "costoEstimado": 950.00,
  "tiempoEstimadoHoras": 1,
  "activa": true,
  "tramos": [
    {
      "id": 40,
      "orden": 1,
      "origenTipo": "ORIGEN",
      "origenDireccion": "Juan de Garay 1755, Córdoba",
      "origenLatitud": -31.403771,
      "origenLongitud": -64.163894,
      "destinoTipo": "DESTINO",
      "destinoDireccion": "De los Toscanos 6581, Córdoba",
      "destinoLatitud": -31.340196,
      "destinoLongitud": -64.224319,
      "tipoTramo": "ORIGEN_DESTINO",
      "estado": "ESTIMADO",
      "distanciaKm": 9.50,
      "costoAproximado": 950.00,
      "tiempoEstimadoHoras": 1,
      "camionId": null,
      "fechaHoraInicio": null,
      "fechaHoraFin": null,
      "costoReal": null
    }
  ],
  "fechaCreacion": "2025-11-12T16:06:15"
}
```

**✅ Validaciones:**
- Ruta creada con ID: `20`
- Distancia calculada: `9.50 km`
- Costo estimado: `$950.00` (9.50 km × $100/km)
- Tiempo estimado: `1 hora`
- Tramo único (origen → destino directo, sin depósitos intermedios)
- Estado del tramo: `ESTIMADO`
- Camión aún no asignado

**📝 Guarda:**
- `id: 20` como `RUTA_ID`
- `tramos[0].id: 40` como `TRAMO_ID`

#### 3.1 Verificar Sincronización con Solicitud

```
GET http://localhost:8080/api/solicitudes/10
Authorization: Bearer TOKEN_OPERADOR
```

**Resultado esperado (200 OK):**
```json
{
  "id": 10,
  "numero": "SOL-20251112160530",
  "estado": "PENDIENTE",
  "clienteId": 1,
  "contenedor": { ... },
  "costoEstimado": 950.00,
  "tiempoEstimadoHoras": 1,
  "costoFinal": null,
  "tiempoRealHoras": null,
  "rutaId": 20,
  "fechaCreacion": "2025-11-12T16:05:30"
}
```

**✅ Validaciones:**
- `costoEstimado`: `950.00` (✅ sincronizado desde ruta)
- `tiempoEstimadoHoras`: `1` (✅ sincronizado desde ruta)
- `rutaId`: `20` (✅ asociada a la ruta)
- `costoFinal` y `tiempoRealHoras`: aún `null` (se actualizarán al finalizar)

---

### Paso 4: Asignar Camión al Tramo

```
POST http://localhost:8080/api/rutas/tramos/40/asignar-camion
Authorization: Bearer TOKEN_OPERADOR
Content-Type: application/json

{
  "camionId": 1
}
```

**Resultado esperado (200 OK):**
```json
{
  "id": 20,
  "solicitudId": 10,
  "cantidadTramos": 1,
  "cantidadDepositos": 0,
  "distanciaTotalKm": 9.50,
  "costoEstimado": 950.00,
  "tiempoEstimadoHoras": 1,
  "activa": true,
  "tramos": [
    {
      "id": 40,
      "orden": 1,
      "estado": "ASIGNADO",
      "distanciaKm": 9.50,
      "costoAproximado": 950.00,
      "tiempoEstimadoHoras": 1,
      "camionId": 1,
      "fechaHoraInicio": null,
      "fechaHoraFin": null,
      ...
    }
  ],
  "fechaCreacion": "2025-11-12T16:06:15"
}
```

**✅ Validaciones:**
- Estado del tramo cambió: `ESTIMADO` → `ASIGNADO`
- `camionId`: `1` (camión Mercedes-Benz AA123BB asignado)

#### 4.1 Verificar Estado del Camión

```
GET http://localhost:8080/api/camiones/1
Authorization: Bearer TOKEN_OPERADOR
```

**Resultado esperado (200 OK):**
```json
{
  "id": 1,
  "patente": "AA123BB",
  "marca": "Mercedes-Benz",
  "modelo": "Actros 2651",
  "capacidadCarga": 15000,
  "estado": "EN_RUTA",
  "activo": true
}
```

**✅ Validación:** Estado del camión cambió: `DISPONIBLE` → `EN_RUTA`

---

### Paso 5: Iniciar Tramo (Transportista)

**⚠️ IMPORTANTE:** Usar el token del transportista para esta operación.

```
POST http://localhost:8080/api/rutas/tramos/40/iniciar
Authorization: Bearer TOKEN_TRANSPORTISTA
```

**Resultado esperado (200 OK):**
```json
{
  "id": 40,
  "orden": 1,
  "origenTipo": "ORIGEN",
  "origenDireccion": "Juan de Garay 1755, Córdoba",
  "destinoTipo": "DESTINO",
  "destinoDireccion": "De los Toscanos 6581, Córdoba",
  "tipoTramo": "ORIGEN_DESTINO",
  "estado": "INICIADO",
  "distanciaKm": 9.50,
  "costoAproximado": 950.00,
  "tiempoEstimadoHoras": 1,
  "camionId": 1,
  "fechaHoraInicio": "2025-11-12T16:10:00",
  "fechaHoraFin": null,
  "costoReal": null,
  "tiempoRealHoras": null
}
```

**✅ Validaciones:**
- Estado del tramo cambió: `ASIGNADO` → `INICIADO`
- `fechaHoraInicio`: registrada con timestamp actual
- `fechaHoraFin`: aún `null`
- `costoReal` y `tiempoRealHoras`: aún `null`

---

### Paso 6: Simular Tiempo de Transporte

**📝 Nota:** En un sistema real, aquí pasaría tiempo mientras el camión viaja. Para esta prueba, procedemos inmediatamente a finalizar.

---

### Paso 7: Finalizar Tramo (Transportista)

```
POST http://localhost:8080/api/rutas/tramos/40/finalizar
Authorization: Bearer TOKEN_TRANSPORTISTA
```

**Resultado esperado (200 OK):**
```json
{
  "id": 40,
  "orden": 1,
  "origenTipo": "ORIGEN",
  "origenDireccion": "Juan de Garay 1755, Córdoba",
  "destinoTipo": "DESTINO",
  "destinoDireccion": "De los Toscanos 6581, Córdoba",
  "tipoTramo": "ORIGEN_DESTINO",
  "estado": "FINALIZADO",
  "distanciaKm": 9.50,
  "costoAproximado": 950.00,
  "tiempoEstimadoHoras": 1,
  "camionId": 1,
  "fechaHoraInicio": "2025-11-12T16:10:00",
  "fechaHoraFin": "2025-11-12T17:25:00",
  "costoReal": 950.00,
  "tiempoRealHoras": 1
}
```

**✅ Validaciones:**
- Estado del tramo cambió: `INICIADO` → `FINALIZADO`
- `fechaHoraFin`: registrada con timestamp actual
- `costoReal`: `950.00` (calculado: 9.50 km × $100/km)
- `tiempoRealHoras`: `1` (diferencia entre inicio y fin)

#### 7.1 Verificar Sincronización Final con Solicitud

```
GET http://localhost:8080/api/solicitudes/10
Authorization: Bearer TOKEN_OPERADOR
```

**Resultado esperado (200 OK):**
```json
{
  "id": 10,
  "numero": "SOL-20251112160530",
  "estado": "PENDIENTE",
  "clienteId": 1,
  "contenedor": {
    "id": 10,
    "identificacion": "CONT-E2E-TEST-001",
    "peso": 5000,
    "volumen": 15,
    "direccionOrigen": "Juan de Garay 1755, Córdoba",
    "latitudOrigen": -31.403771,
    "longitudOrigen": -64.163894,
    "direccionDestino": "De los Toscanos 6581, Córdoba",
    "latitudDestino": -31.340196,
    "longitudDestino": -64.224319
  },
  "costoEstimado": 950.00,
  "tiempoEstimadoHoras": 1,
  "costoFinal": 950.00,
  "tiempoRealHoras": 1,
  "rutaId": 20,
  "fechaCreacion": "2025-11-12T16:05:30"
}
```

**✅ Validaciones Finales:**
- `costoEstimado`: `950.00` (✅ calculado en Paso 3)
- `tiempoEstimadoHoras`: `1` (✅ calculado en Paso 3)
- `costoFinal`: `950.00` (✅ sincronizado desde tramo finalizado)
- `tiempoRealHoras`: `1` (✅ sincronizado desde tramo finalizado)
- `rutaId`: `20` (✅ asociada correctamente)

**🎉 El sistema mantiene TODOS los datos sincronizados automáticamente**

#### 7.2 Verificar Estado Final del Camión

```
GET http://localhost:8080/api/camiones/1
Authorization: Bearer TOKEN_OPERADOR
```

**Resultado esperado (200 OK):**
```json
{
  "id": 1,
  "patente": "AA123BB",
  "marca": "Mercedes-Benz",
  "modelo": "Actros 2651",
  "capacidadCarga": 15000,
  "estado": "DISPONIBLE",
  "activo": true
}
```

**✅ Validación:** Estado del camión volvió a `DISPONIBLE` (liberado después del tramo)

---

### Paso 8: Verificar Ruta Completa

```
GET http://localhost:8080/api/rutas/20
Authorization: Bearer TOKEN_OPERADOR
```

**Resultado esperado (200 OK):**
```json
{
  "id": 20,
  "solicitudId": 10,
  "cantidadTramos": 1,
  "cantidadDepositos": 0,
  "distanciaTotalKm": 9.50,
  "costoEstimado": 950.00,
  "tiempoEstimadoHoras": 1,
  "activa": true,
  "tramos": [
    {
      "id": 40,
      "orden": 1,
      "origenTipo": "ORIGEN",
      "origenDireccion": "Juan de Garay 1755, Córdoba",
      "origenLatitud": -31.403771,
      "origenLongitud": -64.163894,
      "destinoTipo": "DESTINO",
      "destinoDireccion": "De los Toscanos 6581, Córdoba",
      "destinoLatitud": -31.340196,
      "destinoLongitud": -64.224319,
      "tipoTramo": "ORIGEN_DESTINO",
      "estado": "FINALIZADO",
      "distanciaKm": 9.50,
      "costoAproximado": 950.00,
      "tiempoEstimadoHoras": 1,
      "camionId": 1,
      "fechaHoraInicio": "2025-11-12T16:10:00",
      "fechaHoraFin": "2025-11-12T17:25:00",
      "costoReal": 950.00,
      "tiempoRealHoras": 1
    }
  ],
  "fechaCreacion": "2025-11-12T16:06:15"
}
```

**✅ Validación:** Ruta completa con todos los datos históricos del tramo.

---

### Resumen del Flujo Ejecutado

| Paso | Endpoint | Actor | Estado Inicial | Estado Final | Duración |
|------|----------|-------|----------------|--------------|----------|
| 1 | POST /solicitudes | Operador | - | PENDIENTE (solicitud creada) | - |
| 2 | POST /rutas/calcular | Operador | PENDIENTE | PENDIENTE (ruta calculada) | - |
| 3 | POST /tramos/{id}/asignar-camion | Operador | ESTIMADO | ASIGNADO | - |
| 4 | POST /tramos/{id}/iniciar | Transportista | ASIGNADO | INICIADO | - |
| 5 | POST /tramos/{id}/finalizar | Transportista | INICIADO | FINALIZADO | 1h 15min |
| 6 | GET /solicitudes/{id} | Operador | - | Datos sincronizados | - |

---

### Datos de Prueba Utilizados

| Recurso | ID | Detalle |
|---------|-----|---------|
| Cliente | 1 | Juan Pérez |
| Camión | 1 | Mercedes-Benz AA123BB |
| Tarifa | 1 | $100/km (estándar) |
| Solicitud | 10 | CONT-E2E-TEST-001 |
| Ruta | 20 | 9.50 km, $950.00 |
| Tramo | 40 | Origen → Destino |

---

### Cálculos Verificados

**Distancia:**
- Origen: Juan de Garay 1755 (-31.403771, -64.163894)
- Destino: De los Toscanos 6581 (-31.340196, -64.224319)
- **Distancia calculada:** 9.50 km (Haversine o Google Maps API)

**Costo:**
- Tarifa: $100/km
- Distancia: 9.50 km
- **Costo total:** 9.50 × 100 = **$950.00**

**Tiempo:**
- Tiempo estimado: 1 hora
- Inicio: 16:10:00
- Fin: 17:25:00
- **Tiempo real:** 1 hora 15 minutos ≈ **1 hora** (redondeado)

---

### Puntos Clave de Sincronización

1. **Después de calcular ruta (Paso 3):**
   - Solicitud actualiza: `costoEstimado`, `tiempoEstimadoHoras`, `rutaId`

2. **Después de finalizar tramo (Paso 7):**
   - Solicitud actualiza: `costoFinal`, `tiempoRealHoras`

3. **Estados del camión:**
   - `DISPONIBLE` → `EN_RUTA` (al asignar)
   - `EN_RUTA` → `DISPONIBLE` (al finalizar)

4. **Estados del tramo:**
   - `ESTIMADO` → `ASIGNADO` → `INICIADO` → `FINALIZADO`

---

### Errores Comunes y Soluciones

| Error | Causa | Solución |
|-------|-------|----------|
| 403 Forbidden en /tramos/iniciar | Token de operador usado | Usar token de transportista |
| 400 "Camión no disponible" | Camión ya asignado | Verificar estado con GET /camiones/{id} |
| 404 "Tramo no encontrado" | ID de tramo incorrecto | Verificar ID en respuesta de calcular ruta |
| Solicitud sin `costoEstimado` | Error en sincronización | Verificar logs de rutas-service |
| Solicitud sin `costoFinal` | Error en sincronización | Verificar logs al finalizar tramo |

---

## Conclusión

Esta guía proporciona:

1. **Testing completo de todos los endpoints** de cada microservicio
2. **Caso de prueba end-to-end** con datos reales y resultados esperados
3. **Validaciones en cada paso** para verificar el comportamiento correcto
4. **Documentación de sincronización** entre servicios
5. **Troubleshooting** de errores comunes

**📝 Recomendaciones:**
- Ejecutar el caso E2E completo al menos una vez para validar el sistema
- Probar con diferentes coordenadas para verificar el cálculo de distancias
- Verificar logs de cada servicio durante la ejecución
- Usar diferentes tokens (operador, cliente, transportista) para validar seguridad

---

## Apéndice: Resumen de Validaciones por Campo

### Cliente

| Campo | Tipo | Obligatorio | Validación |
|-------|------|-------------|------------|
| nombre | String | ✅ | 2-100 caracteres |
| apellido | String | ✅ | 2-100 caracteres |
| dni | String | ✅ | 7-8 dígitos (solo números) |
| email | String | ✅ | Formato email válido, max 150 chars |
| telefono | String | ✅ | 8-20 caracteres, permite +, -, (), espacios |
| direccion | String | ❌ | Max 200 caracteres |
| ciudad | String | ❌ | Max 100 caracteres |
| provincia | String | ❌ | Max 50 caracteres |
| codigoPostal | String | ❌ | Max 10 caracteres |

### Depósito

| Campo | Tipo | Obligatorio | Validación |
|-------|------|-------------|------------|
| nombre | String | ✅ | Max 100 caracteres |
| direccion | String | ✅ | Max 255 caracteres |
| latitud | Double | ✅ | Entre -90 y 90 |
| longitud | Double | ✅ | Entre -180 y 180 |
| costoDiarioEstadia | BigDecimal | ✅ | Mayor a 0 |
| capacidadMaxima | Integer | ❌ | Mínimo 1 si se provee |

### Camión

| Campo | Tipo | Obligatorio | Validación |
|-------|------|-------------|------------|
| dominio | String | ✅ | Max 20 caracteres |
| nombreTransportista | String | ✅ | Max 100 caracteres |
| telefono | String | ✅ | 10-20 dígitos, puede incluir + |
| capacidadPeso | BigDecimal | ✅ | Mayor a 0 (toneladas) |
| capacidadVolumen | BigDecimal | ✅ | Mayor a 0 (m³) |
| consumoCombustible | BigDecimal | ✅ | Mayor a 0 (litros/km) |
| costoPorKm | BigDecimal | ✅ | Mayor a 0 |

### Tarifa

| Campo | Tipo | Obligatorio | Validación |
|-------|------|-------------|------------|
| tipo | Enum | ✅ | COSTO_KM_BASE, COSTO_COMBUSTIBLE_LITRO, CARGO_GESTION_FIJO, ESTADIA_DEPOSITO |
| descripcion | String | ✅ | Max 200 caracteres |
| valor | BigDecimal | ✅ | Mayor a 0 |
| unidad | String | ❌ | Max 20 caracteres |
| vigenciaDesde | LocalDate | ✅ | Formato: YYYY-MM-DD |
| vigenciaHasta | LocalDate | ❌ | Formato: YYYY-MM-DD |

### Solicitud

| Campo | Tipo | Obligatorio | Validación |
|-------|------|-------------|------------|
| clienteId | Long | ✅ | ID válido de cliente existente |
| contenedor | ContenedorDTO | ✅ | Objeto completo |

### Contenedor

| Campo | Tipo | Obligatorio | Validación |
|-------|------|-------------|------------|
| identificacion | String | ✅ | Max 50 caracteres |
| peso | BigDecimal | ✅ | Mayor a 0 (kg) |
| volumen | BigDecimal | ✅ | Mayor a 0 (m³) |
| direccionOrigen | String | ✅ | Max 500 caracteres |
| latitudOrigen | Double | ✅ | Entre -90 y 90 |
| longitudOrigen | Double | ✅ | Entre -180 y 180 |
| direccionDestino | String | ✅ | Max 500 caracteres |
| latitudDestino | Double | ✅ | Entre -90 y 90 |
| longitudDestino | Double | ✅ | Entre -180 y 180 |

---

¡Éxito con las pruebas! 🚀

