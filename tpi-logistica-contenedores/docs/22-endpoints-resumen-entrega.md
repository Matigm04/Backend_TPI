# Documentación de Endpoints - Sistema de Logística de Contenedores

**Proyecto:** TPI Backend de Aplicaciones 2025  
**Arquitectura:** Microservicios con API Gateway  
**Base URL:** `http://localhost:8080/api`  
**Autenticación:** JWT Bearer Token

---

## Convenciones Generales

- **Formato de datos:** JSON
- **Autenticación:** Todos los endpoints requieren token JWT en header `Authorization: Bearer <token>`
- **Códigos HTTP estándar:**
  - `200 OK` - Operación exitosa
  - `201 Created` - Recurso creado
  - `400 Bad Request` - Datos inválidos
  - `401 Unauthorized` - No autenticado
  - `403 Forbidden` - Sin permisos
  - `404 Not Found` - Recurso no encontrado
  - `500 Internal Server Error` - Error del servidor

---

## 1. Servicio de Clientes (Puerto 8081)

**Responsabilidad:** Gestión de clientes del sistema

| Método | Endpoint | Descripción | Roles |
|--------|----------|-------------|-------|
| POST | `/api/clientes` | Registrar nuevo cliente | Cliente, Operador, Admin |
| GET | `/api/clientes/{id}` | Obtener cliente por ID | Cliente (propio), Operador, Admin |
| PUT | `/api/clientes/{id}` | Actualizar datos del cliente | Cliente (propio), Operador, Admin |
| DELETE | `/api/clientes/{id}` | Eliminar cliente (soft delete) | Admin |
| GET | `/api/clientes` | Listar clientes con filtros | Operador, Admin |
| GET | `/api/clientes/buscar` | Buscar por email o documento | Operador, Admin |

**Datos principales:** nombre, apellido, email, teléfono, documento, dirección

---

## 2. Servicio de Solicitudes (Puerto 8085)

**Responsabilidad:** Gestión de solicitudes de traslado y contenedores

| Método | Endpoint | Descripción | Roles |
|--------|----------|-------------|-------|
| POST | `/api/solicitudes` | Crear solicitud de traslado | Cliente, Operador, Admin |
| GET | `/api/solicitudes/{id}` | Obtener solicitud por ID | Cliente (propia), Operador, Admin |
| PUT | `/api/solicitudes/{id}` | Actualizar solicitud | Operador, Admin |
| PUT | `/api/solicitudes/{id}/estado` | Cambiar estado de solicitud | Operador, Admin |
| GET | `/api/solicitudes/cliente/{clienteId}` | Solicitudes de un cliente | Cliente (propio), Operador, Admin |
| GET | `/api/solicitudes` | Listar con filtros (estado, fecha) | Operador, Admin |
| GET | `/api/solicitudes/pendientes` | Contenedores pendientes de entrega | Operador, Admin |

**Estados de solicitud:** BORRADOR → PROGRAMADA → EN_TRANSITO → ENTREGADA

**Datos principales:** número solicitud, contenedor, cliente, origen, destino, costos, tiempos

---

## 3. Servicio de Rutas (Puerto 8086)

**Responsabilidad:** Cálculo de rutas y gestión de tramos

| Método | Endpoint | Descripción | Roles |
|--------|----------|-------------|-------|
| POST | `/api/rutas/calcular` | Calcular rutas tentativas | Operador, Admin |
| POST | `/api/rutas` | Crear y asignar ruta definitiva | Operador, Admin |
| GET | `/api/rutas/{id}` | Obtener ruta por ID | Operador, Admin, Transportista |
| GET | `/api/rutas/solicitud/{solicitudId}` | Obtener ruta de una solicitud | Cliente, Operador, Admin, Transportista |
| PUT | `/api/rutas/{id}/tramo/{tramoId}` | Actualizar tramo específico | Operador, Admin |
| GET | `/api/rutas/tramos/pendientes` | Tramos sin camión asignado | Operador, Admin |

