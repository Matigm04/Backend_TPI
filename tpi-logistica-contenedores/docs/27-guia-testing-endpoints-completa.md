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

### 1. Listar Todos los Clientes  (Funciona)
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

### 2. Obtener Cliente por ID  (Funciona)
**Rol requerido:** OPERADOR, CLIENTE (solo su propio ID)

```
GET http://localhost:8080/api/clientes/1
Authorization: Bearer <token-operador>
```

**Respuesta esperada (200 OK):** Objeto cliente

### 3. Crear Cliente (FUnciona pero a la primera vez me da error pero si se crea)
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

### 4. Actualizar Cliente (Fuunciona)
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

### 5. Desactivar Cliente  (Funciona)
**Rol requerido:** Admin

```
DELETE http://localhost:8080/api/clientes/5
Authorization: Bearer <token-operador>
```

**Respuesta esperada (204 No Content)**

---

## Depósitos Service

**Base URL:** `http://localhost:8080/api/depositos`

### 1. Listar Todos los Depósitos    (Funciona)
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
    "costoDiarioEstadia": 5000.00,
    "capacidadMaxima": 100,
    "contenedoresActuales": 25,
    "horarioApertura": "08:00:00",
    "horarioCierre": "18:00:00",
    "observaciones": "Requiere cita previa para descarga",
    "activo": true,
    "fechaCreacion": "2025-01-01T10:00:00",
    "fechaActualizacion": "2025-01-15T14:30:00"
  }
]
```

### 2. Obtener Depósito por ID   (Funciona)
**Rol requerido:** OPERADOR, TRANSPORTISTA

```
GET http://localhost:8080/api/depositos/1
Authorization: Bearer <token-operador>
```

**Respuesta esperada (200 OK):** Objeto depósito

### 3. Buscar Depósitos Cercanos     (Funciona)
**Rol requerido:** OPERADOR, TRANSPORTISTA

```
GET http://localhost:8080/api/depositos/cercanos?latitud=-31.403771&longitud=-64.163894&radioKm=20
Authorization: Bearer <token-operador>
```

**Parámetros:**
- `latitud`: Latitud del punto de referencia (requerido)
- `longitud`: Longitud del punto de referencia (requerido)
- `radioKm`: Radio de búsqueda en km (requerido)

**Respuesta esperada (200 OK):**
```json
[
  {
    "id": 1,
    "nombre": "Depósito Central Córdoba",
    "direccion": "Av. Circunvalación Km 5, Córdoba",
    "latitud": -31.3937,
    "longitud": -64.2324,
    "costoDiarioEstadia": 5000.00,
    "capacidadMaxima": 100,
    "contenedoresActuales": 25,
    "horarioApertura": "08:00:00",
    "horarioCierre": "18:00:00",
    "observaciones": "Requiere cita previa para descarga",
    "distanciaKm": 3.45,
    "activo": true
  }
]
```

### 4. Crear Depósito   (Funciona)
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
  "capacidadMaxima": 80,
  "contenedoresActuales": 0,
  "horarioApertura": "07:00:00",
  "horarioCierre": "19:00:00",
  "observaciones": "Acceso por Ruta 9, portón principal"
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
- `contenedoresActuales`: Opcional, mínimo 0, por defecto 0
- `horarioApertura`: Opcional, formato TIME (HH:MM:SS)
- `horarioCierre`: Opcional, formato TIME (HH:MM:SS)
- `observaciones`: Opcional, máximo 1000 caracteres

### 5. Actualizar Depósito  (Funciona sin token y con token)
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
  "capacidadMaxima": 120,
  "contenedoresActuales": 30,
  "horarioApertura": "06:00:00",
  "horarioCierre": "20:00:00",
  "observaciones": "Horario extendido - Cita previa obligatoria"
}
```

**Respuesta esperada (200 OK):** Depósito actualizado

### 6. Desactivar Depósito (Le damos un valor false en el activo) (Funciona)
**Rol requerido:** OPERADOR

```
DELETE http://localhost:8080/api/depositos/1
Authorization: Bearer <token-operador>
```

**Respuesta esperada (204 No Content)**

---

## Camiones Service

**Base URL:** `http://localhost:8080/api/camiones`

### 1. Listar Todos los Transportistas (Funciona)
**Rol requerido:** OPERADOR, TRANSPORTISTA

```
GET http://localhost:8080/api/transportistas
Authorization: Bearer <token-operador>
```

**Respuesta esperada (200 OK):**
```json
[
  {
    "id": 1,
    "nombre": "Carlos",
    "apellido": "Rodríguez",
    "telefono": "+541112345678",
    "activo": true
  }
]
```

