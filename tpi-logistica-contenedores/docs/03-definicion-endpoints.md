# Definición de Endpoints por Microservicio

## Convenciones Generales

- **Base URL:** `http://localhost:8080/api` (a través del API Gateway)
- **Formato:** JSON
- **Autenticación:** JWT Bearer Token en header `Authorization`
- **Códigos HTTP:**
  - `200 OK` - Operación exitosa
  - `201 Created` - Recurso creado
  - `204 No Content` - Operación exitosa sin contenido
  - `400 Bad Request` - Datos inválidos
  - `401 Unauthorized` - No autenticado
  - `403 Forbidden` - No autorizado
  - `404 Not Found` - Recurso no encontrado
  - `500 Internal Server Error` - Error del servidor

---

## 1. Servicio de Clientes

**Base Path:** `/api/clientes`

### POST /api/clientes
**Descripción:** Registrar un nuevo cliente  
**Roles:** `ROLE_CLIENTE`, `ROLE_OPERADOR`, `ROLE_ADMIN`  
**Request Body:**
\`\`\`json
{
  "nombre": "Juan",
  "apellido": "Pérez",
  "email": "juan.perez@email.com",
  "telefono": "+54 11 1234-5678",
  "documentoTipo": "DNI",
  "documentoNumero": "12345678",
  "direccion": "Av. Corrientes 1234, CABA"
}
\`\`\`
**Response:** `201 Created`
\`\`\`json
{
  "id": 1,
  "nombre": "Juan",
  "apellido": "Pérez",
  "email": "juan.perez@email.com",
  "telefono": "+54 11 1234-5678",
  "documentoTipo": "DNI",
  "documentoNumero": "12345678",
  "direccion": "Av. Corrientes 1234, CABA",
  "fechaRegistro": "2025-01-15T10:30:00",
  "activo": true
}
\`\`\`

---

### GET /api/clientes/{id}
**Descripción:** Obtener un cliente por ID  
**Roles:** `ROLE_CLIENTE` (solo su propio ID), `ROLE_OPERADOR`, `ROLE_ADMIN`  
**Response:** `200 OK`
\`\`\`json
{
  "id": 1,
  "nombre": "Juan",
  "apellido": "Pérez",
  "email": "juan.perez@email.com",
  "telefono": "+54 11 1234-5678",
  "documentoTipo": "DNI",
  "documentoNumero": "12345678",
  "direccion": "Av. Corrientes 1234, CABA",
  "fechaRegistro": "2025-01-15T10:30:00",
  "activo": true
}
\`\`\`

---

### PUT /api/clientes/{id}
**Descripción:** Actualizar datos de un cliente  
**Roles:** `ROLE_CLIENTE` (solo su propio ID), `ROLE_OPERADOR`, `ROLE_ADMIN`  
**Request Body:** (campos opcionales)
\`\`\`json
{
  "telefono": "+54 11 9999-8888",
  "direccion": "Nueva dirección 456"
}
\`\`\`
**Response:** `200 OK` (cliente actualizado)

---

### GET /api/clientes
**Descripción:** Listar clientes con filtros  
**Roles:** `ROLE_OPERADOR`, `ROLE_ADMIN`  
**Query Params:**
- `nombre` (opcional)
- `email` (opcional)
- `activo` (opcional, default: true)
- `page` (opcional, default: 0)
- `size` (opcional, default: 20)

**Response:** `200 OK`
\`\`\`json
{
  "content": [
    {
      "id": 1,
      "nombre": "Juan",
      "apellido": "Pérez",
      "email": "juan.perez@email.com"
    }
  ],
  "totalElements": 1,
  "totalPages": 1,
  "size": 20,
  "number": 0
}
\`\`\`

---

## 2. Servicio de Solicitudes

**Base Path:** `/api/solicitudes`

### POST /api/solicitudes
**Descripción:** Crear una nueva solicitud de traslado  
**Roles:** `ROLE_CLIENTE`, `ROLE_OPERADOR`, `ROLE_ADMIN`  
**Request Body:**
\`\`\`json
{
  "clienteId": 1,
  "contenedor": {
    "codigoIdentificacion": "CONT-2025-001",
    "pesoKg": 2500.50,
    "volumenM3": 33.2,
    "largoM": 6.0,
    "anchoM": 2.4,
    "altoM": 2.3,
    "descripcion": "Contenedor estándar 20 pies"
  },
  "ubicacionOrigen": {
    "tipo": "ORIGEN",
    "nombre": "Depósito Central",
    "direccion": "Av. del Libertador 1000, CABA",
    "latitud": -34.5875,
    "longitud": -58.4189,
    "ciudad": "Buenos Aires",
    "provincia": "Buenos Aires"
  },
  "ubicacionDestino": {
    "tipo": "DESTINO",
    "nombre": "Obra en construcción",
    "direccion": "Ruta 9 Km 45, Escobar",
    "latitud": -34.3500,
    "longitud": -58.7833,
    "ciudad": "Escobar",
    "provincia": "Buenos Aires"
  },
  "fechaProgramada": "2025-02-01",
  "observaciones": "Entrega urgente"
}
\`\`\`
**Response:** `201 Created`
\`\`\`json
{
  "id": 1,
  "numeroSolicitud": "SOL-2025-00001",
  "contenedor": {
    "id": 1,
    "codigoIdentificacion": "CONT-2025-001",
    "pesoKg": 2500.50,
    "volumenM3": 33.2,
    "estado": "CREADO"
  },
  "clienteId": 1,
  "ubicacionOrigen": { /* ... */ },
  "ubicacionDestino": { /* ... */ },
  "estado": "BORRADOR",
  "fechaSolicitud": "2025-01-15T10:30:00",
  "fechaProgramada": "2025-02-01",
  "costoEstimado": 45000.00,
  "tiempoEstimadoHoras": 8,
  "observaciones": "Entrega urgente"
}
\`\`\`

---

### GET /api/solicitudes/{id}
**Descripción:** Obtener una solicitud por ID  
**Roles:** `ROLE_CLIENTE` (solo sus solicitudes), `ROLE_OPERADOR`, `ROLE_ADMIN`  
**Response:** `200 OK` (solicitud completa)

---

### PUT /api/solicitudes/{id}/estado
**Descripción:** Cambiar el estado de una solicitud  
**Roles:** `ROLE_OPERADOR`, `ROLE_ADMIN`  
**Request Body:**
\`\`\`json
{
  "nuevoEstado": "PROGRAMADA",
  "observaciones": "Ruta asignada y camión confirmado"
}
\`\`\`
**Response:** `200 OK`

---

### GET /api/solicitudes/cliente/{clienteId}
**Descripción:** Obtener todas las solicitudes de un cliente  
**Roles:** `ROLE_CLIENTE` (solo su propio ID), `ROLE_OPERADOR`, `ROLE_ADMIN`  
**Response:** `200 OK` (lista de solicitudes)

---

### GET /api/solicitudes
**Descripción:** Listar solicitudes con filtros  
**Roles:** `ROLE_OPERADOR`, `ROLE_ADMIN`  
**Query Params:**
- `estado` (opcional): BORRADOR, PROGRAMADA, EN_TRANSITO, ENTREGADA
- `clienteId` (opcional)
- `fechaDesde` (opcional)
- `fechaHasta` (opcional)
- `page`, `size`

**Response:** `200 OK` (página de solicitudes)

---

### GET /api/solicitudes/pendientes
**Descripción:** Obtener contenedores pendientes de entrega con ubicación  
**Roles:** `ROLE_OPERADOR`, `ROLE_ADMIN`  
**Response:** `200 OK`
\`\`\`json
[
  {
    "solicitudId": 1,
    "numeroSolicitud": "SOL-2025-00001",
    "contenedorId": 1,
    "codigoContenedor": "CONT-2025-001",
    "estado": "EN_TRANSITO",
    "ubicacionActual": "Depósito Norte",
    "clienteNombre": "Juan Pérez"
  }
]
\`\`\`

---

## 3. Servicio de Rutas

**Base Path:** `/api/rutas`

### POST /api/rutas/calcular
**Descripción:** Calcular rutas tentativas para una solicitud  
**Roles:** `ROLE_OPERADOR`, `ROLE_ADMIN`  
**Request Body:**
\`\`\`json
{
  "solicitudId": 1,
  "incluirDepositos": true,
  "depositosOpcionales": [1, 2, 3]
}
\`\`\`
**Response:** `200 OK`
\`\`\`json
{
  "rutasCalculadas": [
    {
      "opcion": 1,
      "descripcion": "Ruta directa sin depósitos",
      "cantidadTramos": 1,
      "cantidadDepositos": 0,
      "distanciaTotalKm": 45.3,
      "costoEstimado": 38500.00,
      "tiempoEstimadoHoras": 2,
      "tramos": [
        {
          "numeroOrden": 1,
          "tipoTramo": "ORIGEN_DESTINO",
          "origenNombre": "Depósito Central",
          "destinoNombre": "Obra en construcción",
          "distanciaKm": 45.3,
          "costoAproximado": 38500.00
        }
      ]
    },
    {
      "opcion": 2,
      "descripcion": "Ruta con 1 depósito intermedio",
      "cantidadTramos": 2,
      "cantidadDepositos": 1,
      "distanciaTotalKm": 52.8,
      "costoEstimado": 42300.00,
      "tiempoEstimadoHoras": 4,
      "tramos": [
        {
          "numeroOrden": 1,
          "tipoTramo": "ORIGEN_DEPOSITO",
          "origenNombre": "Depósito Central",
          "destinoNombre": "Depósito Norte",
          "distanciaKm": 28.5,
          "costoAproximado": 22000.00
        },
        {
          "numeroOrden": 2,
          "tipoTramo": "DEPOSITO_DESTINO",
          "origenNombre": "Depósito Norte",
          "destinoNombre": "Obra en construcción",
          "distanciaKm": 24.3,
          "costoAproximado": 20300.00
        }
      ]
    }
  ]
}
\`\`\`

---

### POST /api/rutas
**Descripción:** Crear y asignar ruta definitiva a una solicitud  
**Roles:** `ROLE_OPERADOR`, `ROLE_ADMIN`  
**Request Body:**
\`\`\`json
{
  "solicitudId": 1,
  "opcionRutaSeleccionada": 1,
  "tramos": [
    {
      "numeroOrden": 1,
      "ubicacionOrigenId": 1,
      "ubicacionDestinoId": 2,
      "tipoTramo": "ORIGEN_DESTINO",
      "distanciaKm": 45.3,
      "fechaHoraInicioEstimada": "2025-02-01T08:00:00",
      "fechaHoraFinEstimada": "2025-02-01T10:00:00"
    }
  ]
}
\`\`\`
**Response:** `201 Created`
\`\`\`json
{
  "id": 1,
  "solicitudId": 1,
  "cantidadTramos": 1,
  "cantidadDepositos": 0,
  "distanciaTotalKm": 45.3,
  "costoTotalEstimado": 38500.00,
  "estado": "PLANIFICADA",
  "fechaCreacion": "2025-01-15T11:00:00",
  "tramos": [ /* ... */ ]
}
\`\`\`

---

### GET /api/rutas/{id}
**Descripción:** Obtener una ruta por ID  
**Roles:** `ROLE_OPERADOR`, `ROLE_ADMIN`, `ROLE_TRANSPORTISTA`  
**Response:** `200 OK` (ruta completa con tramos)

---

### GET /api/rutas/solicitud/{solicitudId}
**Descripción:** Obtener la ruta de una solicitud  
**Roles:** `ROLE_CLIENTE`, `ROLE_OPERADOR`, `ROLE_ADMIN`, `ROLE_TRANSPORTISTA`  
**Response:** `200 OK`

---

### POST /api/depositos
**Descripción:** Crear un nuevo depósito  
**Roles:** `ROLE_OPERADOR`, `ROLE_ADMIN`  
**Request Body:**
\`\`\`json
{
  "ubicacion": {
    "tipo": "DEPOSITO",
    "nombre": "Depósito Norte",
    "direccion": "Ruta 8 Km 50, Pilar",
    "latitud": -34.4500,
    "longitud": -58.9000,
    "ciudad": "Pilar",
    "provincia": "Buenos Aires"
  },
  "capacidadMaximaContenedores": 50,
  "costoEstadiaDiario": 1500.00,
  "horarioApertura": "08:00:00",
  "horarioCierre": "18:00:00"
}
\`\`\`
**Response:** `201 Created`

---

### GET /api/depositos
**Descripción:** Listar todos los depósitos  
**Roles:** `ROLE_OPERADOR`, `ROLE_ADMIN`  
**Response:** `200 OK`

---

### GET /api/depositos/{id}/contenedores
**Descripción:** Obtener contenedores actualmente en un depósito  
**Roles:** `ROLE_OPERADOR`, `ROLE_ADMIN`  
**Response:** `200 OK`
\`\`\`json
[
  {
    "contenedorId": 5,
    "codigoIdentificacion": "CONT-2025-005",
    "solicitudId": 5,
    "clienteNombre": "María González",
    "fechaIngreso": "2025-01-14T15:30:00",
    "diasEstadia": 1
  }
]
\`\`\`

---

## 4. Servicio de Flota

**Base Path:** `/api/camiones` y `/api/transportistas`

### POST /api/camiones
**Descripción:** Registrar un nuevo camión  
**Roles:** `ROLE_OPERADOR`, `ROLE_ADMIN`  
**Request Body:**
\`\`\`json
{
  "dominio": "ABC123",
  "marca": "Mercedes-Benz",
  "modelo": "Actros 2651",
  "anio": 2022,
  "capacidadPesoKg": 26000.00,
  "capacidadVolumenM3": 80.00,
  "consumoCombustibleKmLitro": 0.35,
  "costoBasePorKm": 850.00,
  "transportistaId": 1
}
\`\`\`
**Response:** `201 Created`

---

### GET /api/camiones
**Descripción:** Listar todos los camiones  
**Roles:** `ROLE_OPERADOR`, `ROLE_ADMIN`  
**Query Params:**
- `disponible` (opcional): true/false
- `transportistaId` (opcional)

**Response:** `200 OK`

---

### GET /api/camiones/disponibles
**Descripción:** Obtener camiones disponibles que cumplan requisitos  
**Roles:** `ROLE_OPERADOR`, `ROLE_ADMIN`  
**Query Params:**
- `pesoMinKg` (requerido)
- `volumenMinM3` (requerido)

**Response:** `200 OK`
\`\`\`json
[
  {
    "id": 1,
    "dominio": "ABC123",
    "marca": "Mercedes-Benz",
    "modelo": "Actros 2651",
    "capacidadPesoKg": 26000.00,
    "capacidadVolumenM3": 80.00,
    "disponible": true,
    "transportistaNombre": "Carlos Rodríguez"
  }
]
\`\`\`

---

### POST /api/camiones/{id}/asignar-tramo
**Descripción:** Asignar un camión a un tramo  
**Roles:** `ROLE_OPERADOR`, `ROLE_ADMIN`  
**Request Body:**
\`\`\`json
{
  "tramoId": 1,
  "contenedorPesoKg": 2500.50,
  "contenedorVolumenM3": 33.2
}
\`\`\`
**Response:** `200 OK`
\`\`\`json
{
  "mensaje": "Camión ABC123 asignado exitosamente al tramo 1",
  "camionId": 1,
  "tramoId": 1,
  "disponible": false
}
\`\`\`

---

### POST /api/camiones/{id}/iniciar-tramo
**Descripción:** Registrar inicio de un tramo  
**Roles:** `ROLE_TRANSPORTISTA`  
**Request Body:**
\`\`\`json
{
  "tramoId": 1,
  "fechaHoraInicio": "2025-02-01T08:15:00",
  "observaciones": "Salida confirmada"
}
\`\`\`
**Response:** `200 OK`

---

### POST /api/camiones/{id}/finalizar-tramo
**Descripción:** Registrar fin de un tramo  
**Roles:** `ROLE_TRANSPORTISTA`  
**Request Body:**
\`\`\`json
{
  "tramoId": 1,
  "fechaHoraFin": "2025-02-01T10:30:00",
  "observaciones": "Entrega exitosa"
}
\`\`\`
**Response:** `200 OK`

---

### POST /api/transportistas
**Descripción:** Registrar un nuevo transportista  
**Roles:** `ROLE_OPERADOR`, `ROLE_ADMIN`  
**Request Body:**
\`\`\`json
{
  "nombre": "Carlos",
  "apellido": "Rodríguez",
  "documentoTipo": "DNI",
  "documentoNumero": "23456789",
  "telefono": "+54 11 5555-6666",
  "email": "carlos.rodriguez@email.com",
  "licenciaConducir": "B-12345678",
  "fechaVencimientoLicencia": "2027-12-31"
}
\`\`\`
**Response:** `201 Created`

---

### GET /api/transportistas/{id}/tramos
**Descripción:** Obtener tramos asignados a un transportista  
**Roles:** `ROLE_TRANSPORTISTA` (solo su propio ID), `ROLE_OPERADOR`, `ROLE_ADMIN`  
**Query Params:**
- `estado` (opcional): ASIGNADO, INICIADO, FINALIZADO

**Response:** `200 OK`
\`\`\`json
[
  {
    "tramoId": 1,
    "rutaId": 1,
    "solicitudNumero": "SOL-2025-00001",
    "numeroOrden": 1,
    "tipoTramo": "ORIGEN_DESTINO",
    "origenNombre": "Depósito Central",
    "destinoNombre": "Obra en construcción",
    "estado": "ASIGNADO",
    "fechaHoraInicioEstimada": "2025-02-01T08:00:00",
    "camionDominio": "ABC123"
  }
]
\`\`\`

---

## 5. Servicio de Tarifas

**Base Path:** `/api/tarifas`

### POST /api/tarifas
**Descripción:** Crear una nueva tarifa  
**Roles:** `ROLE_OPERADOR`, `ROLE_ADMIN`  
**Request Body:**
\`\`\`json
{
  "concepto": "COMBUSTIBLE_LITRO",
  "descripcion": "Precio del litro de combustible",
  "valor": 850.00,
  "unidad": "POR_LITRO",
  "fechaVigenciaDesde": "2025-01-01",
  "fechaVigenciaHasta": null
}
\`\`\`
**Response:** `201 Created`

---

### GET /api/tarifas
**Descripción:** Listar todas las tarifas  
**Roles:** `ROLE_OPERADOR`, `ROLE_ADMIN`  
**Query Params:**
- `concepto` (opcional)
- `activo` (opcional, default: true)

**Response:** `200 OK`

---

### GET /api/tarifas/vigentes
**Descripción:** Obtener tarifas vigentes en una fecha  
**Roles:** `ROLE_OPERADOR`, `ROLE_ADMIN`  
**Query Params:**
- `fecha` (opcional, default: hoy)

**Response:** `200 OK`

---

### POST /api/tarifas/calcular-estimado
**Descripción:** Calcular costo estimado de un traslado  
**Roles:** `ROLE_OPERADOR`, `ROLE_ADMIN`, `ROLE_CLIENTE`  
**Request Body:**
\`\`\`json
{
  "pesoKg": 2500.50,
  "volumenM3": 33.2,
  "distanciaTotalKm": 45.3,
  "cantidadTramos": 1,
  "cantidadDepositos": 0,
  "diasEstadiaEstimados": 0
}
\`\`\`
**Response:** `200 OK`
\`\`\`json
{
  "costoGestionBase": 5000.00,
  "costoKilometraje": 22650.00,
  "costoCombustibleEstimado": 8500.00,
  "costoEstadiaDepositos": 0.00,
  "costoTotal": 36150.00,
  "detalles": {
    "tarifaKmBase": 500.00,
    "consumoPromedioLitrosKm": 0.22,
    "precioCombustibleLitro": 850.00
  }
}
\`\`\`

---

### POST /api/tarifas/calcular-real
**Descripción:** Calcular costo real de un traslado finalizado  
**Roles:** `ROLE_OPERADOR`, `ROLE_ADMIN`  
**Request Body:**
\`\`\`json
{
  "solicitudId": 1,
  "rutaId": 1,
  "tramos": [
    {
      "tramoId": 1,
      "camionId": 1,
      "distanciaKm": 45.3,
      "consumoCombustibleLitros": 15.86,
      "costoBaseCamionPorKm": 850.00
    }
  ],
  "diasEstadiaReales": 0
}
\`\`\`
**Response:** `200 OK`
\`\`\`json
{
  "costoGestionBase": 5000.00,
  "costoKilometraje": 38505.00,
  "costoCombustibleReal": 13481.00,
  "costoEstadiaDepositos": 0.00,
  "costoTotal": 56986.00,
  "detalles": {
    "distanciaTotal": 45.3,
    "consumoTotalLitros": 15.86,
    "precioCombustibleLitro": 850.00
  }
}
\`\`\`

---

## 6. Servicio de Seguimiento

**Base Path:** `/api/seguimiento`

### POST /api/seguimiento/registrar
**Descripción:** Registrar un evento de seguimiento  
**Roles:** `ROLE_OPERADOR`, `ROLE_ADMIN`, `ROLE_TRANSPORTISTA`  
**Request Body:**
\`\`\`json
{
  "contenedorId": 1,
  "estadoAnterior": "CREADO",
  "estadoNuevo": "EN_TRANSITO",
  "ubicacionId": 1,
  "tramoId": 1,
  "observaciones": "Contenedor retirado del origen"
}
\`\`\`
**Response:** `201 Created`

---

### GET /api/seguimiento/contenedor/{contenedorId}
**Descripción:** Obtener historial completo de un contenedor  
**Roles:** `ROLE_CLIENTE` (si es su contenedor), `ROLE_OPERADOR`, `ROLE_ADMIN`  
**Response:** `200 OK`
\`\`\`json
[
  {
    "id": 1,
    "contenedorId": 1,
    "estadoAnterior": null,
    "estadoNuevo": "CREADO",
    "ubicacionNombre": "Depósito Central",
    "fechaHora": "2025-01-15T10:30:00",
    "observaciones": "Contenedor registrado en el sistema"
  },
  {
    "id": 2,
    "contenedorId": 1,
    "estadoAnterior": "CREADO",
    "estadoNuevo": "EN_TRANSITO",
    "ubicacionNombre": "Depósito Central",
    "tramoNumero": 1,
    "fechaHora": "2025-02-01T08:15:00",
    "observaciones": "Contenedor retirado del origen"
  }
]
\`\`\`

---

### GET /api/seguimiento/solicitud/{solicitudId}
**Descripción:** Obtener timeline de eventos de una solicitud  
**Roles:** `ROLE_CLIENTE` (si es su solicitud), `ROLE_OPERADOR`, `ROLE_ADMIN`  
**Response:** `200 OK`

---

### GET /api/seguimiento/contenedor/{contenedorId}/ubicacion-actual
**Descripción:** Obtener ubicación actual de un contenedor  
**Roles:** `ROLE_CLIENTE` (si es su contenedor), `ROLE_OPERADOR`, `ROLE_ADMIN`  
**Response:** `200 OK`
\`\`\`json
{
  "contenedorId": 1,
  "codigoIdentificacion": "CONT-2025-001",
  "estadoActual": "EN_TRANSITO",
  "ubicacionActual": {
    "nombre": "En ruta hacia Obra en construcción",
    "tipo": "EN_TRANSITO",
    "ultimaActualizacion": "2025-02-01T08:15:00"
  },
  "tramoActual": {
    "tramoId": 1,
    "numeroOrden": 1,
    "origen": "Depósito Central",
    "destino": "Obra en construcción",
    "camionDominio": "ABC123",
    "transportistaNombre": "Carlos Rodríguez"
  }
}
\`\`\`

---

## Resumen de Roles y Accesos

| Endpoint | Cliente | Operador | Transportista | Admin |
|----------|---------|----------|---------------|-------|
| POST /api/clientes | ✓ | ✓ | ✗ | ✓ |
| GET /api/clientes/{id} | ✓* | ✓ | ✗ | ✓ |
| POST /api/solicitudes | ✓ | ✓ | ✗ | ✓ |
| GET /api/solicitudes/{id} | ✓* | ✓ | ✗ | ✓ |
| POST /api/rutas/calcular | ✗ | ✓ | ✗ | ✓ |
| POST /api/camiones/{id}/asignar-tramo | ✗ | ✓ | ✗ | ✓ |
| POST /api/camiones/{id}/iniciar-tramo | ✗ | ✗ | ✓ | ✓ |
| GET /api/seguimiento/contenedor/{id} | ✓* | ✓ | ✗ | ✓ |

*✓* = Solo puede acceder a sus propios recursos*

---

## Notas Finales

1. Todos los endpoints requieren autenticación JWT
2. Los endpoints de listado soportan paginación
3. Las fechas se manejan en formato ISO 8601
4. Los errores devuelven un formato estándar:
\`\`\`json
{
  "timestamp": "2025-01-15T10:30:00",
  "status": 400,
  "error": "Bad Request",
  "message": "El peso del contenedor no puede ser negativo",
  "path": "/api/solicitudes"
}