**Tipos de tramo:** ORIGEN_DEPOSITO, DEPOSITO_DEPOSITO, DEPOSITO_DESTINO, ORIGEN_DESTINO

**Datos principales:** solicitud, tramos, distancias, costos, fechas estimadas/reales

---

## 4. Servicio de Depósitos (Puerto 8082)

**Responsabilidad:** Gestión de depósitos intermedios

| Método | Endpoint | Descripción | Roles |
|--------|----------|-------------|-------|
| POST | `/api/depositos` | Crear nuevo depósito | Operador, Admin |
| GET | `/api/depositos/{id}` | Obtener depósito por ID | Operador, Admin |
| PUT | `/api/depositos/{id}` | Actualizar depósito | Operador, Admin |
| DELETE | `/api/depositos/{id}` | Eliminar depósito | Admin |
| GET | `/api/depositos` | Listar todos los depósitos | Operador, Admin |
| GET | `/api/depositos/cercanos` | Buscar depósitos cercanos (lat/lng) | Operador, Admin |
| GET | `/api/depositos/{id}/contenedores` | Contenedores en el depósito | Operador, Admin |

**Datos principales:** ubicación (dirección, coordenadas), capacidad, costo estadía diaria, horarios

---

## 5. Servicio de Camiones (Puerto 8083)

**Responsabilidad:** Gestión de flota de camiones y transportistas

### Endpoints de Camiones

| Método | Endpoint | Descripción | Roles |
|--------|----------|-------------|-------|
| POST | `/api/camiones` | Registrar nuevo camión | Operador, Admin |
| GET | `/api/camiones/{id}` | Obtener camión por ID | Operador, Admin |
| PUT | `/api/camiones/{id}` | Actualizar datos del camión | Operador, Admin |
| DELETE | `/api/camiones/{id}` | Eliminar camión | Admin |
| GET | `/api/camiones` | Listar camiones con filtros | Operador, Admin |
| GET | `/api/camiones/disponibles` | Camiones disponibles por capacidad | Operador, Admin |
| POST | `/api/camiones/{id}/asignar-tramo` | Asignar camión a un tramo | Operador, Admin |
| POST | `/api/camiones/{id}/liberar` | Liberar camión (disponible) | Operador, Admin |
| POST | `/api/camiones/{id}/iniciar-tramo` | Registrar inicio de tramo | Transportista |
| POST | `/api/camiones/{id}/finalizar-tramo` | Registrar fin de tramo | Transportista |

**Datos principales:** dominio, marca, modelo, capacidades (peso/volumen), consumo, costo por km, transportista

### Endpoints de Transportistas

| Método | Endpoint | Descripción | Roles |
|--------|----------|-------------|-------|
| POST | `/api/transportistas` | Registrar transportista | Operador, Admin |
| GET | `/api/transportistas/{id}` | Obtener transportista por ID | Transportista (propio), Operador, Admin |
| PUT | `/api/transportistas/{id}` | Actualizar transportista | Transportista (propio), Operador, Admin |
| GET | `/api/transportistas` | Listar transportistas | Operador, Admin |
| GET | `/api/transportistas/{id}/tramos` | Tramos asignados al transportista | Transportista (propio), Operador, Admin |

**Datos principales:** nombre, apellido, documento, teléfono, email, licencia de conducir

---

## 6. Servicio de Tarifas (Puerto 8084)

**Responsabilidad:** Gestión de tarifas y cálculo de costos

| Método | Endpoint | Descripción | Roles |
|--------|----------|-------------|-------|
| POST | `/api/tarifas` | Crear nueva tarifa | Operador, Admin |
| GET | `/api/tarifas/{id}` | Obtener tarifa por ID | Operador, Admin |
| PUT | `/api/tarifas/{id}` | Actualizar tarifa | Operador, Admin |
| DELETE | `/api/tarifas/{id}` | Eliminar tarifa | Admin |
| GET | `/api/tarifas` | Listar tarifas con filtros | Operador, Admin |
| GET | `/api/tarifas/vigentes` | Tarifas vigentes en una fecha | Operador, Admin |
| POST | `/api/tarifas/calcular-estimado` | Calcular costo estimado | Cliente, Operador, Admin |
| POST | `/api/tarifas/calcular-real` | Calcular costo real (finalizado) | Operador, Admin |