### 2. Obtener Transportista por ID (Funciona)
**Rol requerido:** OPERADOR, TRANSPORTISTA

```
GET http://localhost:8080/api/transportistas/1
Authorization: Bearer <token-operador>
```

**Respuesta esperada (200 OK):** Objeto transportista

### 3. Buscar Transportista por Nombre  (Funciona)
**Rol requerido:** OPERADOR, TRANSPORTISTA

```
GET http://localhost:8080/api/transportistas/buscar?nombre=Carlos
Authorization: Bearer <token-operador>
```

**Respuesta esperada (200 OK):** Lista de transportistas que coincidan con el nombre o apellido

### 4. Crear Transportista  (Funciona)
**Rol requerido:** OPERADOR

```
POST http://localhost:8080/api/transportistas
Authorization: Bearer <token-operador>
Content-Type: application/json

{
  "nombre": "Jorge",
  "apellido": "Martínez",
  "telefono": "+541198765432"
}
```

**Respuesta esperada (201 Created):**
```json
{
  "id": 2,
  "nombre": "Jorge",
  "apellido": "Martínez",
  "telefono": "+541198765432",
  "activo": true
}
```

**📝 Validaciones:**
- `nombre`: Obligatorio, entre 2 y 100 caracteres
- `apellido`: Obligatorio, entre 2 y 100 caracteres
- `telefono`: Obligatorio, 10-20 dígitos, puede incluir +

### 5. Actualizar Transportista  (Funciona)
**Rol requerido:** OPERADOR

```
PUT http://localhost:8080/api/transportistas/2
Authorization: Bearer <token-operador>
Content-Type: application/json

{
  "nombre": "Jorge Luis",
  "apellido": "Martínez Pérez",
  "telefono": "+541198765432"
}
```

**Respuesta esperada (200 OK):** Transportista actualizado

### 6. Eliminar Transportista  (Funciona)
**Rol requerido:** OPERADOR

```
DELETE http://localhost:8080/api/transportistas/2
Authorization: Bearer <token-operador>
```

**Respuesta esperada (204 No Content)**

---

### 7. Listar Todos los Camiones (Funciona)
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
    "marca": "Mercedes-Benz",
    "modelo": "Actros 2651",
    "año": 2020,
    "transportista": {
      "id": 1,
      "nombre": "Carlos",
      "apellido": "Rodríguez",
      "telefono": "+541112345678",
      "activo": true
    },
    "capacidadPeso": 15.00,
    "capacidadVolumen": 40.00,
    "consumoCombustible": 0.35,
    "costoPorKm": 150.00,
    "disponible": true,
    "activo": true
  }
]
```

### 8. Obtener Camión por ID (Funciona)
**Rol requerido:** OPERADOR, TRANSPORTISTA

```
GET http://localhost:8080/api/camiones/1
Authorization: Bearer <token-operador>
```

**Respuesta esperada (200 OK):** Objeto camión

### 9. Listar Camiones Disponibles  (Funciona)
**Rol requerido:** OPERADOR

```
GET http://localhost:8080/api/camiones/disponibles
Authorization: Bearer <token-operador>
```

**Respuesta esperada (200 OK):** Lista de camiones con estado DISPONIBLE

### 10. Crear Camión  (Funciona)
**Rol requerido:** OPERADOR

**⚠️ REQUISITO PREVIO:** Debe existir un transportista creado. Primero crear transportista con POST /api/transportistas

```
POST http://localhost:8080/api/camiones
Authorization: Bearer <token-operador>
Content-Type: application/json

