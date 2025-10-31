# Diseño Entidad-Relación (DER) - Sistema de Logística de Contenedores

## Estrategia de Base de Datos

**Opción recomendada:** Base de datos independiente por microservicio (patrón Database per Service)

- **Ventajas:** Desacoplamiento total, escalabilidad independiente, tecnologías específicas por servicio
- **DBMS propuesto:** PostgreSQL para producción, H2 para desarrollo/testing

---

## Entidades y Atributos

### 1. CLIENTE
Gestiona la información de los clientes que solicitan traslados.

\`\`\`
CLIENTE
├── id (PK, BIGINT, AUTO_INCREMENT)
├── nombre (VARCHAR(100), NOT NULL)
├── apellido (VARCHAR(100), NOT NULL)
├── email (VARCHAR(150), UNIQUE, NOT NULL)
├── telefono (VARCHAR(20), NOT NULL)
├── documento_tipo (VARCHAR(10), NOT NULL) // DNI, CUIT, etc.
├── documento_numero (VARCHAR(20), UNIQUE, NOT NULL)
├── direccion (VARCHAR(255))
├── fecha_registro (TIMESTAMP, DEFAULT CURRENT_TIMESTAMP)
└── activo (BOOLEAN, DEFAULT TRUE)
\`\`\`

---

### 2. CONTENEDOR
Representa los contenedores a transportar.

\`\`\`
CONTENEDOR
├── id (PK, BIGINT, AUTO_INCREMENT)
├── codigo_identificacion (VARCHAR(50), UNIQUE, NOT NULL) // Código único del contenedor
├── peso_kg (DECIMAL(10,2), NOT NULL) // Peso en kilogramos
├── volumen_m3 (DECIMAL(10,2), NOT NULL) // Volumen en metros cúbicos
├── largo_m (DECIMAL(5,2), NOT NULL) // Dimensiones
├── ancho_m (DECIMAL(5,2), NOT NULL)
├── alto_m (DECIMAL(5,2), NOT NULL)
├── estado (VARCHAR(30), NOT NULL) // CREADO, EN_TRANSITO, EN_DEPOSITO, ENTREGADO
├── descripcion (TEXT)
├── fecha_creacion (TIMESTAMP, DEFAULT CURRENT_TIMESTAMP)
└── cliente_id (FK -> CLIENTE.id, NOT NULL)
\`\`\`

**Estados posibles:** CREADO, EN_TRANSITO, EN_DEPOSITO, ENTREGADO

---

### 3. UBICACION
Representa puntos geográficos (origen, destino, depósitos).

\`\`\`
UBICACION
├── id (PK, BIGINT, AUTO_INCREMENT)
├── tipo (VARCHAR(20), NOT NULL) // ORIGEN, DESTINO, DEPOSITO
├── nombre (VARCHAR(100))
├── direccion (VARCHAR(255), NOT NULL)
├── latitud (DECIMAL(10,8), NOT NULL)
├── longitud (DECIMAL(11,8), NOT NULL)
├── ciudad (VARCHAR(100))
├── provincia (VARCHAR(100))
├── codigo_postal (VARCHAR(10))
└── activo (BOOLEAN, DEFAULT TRUE)
\`\`\`

---

### 4. DEPOSITO
Información específica de depósitos (extiende UBICACION).

\`\`\`
DEPOSITO
├── id (PK, BIGINT, AUTO_INCREMENT)
├── ubicacion_id (FK -> UBICACION.id, UNIQUE, NOT NULL)
├── capacidad_maxima_contenedores (INTEGER, NOT NULL)
├── contenedores_actuales (INTEGER, DEFAULT 0)
├── costo_estadia_diario (DECIMAL(10,2), NOT NULL) // Costo por día de estadía
├── horario_apertura (TIME)
├── horario_cierre (TIME)
└── observaciones (TEXT)
\`\`\`

---

### 5. SOLICITUD
Representa una solicitud de traslado de contenedor.

\`\`\`
SOLICITUD
├── id (PK, BIGINT, AUTO_INCREMENT)
├── numero_solicitud (VARCHAR(20), UNIQUE, NOT NULL) // Ej: SOL-2025-00001
├── contenedor_id (FK -> CONTENEDOR.id, NOT NULL)
├── cliente_id (FK -> CLIENTE.id, NOT NULL)
├── ubicacion_origen_id (FK -> UBICACION.id, NOT NULL)
├── ubicacion_destino_id (FK -> UBICACION.id, NOT NULL)
├── estado (VARCHAR(30), NOT NULL) // BORRADOR, PROGRAMADA, EN_TRANSITO, ENTREGADA, CANCELADA
├── fecha_solicitud (TIMESTAMP, DEFAULT CURRENT_TIMESTAMP)
├── fecha_programada (DATE)
├── fecha_entrega_estimada (TIMESTAMP)
├── fecha_entrega_real (TIMESTAMP)
├── costo_estimado (DECIMAL(10,2))
├── costo_final (DECIMAL(10,2))
├── tiempo_estimado_horas (INTEGER)
├── tiempo_real_horas (INTEGER)
├── observaciones (TEXT)
└── activo (BOOLEAN, DEFAULT TRUE)
\`\`\`

**Estados posibles:** BORRADOR, PROGRAMADA, EN_TRANSITO, ENTREGADA, CANCELADA

---

### 6. RUTA
Define la ruta completa de una solicitud.

\`\`\`
RUTA
├── id (PK, BIGINT, AUTO_INCREMENT)
├── solicitud_id (FK -> SOLICITUD.id, UNIQUE, NOT NULL)
├── cantidad_tramos (INTEGER, NOT NULL)
├── cantidad_depositos (INTEGER, DEFAULT 0)
├── distancia_total_km (DECIMAL(10,2))
├── costo_total_estimado (DECIMAL(10,2))
├── costo_total_real (DECIMAL(10,2))
├── fecha_creacion (TIMESTAMP, DEFAULT CURRENT_TIMESTAMP)
└── estado (VARCHAR(30), NOT NULL) // PLANIFICADA, EN_EJECUCION, COMPLETADA
\`\`\`

---

### 7. TRAMO
Representa cada segmento de la ruta.

\`\`\`
TRAMO
├── id (PK, BIGINT, AUTO_INCREMENT)
├── ruta_id (FK -> RUTA.id, NOT NULL)
├── numero_orden (INTEGER, NOT NULL) // Orden del tramo en la ruta (1, 2, 3...)
├── ubicacion_origen_id (FK -> UBICACION.id, NOT NULL)
├── ubicacion_destino_id (FK -> UBICACION.id, NOT NULL)
├── tipo_tramo (VARCHAR(30), NOT NULL) // ORIGEN_DEPOSITO, DEPOSITO_DEPOSITO, DEPOSITO_DESTINO, ORIGEN_DESTINO
├── estado (VARCHAR(30), NOT NULL) // ESTIMADO, ASIGNADO, INICIADO, FINALIZADO
├── distancia_km (DECIMAL(10,2))
├── camion_id (FK -> CAMION.id)
├── costo_aproximado (DECIMAL(10,2))
├── costo_real (DECIMAL(10,2))
├── fecha_hora_inicio_estimada (TIMESTAMP)
├── fecha_hora_fin_estimada (TIMESTAMP)
├── fecha_hora_inicio_real (TIMESTAMP)
├── fecha_hora_fin_real (TIMESTAMP)
└── observaciones (TEXT)
\`\`\`

**Tipos de tramo:** ORIGEN_DEPOSITO, DEPOSITO_DEPOSITO, DEPOSITO_DESTINO, ORIGEN_DESTINO

**Estados posibles:** ESTIMADO, ASIGNADO, INICIADO, FINALIZADO

---

### 8. CAMION
Información de los camiones disponibles.

\`\`\`
CAMION
├── id (PK, BIGINT, AUTO_INCREMENT)
├── dominio (VARCHAR(10), UNIQUE, NOT NULL) // Patente/Placa
├── marca (VARCHAR(50))
├── modelo (VARCHAR(50))
├── anio (INTEGER)
├── capacidad_peso_kg (DECIMAL(10,2), NOT NULL)
├── capacidad_volumen_m3 (DECIMAL(10,2), NOT NULL)
├── consumo_combustible_km_litro (DECIMAL(5,2), NOT NULL) // Litros por km
├── costo_base_por_km (DECIMAL(10,2), NOT NULL)
├── disponible (BOOLEAN, DEFAULT TRUE)
├── transportista_id (FK -> TRANSPORTISTA.id, NOT NULL)
├── fecha_registro (TIMESTAMP, DEFAULT CURRENT_TIMESTAMP)
└── activo (BOOLEAN, DEFAULT TRUE)
\`\`\`

---

### 9. TRANSPORTISTA
Información de los choferes/transportistas.

\`\`\`
TRANSPORTISTA
├── id (PK, BIGINT, AUTO_INCREMENT)
├── nombre (VARCHAR(100), NOT NULL)
├── apellido (VARCHAR(100), NOT NULL)
├── documento_tipo (VARCHAR(10), NOT NULL)
├── documento_numero (VARCHAR(20), UNIQUE, NOT NULL)
├── telefono (VARCHAR(20), NOT NULL)
├── email (VARCHAR(150), UNIQUE)
├── licencia_conducir (VARCHAR(20), UNIQUE, NOT NULL)
├── fecha_vencimiento_licencia (DATE, NOT NULL)
├── fecha_registro (TIMESTAMP, DEFAULT CURRENT_TIMESTAMP)
└── activo (BOOLEAN, DEFAULT TRUE)
\`\`\`

---

### 10. TARIFA
Configuración de tarifas del sistema.

\`\`\`
TARIFA
├── id (PK, BIGINT, AUTO_INCREMENT)
├── concepto (VARCHAR(50), NOT NULL) // GESTION_BASE, KM_BASE, COMBUSTIBLE_LITRO, etc.
├── descripcion (VARCHAR(255))
├── valor (DECIMAL(10,2), NOT NULL)
├── unidad (VARCHAR(20)) // FIJO, POR_KM, POR_LITRO, POR_DIA, etc.
├── rango_peso_min_kg (DECIMAL(10,2)) // Para tarifas por rango
├── rango_peso_max_kg (DECIMAL(10,2))
├── rango_volumen_min_m3 (DECIMAL(10,2))
├── rango_volumen_max_m3 (DECIMAL(10,2))
├── fecha_vigencia_desde (DATE, NOT NULL)
├── fecha_vigencia_hasta (DATE)
└── activo (BOOLEAN, DEFAULT TRUE)
\`\`\`

**Conceptos de tarifa:**
- GESTION_BASE: Cargo fijo por gestión
- KM_BASE: Costo base por kilómetro
- COMBUSTIBLE_LITRO: Precio del litro de combustible
- ESTADIA_DEPOSITO: Costo por día en depósito (puede estar en DEPOSITO también)

---

### 11. HISTORIAL_ESTADO_CONTENEDOR
Auditoría de cambios de estado del contenedor.

\`\`\`
HISTORIAL_ESTADO_CONTENEDOR
├── id (PK, BIGINT, AUTO_INCREMENT)
├── contenedor_id (FK -> CONTENEDOR.id, NOT NULL)
├── estado_anterior (VARCHAR(30))
├── estado_nuevo (VARCHAR(30), NOT NULL)
├── ubicacion_id (FK -> UBICACION.id)
├── tramo_id (FK -> TRAMO.id)
├── fecha_hora (TIMESTAMP, DEFAULT CURRENT_TIMESTAMP)
├── observaciones (TEXT)
└── usuario_registro (VARCHAR(100)) // Usuario que registró el cambio
\`\`\`

---

## Relaciones entre Entidades

### Relaciones Principales

1. **CLIENTE → CONTENEDOR** (1:N)
   - Un cliente puede tener múltiples contenedores
   - Un contenedor pertenece a un solo cliente

2. **CLIENTE → SOLICITUD** (1:N)
   - Un cliente puede tener múltiples solicitudes
   - Una solicitud pertenece a un solo cliente

3. **CONTENEDOR → SOLICITUD** (1:1)
   - Un contenedor tiene una solicitud asociada
   - Una solicitud es para un contenedor específico

4. **SOLICITUD → RUTA** (1:1)
   - Una solicitud tiene una ruta asignada
   - Una ruta pertenece a una solicitud

5. **RUTA → TRAMO** (1:N)
   - Una ruta tiene múltiples tramos
   - Un tramo pertenece a una ruta

6. **UBICACION → DEPOSITO** (1:1)
   - Un depósito tiene una ubicación
   - Una ubicación puede ser un depósito

7. **TRAMO → UBICACION** (2:N)
   - Un tramo tiene una ubicación de origen y una de destino
   - Una ubicación puede ser origen o destino de múltiples tramos

8. **CAMION → TRAMO** (1:N)
   - Un camión puede ser asignado a múltiples tramos (en diferentes momentos)
   - Un tramo tiene un camión asignado

9. **TRANSPORTISTA → CAMION** (1:N)
   - Un transportista puede tener múltiples camiones
   - Un camión pertenece a un transportista

10. **CONTENEDOR → HISTORIAL_ESTADO_CONTENEDOR** (1:N)
    - Un contenedor tiene múltiples registros de historial
    - Un registro de historial pertenece a un contenedor

---

## Índices Recomendados

Para optimizar las consultas más frecuentes:

\`\`\`sql
-- CONTENEDOR
CREATE INDEX idx_contenedor_cliente ON CONTENEDOR(cliente_id);
CREATE INDEX idx_contenedor_estado ON CONTENEDOR(estado);

-- SOLICITUD
CREATE INDEX idx_solicitud_cliente ON SOLICITUD(cliente_id);
CREATE INDEX idx_solicitud_estado ON SOLICITUD(estado);
CREATE INDEX idx_solicitud_fecha ON SOLICITUD(fecha_solicitud);

-- TRAMO
CREATE INDEX idx_tramo_ruta ON TRAMO(ruta_id);
CREATE INDEX idx_tramo_camion ON TRAMO(camion_id);
CREATE INDEX idx_tramo_estado ON TRAMO(estado);

-- CAMION
CREATE INDEX idx_camion_disponible ON CAMION(disponible);
CREATE INDEX idx_camion_transportista ON CAMION(transportista_id);

-- HISTORIAL
CREATE INDEX idx_historial_contenedor ON HISTORIAL_ESTADO_CONTENEDOR(contenedor_id);
CREATE INDEX idx_historial_fecha ON HISTORIAL_ESTADO_CONTENEDOR(fecha_hora);
\`\`\`

---

## Consideraciones de Diseño

1. **Normalización:** El diseño está en 3FN (Tercera Forma Normal) para evitar redundancia
2. **Auditoría:** Se incluye tabla de historial para seguimiento completo
3. **Soft Delete:** Uso de campos `activo` en lugar de eliminación física
4. **Timestamps:** Registro de fechas de creación y modificación
5. **Validaciones:** Las restricciones NOT NULL y UNIQUE garantizan integridad
6. **Escalabilidad:** Diseño preparado para crecimiento y particionamiento futuro