**Tipos de tarifa:** COSTO_KM_BASE, COMBUSTIBLE_LITRO, CARGO_GESTION, ESTADIA_DEPOSITO

**Datos principales:** concepto, descripción, valor, unidad, rangos (peso/volumen), vigencia

---

## 7. Servicio de Seguimiento

**Responsabilidad:** Tracking y historial de estados de contenedores

| Método | Endpoint | Descripción | Roles |
|--------|----------|-------------|-------|
| POST | `/api/seguimiento/registrar` | Registrar evento de seguimiento | Operador, Admin, Transportista |
| GET | `/api/seguimiento/contenedor/{id}` | Historial completo del contenedor | Cliente (propio), Operador, Admin |
| GET | `/api/seguimiento/solicitud/{id}` | Timeline de eventos de solicitud | Cliente (propia), Operador, Admin |
| GET | `/api/seguimiento/contenedor/{id}/ubicacion-actual` | Ubicación actual del contenedor | Cliente (propio), Operador, Admin |

**Estados de contenedor:** CREADO → EN_DEPOSITO → EN_TRANSITO → ENTREGADO

---

## Resumen de Roles y Permisos

### Cliente
- Crear solicitudes de traslado
- Consultar sus propias solicitudes y contenedores
- Ver seguimiento de sus contenedores
- Calcular costos estimados

### Operador/Administrador
- Gestión completa de depósitos, camiones, tarifas
- Asignar rutas y camiones a tramos
- Modificar estados de solicitudes
- Consultar toda la información del sistema
- Calcular costos reales

### Transportista
- Ver tramos asignados
- Registrar inicio y fin de tramos
- Actualizar sus datos personales

---

## Flujo Principal del Sistema

\`\`\`
1. Cliente crea solicitud → POST /api/solicitudes
2. Operador calcula rutas → POST /api/rutas/calcular
3. Operador asigna ruta → POST /api/rutas
4. Operador asigna camión → POST /api/camiones/{id}/asignar-tramo
5. Transportista inicia tramo → POST /api/camiones/{id}/iniciar-tramo
6. Sistema registra seguimiento → POST /api/seguimiento/registrar
7. Transportista finaliza tramo → POST /api/camiones/{id}/finalizar-tramo
8. Sistema calcula costo real → POST /api/tarifas/calcular-real
9. Cliente consulta estado → GET /api/seguimiento/contenedor/{id}
\`\`\`

---

## Integraciones Externas

### Google Maps Distance Matrix API
- **Uso:** Cálculo de distancias reales entre coordenadas
- **Servicio:** rutas-service
- **Endpoint interno:** Integrado en POST `/api/rutas/calcular`

### Keycloak (Autenticación)
- **Uso:** Gestión de identidad y autenticación JWT
- **Puerto:** 8180
- **Realm:** logistica-realm
- **Roles:** ROLE_CLIENTE, ROLE_OPERADOR, ROLE_TRANSPORTISTA, ROLE_ADMIN

---

## Documentación Técnica Adicional

- **Swagger UI:** Disponible en cada microservicio en `/swagger-ui.html`
- **OpenAPI Spec:** Disponible en `/v3/api-docs`
- **Health Check:** Disponible en `/actuator/health` (si está habilitado)

---

## Notas Importantes

1. **Paginación:** Los endpoints de listado soportan parámetros `page` y `size`
2. **Filtros:** Muchos endpoints GET soportan query parameters para filtrado
3. **Soft Delete:** Las eliminaciones son lógicas (campo `activo = false`)
4. **Auditoría:** Todas las entidades tienen `fechaCreacion` y `fechaActualizacion`
5. **Validaciones:** Todos los endpoints validan datos de entrada con Bean Validation

---

**Fecha de elaboración:** Enero 2025  
**Versión:** 1.0