{
  "dominio": "ZZ999YY",
  "marca": "Scania",
  "modelo": "R450",
  "año": 2022,
  "transportistaId": 2,
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
  "marca": "Scania",
  "modelo": "R450",
  "año": 2022,
  "transportista": {
    "id": 2,
    "nombre": "Jorge",
    "apellido": "Martínez",
    "telefono": "+541198765432",
    "activo": true
  },
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
- `marca`: Obligatorio, máximo 50 caracteres
- `modelo`: Obligatorio, máximo 50 caracteres
- `año`: Obligatorio, entre 1900 y 2100
- `transportistaId`: Obligatorio, debe existir un transportista con ese ID
- `capacidadPeso`: Obligatorio, mayor a 0 (en toneladas)
- `capacidadVolumen`: Obligatorio, mayor a 0 (en metros cúbicos)
- `consumoCombustible`: Obligatorio, mayor a 0 (litros por km)
- `costoPorKm`: Obligatorio, mayor a 0

### 11. Actualizar Camión (Funciona)
**Rol requerido:** OPERADOR

```
PUT http://localhost:8080/api/camiones/5
Authorization: Bearer <token-operador>
Content-Type: application/json

{
  "dominio": "ZZ999YY",
  "marca": "Scania",
  "modelo": "R500",
  "año": 2023,
  "transportistaId": 2,
  "capacidadPeso": 20.00,
  "capacidadVolumen": 55.00,
  "consumoCombustible": 0.38,
  "costoPorKm": 175.00
}
```

**Respuesta esperada (200 OK):** Camión actualizado

### 12. Desactivar Camión (Funciona)
**Rol requerido:** OPERADOR

```
DELETE http://localhost:8080/api/camiones/5
Authorization: Bearer <token-operador>
```

**Respuesta esperada (204 No Content)**

---

## Tarifas Service

**Base URL:** `http://localhost:8080/api/tarifas`

### 1. Listar Todas las Tarifas  (Funciona)
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
    "rangoPesoMinKg": 0.00,
    "rangoPesoMaxKg": 5000.00,
    "rangoVolumenMinM3": 0.00,
    "rangoVolumenMaxM3": 20.00,
    "vigenciaDesde": "2025-01-01",
    "vigenciaHasta": null,
    "activo": true
  }
]
```

### 2. Obtener Tarifa por ID (Funciona)
**Rol requerido:** OPERADOR

```
GET http://localhost:8080/api/tarifas/1
Authorization: Bearer <token-operador>
```

**Respuesta esperada (200 OK):** Objeto tarifa

### 3. Obtener Tarifas Vigentes  (Funciona)
**Rol requerido:** OPERADOR, CLIENTE

```
GET http://localhost:8080/api/tarifas/vigentes
Authorization: Bearer <token-operador>
```

**Respuesta esperada (200 OK):** Lista de tarifas activas sin fecha de fin

### 4. Crear Tarifa  (Funciona)
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
  "rangoPesoMinKg": null,
  "rangoPesoMaxKg": null,
  "rangoVolumenMinM3": null,
  "rangoVolumenMaxM3": null,
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
  "rangoPesoMinKg": null,
  "rangoPesoMaxKg": null,
  "rangoVolumenMinM3": null,
  "rangoVolumenMaxM3": null,
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
- `rangoPesoMinKg`: Opcional, mínimo 0
- `rangoPesoMaxKg`: Opcional, mínimo 0
- `rangoVolumenMinM3`: Opcional, mínimo 0
- `rangoVolumenMaxM3`: Opcional, mínimo 0
- `vigenciaDesde`: Obligatoria (formato: YYYY-MM-DD)
- `vigenciaHasta`: Opcional (formato: YYYY-MM-DD)

### 5. Actualizar Tarifa (Funciona)
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
  "rangoPesoMinKg": null,
  "rangoPesoMaxKg": null,
  "rangoVolumenMinM3": null,
  "rangoVolumenMaxM3": null,
  "vigenciaDesde": "2025-02-01"
}
```

**Respuesta esperada (200 OK):** Tarifa actualizada

### 6. Desactivar Tarifa (Funciona)
**Rol requerido:** Admin

```
DELETE http://localhost:8080/api/tarifas/3
Authorization: Bearer <token-operador>
```

**Respuesta esperada (204 No Content)**

---

## Solicitudes Service

**Base URL:** `http://localhost:8080/api/solicitudes`

### 1. Listar Todas las Solicitudes  (Funciona)
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

### 2. Obtener Solicitud por ID   (Funciona)
**Rol requerido:** OPERADOR, CLIENTE (solo sus solicitudes)

```
GET http://localhost:8080/api/solicitudes/1
Authorization: Bearer <token-operador>
```

**Respuesta esperada (200 OK):** Objeto solicitud completo

### 3. Listar Solicitudes por Cliente  (Funciona)
**Rol requerido:** OPERADOR, CLIENTE (solo sus propias solicitudes)

```
GET http://localhost:8080/api/solicitudes/cliente/1
Authorization: Bearer <token-operador>
```

**Respuesta esperada (200 OK):** Lista de solicitudes del cliente

### 4. Crear Solicitud   (Funciona)
**Rol requerido:** OPERADOR, CLIENTE

```
POST http://localhost:8080/api/solicitudes
Authorization: Bearer <token-operador>
Content-Type: application/json

{
  "clienteId": 1,
  "ubicacionOrigen": "Juan de Garay 1755, Córdoba",
  "ubicacionDestino": "De los Toscanos 6581, Córdoba",
  "fechaProgramada": "2025-12-15",
  "observaciones": "Carga frágil, manejar con cuidado",
  "tarifaId": 1,
  "contenedor": {
    "identificacion": "CONT-TEST-006",
    "peso": 5000.00,
    "volumen": 15.00,
    "largoM": 6.00,
    "anchoM": 2.50,
    "altoM": 2.60,
    "estado": "DISPONIBLE",
    "descripcion": "Contenedor refrigerado de 20 pies",
    "clienteId": 1,
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
  "estado": "BORRADOR",
  "clienteId": 1,
  "ubicacionOrigen": "Juan de Garay 1755, Córdoba",
  "ubicacionDestino": "De los Toscanos 6581, Córdoba",
  "fechaProgramada": "2025-12-15",
  "observaciones": "Carga frágil, manejar con cuidado",
  "tarifaId": 1,
  "contenedor": {
    "id": 6,
    "identificacion": "CONT-TEST-006",
    "peso": 5000.00,
    "volumen": 15.00,
    "largoM": 6.00,
    "anchoM": 2.50,
    "altoM": 2.60,
    "estado": "DISPONIBLE",
    "descripcion": "Contenedor refrigerado de 20 pies",
    "clienteId": 1,
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
  "activo": true,
  "fechaCreacion": "2025-11-12T15:30:45",
  "fechaActualizacion": "2025-11-12T15:30:45"
}
```

### 5. Cancelar Solicitud (Funciona)
**Rol requerido:** OPERADOR

**⚠️ NOTA IMPORTANTE:** Las solicitudes NO se pueden actualizar una vez creadas. Si necesitas cambiar los datos del contenedor, debes:
1. Crear una nueva solicitud con los datos correctos
2. Cancelar la solicitud anterior usando el endpoint de cambio de estado

Para cancelar una solicitud:

```
PATCH http://localhost:8080/api/solicitudes/6/estado?estado=CANCELADA
Authorization: Bearer <token-operador>
```

**Respuesta esperada (200 OK):** Solicitud con estado `CANCELADA`

### 6. Cambiar Estado de Solicitud (Funciona)
**Rol requerido:** OPERADOR

```
PATCH http://localhost:8080/api/solicitudes/6/estado?estado=EN_PROCESO
Authorization: Bearer <token-operador>
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

## Historial Estado Contenedor

**Base URL:** `http://localhost:8080/api/historial-estados`

### 1. Registrar Cambio de Estado
**Rol requerido:** OPERADOR, TRANSPORTISTA

```
POST http://localhost:8080/api/historial-estados
Authorization: Bearer <token-operador>
Content-Type: application/json

{
  "contenedorId": 1,
  "estadoAnterior": "DISPONIBLE",
  "estadoNuevo": "EN_TRANSITO",
  "ubicacion": "Juan de Garay 1755, Córdoba",
  "tramoId": 1,
  "observaciones": "Contenedor cargado y en camino",
  "usuarioRegistro": "operador@logistica.com",
  "depositoId": null
}
```

**Respuesta esperada (201 Created):**
```json
{
  "id": 1,
  "contenedorId": 1,
  "estadoAnterior": "DISPONIBLE",
  "estadoNuevo": "EN_TRANSITO",
  "ubicacion": "Juan de Garay 1755, Córdoba",
  "tramoId": 1,
  "fechaHora": "2025-11-13T10:30:00",
  "observaciones": "Contenedor cargado y en camino",
  "usuarioRegistro": "operador@logistica.com",
  "depositoId": null
}
```

**📝 Validaciones:**
- `contenedorId`: Obligatorio, debe existir el contenedor
- `estadoAnterior`: Opcional, máximo 30 caracteres
- `estadoNuevo`: Obligatorio, máximo 30 caracteres
- `ubicacion`: Opcional, máximo 500 caracteres
- `tramoId`: Opcional, debe existir el tramo si se provee
- `observaciones`: Opcional, máximo 1000 caracteres
- `usuarioRegistro`: Opcional, máximo 100 caracteres
- `depositoId`: Opcional, debe existir el depósito si se provee

**Estados típicos de contenedor:**
- `DISPONIBLE`: Contenedor disponible para asignar
- `EN_TRANSITO`: En movimiento hacia destino
- `EN_DEPOSITO`: Almacenado en depósito
- `ENTREGADO`: Entregado al destino final
- `EN_MANTENIMIENTO`: En proceso de mantenimiento
- `FUERA_DE_SERVICIO`: No disponible para uso

### 2. Listar Todo el Historial
**Rol requerido:** OPERADOR

```
GET http://localhost:8080/api/historial-estados
Authorization: Bearer <token-operador>
```

**Respuesta esperada (200 OK):** Lista completa de cambios de estado

### 3. Obtener Historial por Contenedor
**Rol requerido:** OPERADOR, CLIENTE

```
GET http://localhost:8080/api/historial-estados/contenedor/1
Authorization: Bearer <token-operador>
```

**Respuesta esperada (200 OK):**
```json
[
  {
    "id": 3,
    "contenedorId": 1,
    "estadoAnterior": "EN_TRANSITO",
    "estadoNuevo": "ENTREGADO",
    "ubicacion": "De los Toscanos 6581, Córdoba",
    "tramoId": 1,
    "fechaHora": "2025-11-13T11:30:00",
    "observaciones": "Entrega exitosa, cliente presente",
    "usuarioRegistro": "transportista@logistica.com",
    "depositoId": null
  },
  {
    "id": 2,
    "contenedorId": 1,
    "estadoAnterior": "DISPONIBLE",
    "estadoNuevo": "EN_TRANSITO",
    "ubicacion": "Juan de Garay 1755, Córdoba",
    "tramoId": 1,
    "fechaHora": "2025-11-13T10:30:00",
    "observaciones": "Contenedor cargado y en camino",
    "usuarioRegistro": "operador@logistica.com",
    "depositoId": null
  }
]
```

**📝 Nota:** Los resultados están ordenados por fecha descendente (más reciente primero)

### 4. Obtener Historial por Tramo
**Rol requerido:** OPERADOR, TRANSPORTISTA

```
GET http://localhost:8080/api/historial-estados/tramo/1
Authorization: Bearer <token-operador>
```

**Respuesta esperada (200 OK):** Lista de cambios de estado durante un tramo específico

### 5. Obtener Historial por Depósito
**Rol requerido:** OPERADOR

```
GET http://localhost:8080/api/historial-estados/deposito/1
Authorization: Bearer <token-operador>
```

**Respuesta esperada (200 OK):**
```json
[
  {
    "id": 5,
    "contenedorId": 2,
    "estadoAnterior": "EN_TRANSITO",
    "estadoNuevo": "EN_DEPOSITO",
    "ubicacion": "Depósito Central Córdoba",
    "tramoId": 3,
    "fechaHora": "2025-11-13T14:00:00",
    "observaciones": "Almacenamiento temporal por 2 días",
    "usuarioRegistro": "operador@logistica.com",
    "depositoId": 1
  }
]
```

**📝 Casos de Uso Típicos:**

1. **Seguimiento de contenedor durante transporte:**
   - Registrar estado `EN_TRANSITO` al iniciar tramo
   - Registrar `EN_DEPOSITO` si hay parada intermedia
   - Registrar `ENTREGADO` al completar

2. **Auditoría de movimientos:**
   - Consultar historial completo por contenedor
   - Verificar cambios durante un tramo específico
   - Revisar actividad en un depósito

3. **Trazabilidad para clientes:**
   - Cliente puede consultar historial de su contenedor
   - Ver ubicación actual y estados previos
   - Recibir observaciones sobre el estado

---

## Rutas Service

**Base URL:** `http://localhost:8080/api/rutas`

### 1. Listar Todas las Rutas  (Funciona)
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
    "costoTotalReal": null,
    "tiempoEstimadoHoras": 1,
    "estado": "PLANIFICADA",
    "activa": true,
    "tramos": [
      {
        "id": 1,
        "orden": 1,
        "origenTipo": "ORIGEN",
        "origenDireccion": "Juan de Garay 1755, Córdoba",
        "destinoTipo": "DESTINO",
        "destinoDireccion": "De los Toscanos 6581, Córdoba",
        "tipoTramo": "ORIGEN_DESTINO",
        "estado": "ESTIMADO",
        "distanciaKm": 9.50,
        "costoAproximado": 950.00,
        "costoReal": null,
        "fechaHoraInicioEstimada": "2025-12-15T08:00:00",
        "fechaHoraFinEstimada": "2025-12-15T09:00:00",
        "fechaHoraInicio": null,
        "fechaHoraFin": null,
        "observaciones": null,
        "camionId": null
      }
    ],
    "fechaCreacion": "2025-11-12T10:35:00",
    "fechaActualizacion": "2025-11-12T10:35:00"
  }
]
```

### 2. Obtener Ruta por ID (Funciona)
**Rol requerido:** OPERADOR, TRANSPORTISTA

```
GET http://localhost:8080/api/rutas/1
Authorization: Bearer <token-operador>
```

**Respuesta esperada (200 OK):** Objeto ruta completo con tramos

### 3. Obtener Ruta por Solicitud (Funciona)
**Rol requerido:** OPERADOR, CLIENTE (solo sus solicitudes)

```
GET http://localhost:8080/api/rutas/solicitud/1
Authorization: Bearer <token-operador>
```

**Respuesta esperada (200 OK):** Ruta asociada a la solicitud

### 4. Calcular Ruta Tentativa (Funciona)
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

### 5. Asignar Camión a un Tramo (Funciona)
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
  "estado": "ASIGNADO",
  "distanciaKm": 9.10,
  "costoAproximado": 910.00,
  "costoReal": null,
  "fechaHoraInicioEstimada": null,
  "fechaHoraFinEstimada": null,
  "fechaHoraInicio": null,
  "fechaHoraFin": null,
  "observaciones": null,
  "camionId": 1
}
```

**📝 Nota:** El estado del tramo cambia de `ESTIMADO` a `ASIGNADO` y el camión queda reservado. La respuesta devuelve solo el tramo actualizado para mantener consistencia con los endpoints de iniciar/finalizar tramo.

### 6. Iniciar Tramo (Funciona)
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

### 7. Finalizar Tramo (Funciona)
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

### 8. Desactivar Ruta (Funciona)
**Rol requerido:** OPERADOR

```
DELETE http://localhost:8080/api/rutas/15
Authorization: Bearer <token-operador>
```

**Respuesta esperada (204 No Content)**

**📝 Notas importantes:**
- **Soft Delete:** La ruta NO se elimina de la base de datos, solo se marca como `activa=false`
- **Validación:** No se puede desactivar una ruta que tenga tramos en estado `INICIADO`
- **Filtrado automático:** Las rutas desactivadas NO aparecen en `GET /api/rutas` (solo muestra activas)
- **Historial:** Los datos se preservan para auditoría y trazabilidad
- **Caso de uso:** Útil para eliminar rutas tentativas que no se van a usar

**Ejemplo de error al intentar desactivar ruta con tramo en proceso:**
```json
{
  "status": 500,
  "message": "No se puede desactivar una ruta con tramos en proceso",
  "timestamp": "2025-11-12T21:30:00"
}
```

**Verificación después de desactivar:**
```
GET http://localhost:8080/api/rutas
Authorization: Bearer <token-operador>
```

La ruta desactivada ya NO aparecerá en la lista.

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

#### 1.1 Verificar Clientes  (Funciona)

```
GET http://localhost:8080/api/clientes
Authorization: Bearer TOKEN_OPERADOR
```

**Resultado esperado:**
```json
[
  {
        "id": 5,
        "nombre": "Fran",
        "apellido": "Torrens",
        "dni": "4256789",
        "email": "franTorrens@gmail.com",
        "telefono": "+3516416675",
        "direccion": "Av. Vélez Sarsfield 500",
        "ciudad": "Córdoba",
        "provincia": "Córdoba",
        "codigoPostal": "5000",
        "activo": true,
        "fechaRegistro": "2025-11-13T13:03:24.41682",
        "fechaActualizacion": "2025-11-13T13:05:24.829718"
    }
]
```

**✅ Validación:** Cliente con `id: 5` existe y está activo.

#### 1.2 Verificar Camiones Disponibles (Funciona)

```
GET http://localhost:8080/api/camiones/disponibles
Authorization: Bearer TOKEN_OPERADOR
```

**Resultado esperado:**
```json
[
  {
        "id": 2,
        "dominio": "AD788RT",
        "marca": "Scania",
        "modelo": "R450",
        "año": 2020,
        "nombreTransportista": "Fran Molines Torrens",
        "capacidadPeso": 2000.00,
        "capacidadVolumen": 500.00,
        "consumoCombustible": 0.40,
        "costoPorKm": 180.00,
        "disponible": true,
        "activo": true,
        "fechaCreacion": "2025-11-13T14:44:09.10202",
        "fechaActualizacion": "2025-11-13T19:21:57.844239"
    }
]
```

**✅ Validación:** Camión con `id: 2` está disponible y su capacidad (2000 kg) es suficiente para nuestro contenedor (500 kg de peso y 15 kg de volumen).

#### 1.3 Verificar Tarifas Vigentes (Funciona)

```
GET http://localhost:8080/api/tarifas/vigentes
Authorization: Bearer TOKEN_OPERADOR
```

**Resultado esperado:**
```json
[
  {
        "id": 2,
        "tipo": "COSTO_KM_BASE",
        "descripcion": "Tarifa base por kilometraje",
        "valor": 1435.99,
        "unidad": "$/litro",
        "rangoPesoMinKg": null,
        "rangoPesoMaxKg": null,
        "rangoVolumenMinM3": null,
        "rangoVolumenMaxM3": null,
        "vigenciaDesde": "2025-02-01",
        "vigenciaHasta": null,
        "activo": true,
        "fechaCreacion": "2025-11-13T19:29:34.434835",
        "fechaActualizacion": "2025-11-13T19:29:34.43411"
    }
]
```
Tomar el id de la tarifa que luego se va a asignar, en este caso ID 2.

**✅ Validación:** Tarifa estándar activa: $1435.99 por km.

---

### Paso 2: Crear Solicitud de Traslado (Funciona)

```
POST http://localhost:8080/api/solicitudes
Authorization: Bearer TOKEN_OPERADOR
Content-Type: application/json

{
  "clienteId": 5,
  "ubicacionOrigen": "Acoqnuija 3200, Córdoba",
  "ubicacionDestino": "De los Toscanos 6581, Córdoba",
  "fechaProgramada": "2025-12-15",
  "observaciones": "Carga frágil, manejar con cuidado",
  "tarifaId": 2,
  "contenedor": {
    "identificacion": "CONT-PRUEBA-002",
    "peso": 500.00,
    "volumen": 15.00,
    "largoM": 6.00,
    "anchoM": 2.50,
    "altoM": 2.60,
    "estado": "DISPONIBLE",
    "descripcion": "Contenedor refrigerado",
    "clienteId": 5,
    "direccionOrigen": "Aconquija 3200, Córdoba",
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
        "id": 2,
        "numero": "SOL-20251113193842",
        "clienteId": 5,
        "contenedor": {
            "id": 2,
            "identificacion": "CONT-PRUEBA-0061",
            "peso": 500.00,
            "volumen": 15.00,
            "largoM": 6.00,
            "anchoM": 2.50,
            "altoM": 2.60,
            "estado": "DISPONIBLE",
            "descripcion": "Contenedor refrigerado",
            "clienteId": 5,
            "direccionOrigen": "Juan de Garay 1755, Córdoba",
            "latitudOrigen": -31.403771,
            "longitudOrigen": -64.163894,
            "direccionDestino": "De los Toscanos 6581, Córdoba",
            "latitudDestino": -31.340196,
            "longitudDestino": -64.224319
        },
        "ubicacionOrigen": "Juan de Garay 1755, Córdoba",
        "ubicacionDestino": "De los Toscanos 6581, Córdoba",
        "estado": "BORRADOR",
        "costoEstimado": null,
        "tiempoEstimadoHoras": null,
        "costoFinal": null,
        "tiempoRealHoras": null,
        "rutaId": null,
        "tarifaId": 2,
        "fechaSolicitud": "2025-11-13T19:38:42.577344",
        "fechaProgramada": "2025-12-15",
        "fechaEntregaEstimada": null,
        "fechaEntregaReal": null,
        "observaciones": "Carga frágil, manejar con cuidado",
        "activo": true,
        "fechaCreacion": "2025-11-13T19:38:42.612712",
        "fechaActualizacion": "2025-11-13T19:38:42.612739"
    }
```
La solicitud deberia manejar informacion hasta la informacion proporcionada por el contendor, ya que se duplica las ubicaciones y esto genera redundancia. Deberia terminar en la "longitudDestino" haciendo referencia al contenedor.

Tomamos el id 2 de la solicitud

```

**✅ Validaciones:**
- Solicitud creada con ID: `2`
- Estado inicial: `PENDIENTE`
- Campos de costo y ruta en `null` (aún no calculados)
- Contenedor asociado correctamente

**📝 Guarda:**
- `id: 10` como `SOLICITUD_ID`
- `rutaId` (creado automáticamente al crear la solicitud)
- `contenedores[0].id` como `CONTENEDOR_ID`

---

```

### Paso 3: Consultar Ruta Creada Automáticamente  (Funciona)

**ℹ️ Nota:** La ruta se crea automáticamente al crear la solicitud. No es necesario llamar a `/api/rutas/calcular` ya que esto generaría un error de constraint de unicidad (una solicitud solo puede tener una ruta).

```
GET http://localhost:8080/api/rutas/solicitud/2
Authorization: Bearer TOKEN_OPERADOR
```

**Resultado esperado (200 OK):**
```json
{
    "id": 2,
    "solicitudId": 2,
    "cantidadTramos": 1,
    "cantidadDepositos": 0,
    "distanciaTotalKm": 9.10,
    "costoEstimado": 910.00,
    "costoTotalReal": null,
    "tiempoEstimadoHoras": 1,
    "estado": null,
    "activa": true,
    "tramos": [
        {
            "id": 2,
            "orden": 1,
            "origenTipo": "ORIGEN",
            "origenDireccion": "Juan de Garay 1755, Córdoba",
            "destinoTipo": "DESTINO",
            "destinoDireccion": "De los Toscanos 6581, Córdoba",
            "tipoTramo": "ORIGEN_DESTINO",
            "estado": "ESTIMADO",
            "distanciaKm": 9.10,
            "costoAproximado": 910.00,
            "costoReal": null,
            "fechaHoraInicioEstimada": null,
            "fechaHoraFinEstimada": null,
            "fechaHoraInicio": null,
            "fechaHoraFin": null,
            "observaciones": null,
            "camionId": null
        }
    ],
    "fechaCreacion": "2025-11-13T20:02:30.532628",
    "fechaActualizacion": "2025-11-13T20:02:30.532665"
}
```

**✅ Validaciones:**
- Ruta creada automáticamente con la solicitud
- **Distancia calculada usando Google Maps Distance Matrix API** (distancia real por carreteras)
- **Costo calculado con tarifa real de la solicitud** (no valor hardcodeado)
- Fórmula: `distanciaKm × precioPorKm` de la tarifa asociada
- Ejemplo: Si distancia = 9.10 km y tarifa ID 2 tiene `precioPorKm: 1435.99`:
  - Costo estimado = 9.10 × 1435.99 = **$13,067.51**
- Tiempo estimado calculado según velocidad promedio (60 km/h)
- Tramo único (origen → destino directo, sin depósitos intermedios)
- Estado de la ruta: `PLANIFICADA`
- Estado del tramo: `ESTIMADO`
- Camión aún no asignado

**🔧 Implementación técnica:**
- Google Maps API Key configurada en `docker-compose.yml`
- API: `https://maps.googleapis.com/maps/api/distancematrix/json`
- Parámetros: `origins`, `destinations`, `mode=driving`, `units=metric`
- Fallback: Si Google Maps falla, usa fórmula de Haversine (distancia en línea recta)
- Tarifa obtenida dinámicamente desde `tarifas-service` usando `tarifaId` de la solicitud

**📝 Guarda:**
- `id: 2` como `RUTA_ID`
- `tramos[0].id: 2` como `TRAMO_ID`


### Paso 4: Asignar Camión al Tramo   (Funciona)

```
POST http://localhost:8080/api/rutas/tramos/40/asignar-camion
Authorization: Bearer TOKEN_OPERADOR
Content-Type: application/json

{
  "camionId": 2
}
```

**Resultado esperado (200 OK):**
```json
{
    "id": 2,
    "orden": 1,
    "origenTipo": "ORIGEN",
    "origenDireccion": "Juan de Garay 1755, Córdoba",
    "destinoTipo": "DESTINO",
    "destinoDireccion": "De los Toscanos 6581, Córdoba",
    "tipoTramo": "ORIGEN_DESTINO",
    "estado": "ASIGNADO",
    "distanciaKm": 9.10,
    "costoAproximado": 910.00,
    "costoReal": null,
    "fechaHoraInicioEstimada": null,
    "fechaHoraFinEstimada": null,
    "fechaHoraInicio": null,
    "fechaHoraFin": null,
    "observaciones": null,
    "camionId": 2
}
```

**✅ Validaciones:**
- Estado del tramo cambió: `ESTIMADO` → `ASIGNADO`
- `camionId`: `2` 

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
| 404 "Transportista no encontrado" | ID de transportista inválido | Crear transportista primero con POST /api/transportistas |
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
| contenedoresActuales | Integer | ❌ | Mínimo 0, por defecto 0 |
| horarioApertura | LocalTime | ❌ | Formato: HH:MM:SS |
| horarioCierre | LocalTime | ❌ | Formato: HH:MM:SS |
| observaciones | String | ❌ | Max 1000 caracteres |

### Transportista

| Campo | Tipo | Obligatorio | Validación |
|-------|------|-------------|------------|
| nombre | String | ✅ | 2-100 caracteres |
| apellido | String | ✅ | 2-100 caracteres |
| telefono | String | ✅ | 10-20 dígitos, puede incluir + |

### Camión

| Campo | Tipo | Obligatorio | Validación |
|-------|------|-------------|------------|
| dominio | String | ✅ | Max 20 caracteres |
| marca | String | ✅ | Max 50 caracteres |
| modelo | String | ✅ | Max 50 caracteres |
| año | Integer | ✅ | Entre 1900 y 2100 |
| transportistaId | Long | ✅ | ID válido de transportista existente |
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

