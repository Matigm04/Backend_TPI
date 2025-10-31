# Análisis del Modelo de Base de Datos Actualizado

## Resumen Ejecutivo

**Calificación del modelo actualizado: 8.5/10** ⬆️ (mejora desde 7.4/10)

Tu modelo ha mejorado significativamente al eliminar la tabla Ubicacion centralizada del diagrama. Sin embargo, aún existen **referencias a `ubicacion_id`** en varias tablas que necesitan ser corregidas para lograr total coherencia con la arquitectura de microservicios implementada.

---

## Comparación con Modelo de Microservicios (Archivo 14)

### Mapeo Entidades → Microservicios

| Microservicio | Entidades en tu modelo | Estado |
|---------------|------------------------|---------|
| **clientes-service** | Cliente | ✅ Correcto |
| **depositos-service** | Deposito | ✅ Correcto |
| **camiones-service** | Camion, Transportista | ✅ Correcto |
| **tarifas-service** | Tarifa | ✅ Correcto |
| **solicitudes-service** | Solicitud, Contenedor, HistorialEstadoContenedor | ⚠️ Necesita ajustes |
| **rutas-service** | Ruta, Tramo | ⚠️ Necesita ajustes |

---

## Problemas Identificados

### 1. Referencias a `ubicacion_id` sin tabla Ubicacion visible ❌

**Tablas afectadas:**
- `Solicitud`: tiene `ubicacion_origen_id` y `ubicacion_destino_id`
- `Tramo`: tiene `ubicacion_origen_id` y `ubicacion_destino_id`
- `Deposito`: tiene `ubicacion_id`
- `HistorialEstadoContenedor`: tiene `ubicacion_id`

**Problema:**
- Si la tabla `Ubicacion` existe pero no está en el diagrama → falta documentarla
- Si la tabla `Ubicacion` NO existe → las referencias están rotas
- Si la tabla `Ubicacion` es compartida entre microservicios → viola principios de microservicios

**Solución:**
Eliminar todas las referencias a `ubicacion_id` y usar campos directos en cada tabla.

---

### 2. Contenedor tiene `deposito_ID` ❌

**Problema:**
Esto crea acoplamiento directo entre `solicitudes-service` y `depositos-service`. El servicio de solicitudes necesitaría consultar constantemente al servicio de depósitos.

**Solución:**
La ubicación actual del contenedor debe manejarse a través de:
- El `Tramo` activo (que indica si está en tránsito)
- El `HistorialEstadoContenedor` (que registra cuando entra/sale de un depósito)

---

### 3. Falta el campo `numero_orden` en Tramo ⚠️

**Estado:** ✅ Ya lo tienes en tu modelo (bien hecho)

---

## Modelo Corregido por Microservicio

### 1. clientes-service

\`\`\`sql
CREATE TABLE Cliente (
    cliente_ID BIGINT PRIMARY KEY AUTO_INCREMENT,
    nombre VARCHAR(100) NOT NULL,
    apellido VARCHAR(100) NOT NULL,
    email VARCHAR(150) NOT NULL UNIQUE,
    telefono VARCHAR(20) NOT NULL,
    documento_tipo VARCHAR(10) NOT NULL,
    documento_numero VARCHAR(20) NOT NULL UNIQUE,
    direccion VARCHAR(255) NOT NULL,
    fecha_registro TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    activo BOOLEAN DEFAULT TRUE
);
\`\`\`

**Estado:** ✅ Perfecto, sin cambios necesarios

---

### 2. depositos-service

\`\`\`sql
CREATE TABLE Deposito (
    deposito_ID BIGINT PRIMARY KEY AUTO_INCREMENT,
    nombre VARCHAR(100) NOT NULL,                    -- Agregar nombre
    direccion VARCHAR(255) NOT NULL,                 -- Agregar dirección
    latitud DECIMAL(10,8) NOT NULL,                  -- Agregar latitud
    longitud DECIMAL(11,8) NOT NULL,                 -- Agregar longitud
    capacidad_maxima_contenedores INTEGER,
    contenedores_actuales INTEGER DEFAULT 0,
    costo_estadia_diario DECIMAL(10,2) NOT NULL,
    horario_apertura TIME,
    horario_cierre TIME,
    observaciones TEXT,
    activo BOOLEAN DEFAULT TRUE,
    fecha_creacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
\`\`\`

**Cambios necesarios:**
- ❌ Eliminar `ubicacion_id`
- ✅ Agregar `nombre`, `direccion`, `latitud`, `longitud` directamente

---

### 3. camiones-service

\`\`\`sql
CREATE TABLE Transportista (
    transportista_ID BIGINT PRIMARY KEY AUTO_INCREMENT,
    nombre VARCHAR(100) NOT NULL,
    apellido VARCHAR(100) NOT NULL,
    documento_tipo VARCHAR(10) NOT NULL,
    documento_numero VARCHAR(20) NOT NULL UNIQUE,
    telefono VARCHAR(20),
    email VARCHAR(150),
    licencia_conducir VARCHAR(20),
    fecha_vencimiento_licencia DATE,
    fecha_registro TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    activo BOOLEAN DEFAULT TRUE
);

CREATE TABLE Camion (
    camion_ID BIGINT PRIMARY KEY AUTO_INCREMENT,
    dominio VARCHAR(10) UNIQUE NOT NULL,
    marca VARCHAR(50),
    modelo VARCHAR(50),
    anio INTEGER,                                    -- Renombrar de 'año' a 'anio'
    capacidad_peso_kg DECIMAL(10,2) NOT NULL,
    capacidad_volumen_m3 DECIMAL(10,2) NOT NULL,
    consumo_combustible_km_litro DECIMAL(5,2),       -- Renombrar para claridad
    costo_base_por_km DECIMAL(10,2) NOT NULL,
    disponible BOOLEAN DEFAULT TRUE,
    transportista_id BIGINT NOT NULL,
    fecha_registro TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    activo BOOLEAN DEFAULT TRUE,
    FOREIGN KEY (transportista_id) REFERENCES Transportista(transportista_ID)
);
\`\`\`

**Estado:** ✅ Muy bien estructurado

---

### 4. tarifas-service

\`\`\`sql
CREATE TABLE Tarifa (
    tarifa_ID BIGINT PRIMARY KEY AUTO_INCREMENT,
    concepto VARCHAR(50) NOT NULL,
    descripcion VARCHAR(255),
    valor DECIMAL(10,2) NOT NULL,
    unidad VARCHAR(20) NOT NULL,
    rango_peso_min_kg DECIMAL(10,2),
    rango_peso_max_kg DECIMAL(10,2),
    rango_volumen_min_m3 DECIMAL(10,2),
    rango_volumen_max_m3 DECIMAL(10,2),
    fecha_vigencia_desde DATE,
    fecha_vigencia_hasta DATE,
    activo BOOLEAN DEFAULT TRUE
);
\`\`\`

**Estado:** ✅ Perfecto

---

### 5. solicitudes-service

\`\`\`sql
CREATE TABLE Contenedor (
    contenedor_ID BIGINT PRIMARY KEY AUTO_INCREMENT,
    codigo_identificacion VARCHAR(50) UNIQUE NOT NULL,
    peso_kg DECIMAL(10,2) NOT NULL,
    volumen_m3 DECIMAL(10,2) NOT NULL,
    largo_m DECIMAL(5,2),
    ancho_m DECIMAL(5,2),
    alto_m DECIMAL(5,2),
    estado VARCHAR(30) NOT NULL,                     -- Agregar estado actual
    descripcion TEXT,
    fecha_creacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP
    -- ELIMINAR deposito_ID
);

CREATE TABLE Solicitud (
    solicitud_ID BIGINT PRIMARY KEY AUTO_INCREMENT,
    numero_solicitud VARCHAR(20) UNIQUE NOT NULL,
    contenedor_id BIGINT NOT NULL,
    cliente_id BIGINT NOT NULL,
    -- ELIMINAR ubicacion_origen_id y ubicacion_destino_id
    -- AGREGAR campos directos de ubicación
    direccion_origen VARCHAR(255) NOT NULL,
    latitud_origen DECIMAL(10,8) NOT NULL,
    longitud_origen DECIMAL(11,8) NOT NULL,
    direccion_destino VARCHAR(255) NOT NULL,
    latitud_destino DECIMAL(10,8) NOT NULL,
    longitud_destino DECIMAL(11,8) NOT NULL,
    estado VARCHAR(30) NOT NULL,
    fecha_solicitud TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    fecha_programada DATE,
    fecha_entrega_estimada TIMESTAMP,
    fecha_entrega_real TIMESTAMP,
    costo_estimado DECIMAL(10,2),
    costo_final DECIMAL(10,2),
    tiempo_estimado_horas INTEGER,
    tiempo_real_horas INTEGER,
    observaciones TEXT,
    activo BOOLEAN DEFAULT TRUE,
    tarifa_ID BIGINT,
    FOREIGN KEY (contenedor_id) REFERENCES Contenedor(contenedor_ID),
    FOREIGN KEY (cliente_id) REFERENCES Cliente(cliente_ID),      -- Referencia externa
    FOREIGN KEY (tarifa_ID) REFERENCES Tarifa(tarifa_ID)          -- Referencia externa
);

CREATE TABLE HistorialEstadoContenedor (
    historialEstadoContenedor_ID BIGINT PRIMARY KEY AUTO_INCREMENT,
    contenedor_id BIGINT NOT NULL,
    estado_anterior VARCHAR(30),
    estado_nuevo VARCHAR(30) NOT NULL,
    -- ELIMINAR ubicacion_id
    -- AGREGAR campos opcionales de ubicación
    descripcion_ubicacion VARCHAR(255),              -- Ej: "Depósito Central", "En tránsito"
    tramo_id BIGINT,
    fecha_hora TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    observaciones TEXT,
    usuario_registro VARCHAR(100),
    FOREIGN KEY (contenedor_id) REFERENCES Contenedor(contenedor_ID),
    FOREIGN KEY (tramo_id) REFERENCES Tramo(tramo_ID)              -- Referencia externa
);
\`\`\`

**Cambios críticos:**
- ❌ Eliminar `deposito_ID` de Contenedor
- ❌ Eliminar `ubicacion_origen_id` y `ubicacion_destino_id` de Solicitud
- ✅ Agregar campos directos de ubicación en Solicitud
- ❌ Eliminar `ubicacion_id` de HistorialEstadoContenedor
- ✅ Agregar `descripcion_ubicacion` en HistorialEstadoContenedor

---

### 6. rutas-service

\`\`\`sql
CREATE TABLE Ruta (
    ruta_ID BIGINT PRIMARY KEY AUTO_INCREMENT,
    solicitud_id BIGINT NOT NULL,
    cantidad_tramos INTEGER NOT NULL,
    cantidad_depositos INTEGER DEFAULT 0,
    distancia_total_km DECIMAL(10,2),
    costo_total_estimado DECIMAL(10,2),
    costo_total_real DECIMAL(10,2),
    fecha_creacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    estado VARCHAR(30) NOT NULL,
    FOREIGN KEY (solicitud_id) REFERENCES Solicitud(solicitud_ID)  -- Referencia externa
);

CREATE TABLE Tramo (
    tramo_ID BIGINT PRIMARY KEY AUTO_INCREMENT,
    ruta_id BIGINT NOT NULL,
    numero_orden INTEGER NOT NULL,
    -- ELIMINAR ubicacion_origen_id y ubicacion_destino_id
    -- AGREGAR campos directos de ubicación
    origen_tipo VARCHAR(20) NOT NULL,                -- 'ORIGEN', 'DEPOSITO'
    origen_id BIGINT,                                 -- ID del depósito si aplica
    origen_direccion VARCHAR(255) NOT NULL,
    origen_latitud DECIMAL(10,8) NOT NULL,
    origen_longitud DECIMAL(11,8) NOT NULL,
    destino_tipo VARCHAR(20) NOT NULL,               -- 'DEPOSITO', 'DESTINO'
    destino_id BIGINT,                               -- ID del depósito si aplica
    destino_direccion VARCHAR(255) NOT NULL,
    destino_latitud DECIMAL(10,8) NOT NULL,
    destino_longitud DECIMAL(11,8) NOT NULL,
    tipo_tramo VARCHAR(30) NOT NULL,
    estado VARCHAR(30) NOT NULL,
    distancia_km DECIMAL(10,2),
    costo_aproximado DECIMAL(10,2),
    costo_real DECIMAL(10,2),
    fecha_hora_inicio_estimada TIMESTAMP,
    fecha_hora_fin_estimada TIMESTAMP,
    fecha_hora_inicio_real TIMESTAMP,
    fecha_hora_fin_real TIMESTAMP,
    observaciones TEXT,
    camion_id BIGINT,
    FOREIGN KEY (ruta_id) REFERENCES Ruta(ruta_ID),
    FOREIGN KEY (camion_id) REFERENCES Camion(camion_ID)           -- Referencia externa
);
\`\`\`

**Cambios críticos:**
- ❌ Eliminar `ubicacion_origen_id` y `ubicacion_destino_id`
- ✅ Agregar campos directos de origen y destino con tipo, dirección y coordenadas
- ✅ Mantener `origen_id` y `destino_id` opcionales para referenciar depósitos cuando aplique

---

## Checklist de Correcciones

### Prioridad Alta (Crítico)

- [ ] **Eliminar todas las referencias a `ubicacion_id`** en:
  - [ ] Solicitud (ubicacion_origen_id, ubicacion_destino_id)
  - [ ] Tramo (ubicacion_origen_id, ubicacion_destino_id)
  - [ ] Deposito (ubicacion_id)
  - [ ] HistorialEstadoContenedor (ubicacion_id)

- [ ] **Agregar campos de ubicación directos en Solicitud:**
  - [ ] direccion_origen, latitud_origen, longitud_origen
  - [ ] direccion_destino, latitud_destino, longitud_destino

- [ ] **Agregar campos de ubicación directos en Tramo:**
  - [ ] origen_tipo, origen_id, origen_direccion, origen_latitud, origen_longitud
  - [ ] destino_tipo, destino_id, destino_direccion, destino_latitud, destino_longitud

- [ ] **Agregar campos de ubicación directos en Deposito:**
  - [ ] nombre, direccion, latitud, longitud

- [ ] **Eliminar `deposito_ID` de Contenedor**

- [ ] **Agregar `estado` en Contenedor**

### Prioridad Media

- [ ] Agregar `descripcion_ubicacion` en HistorialEstadoContenedor
- [ ] Renombrar `año` a `anio` en Camion (evitar palabras reservadas)
- [ ] Verificar índices en campos de búsqueda frecuente

---

## Comparación: Antes vs Después

| Aspecto | Modelo Anterior | Modelo Actual | Modelo Corregido |
|---------|----------------|---------------|------------------|
| **Tabla Ubicacion centralizada** | ❌ Existía | ⚠️ No visible pero referenciada | ✅ Eliminada completamente |
| **Acoplamiento entre servicios** | ❌ Alto | ⚠️ Medio | ✅ Bajo |
| **Coherencia con microservicios** | ❌ 7.4/10 | ⚠️ 8.5/10 | ✅ 9.5/10 |
| **Referencias externas** | ❌ Muchas | ⚠️ Algunas | ✅ Solo IDs necesarios |
| **Campos de ubicación** | ❌ Centralizados | ⚠️ Referenciados | ✅ Directos en cada tabla |

---

## Respuestas para la Defensa

### P: ¿Por qué eliminaron la tabla Ubicacion?

**R:** "Decidimos eliminar la tabla Ubicacion centralizada porque violaba el principio de bases de datos independientes por microservicio. En una arquitectura de microservicios, cada servicio debe ser autónomo y no depender de tablas compartidas. En su lugar, cada entidad que necesita información de ubicación (Solicitud, Tramo, Deposito) tiene sus propios campos de dirección, latitud y longitud. Esto permite que cada microservicio funcione de manera independiente sin acoplamiento con otros servicios."

### P: ¿Por qué Contenedor no tiene deposito_ID?

**R:** "El campo deposito_ID en Contenedor crearía un acoplamiento directo entre solicitudes-service y depositos-service. En su lugar, la ubicación actual del contenedor se gestiona a través de:
1. El Tramo activo que indica si está en tránsito o en un depósito
2. El HistorialEstadoContenedor que registra todos los movimientos
3. El campo estado en Contenedor que indica su estado general

Esta arquitectura permite que el servicio de solicitudes funcione sin necesidad de consultar constantemente al servicio de depósitos."

### P: ¿Cómo manejan las ubicaciones sin una tabla centralizada?

**R:** "Cada entidad almacena directamente la información de ubicación que necesita:
- **Solicitud**: Tiene origen y destino con dirección y coordenadas
- **Tramo**: Tiene origen y destino con tipo (ORIGEN/DEPOSITO/DESTINO), dirección y coordenadas
- **Deposito**: Tiene su propia dirección y coordenadas
- **HistorialEstadoContenedor**: Tiene una descripción textual de la ubicación

Esto elimina la necesidad de joins entre microservicios y permite que cada servicio sea completamente autónomo."

### P: ¿No hay duplicación de datos?

**R:** "Sí, hay cierta duplicación de datos, pero es intencional y aceptable en arquitecturas de microservicios. Los beneficios superan los costos:
- **Ventajas**: Autonomía de servicios, sin acoplamiento, mejor rendimiento (sin joins entre servicios), escalabilidad independiente
- **Desventajas**: Duplicación de datos, mayor espacio de almacenamiento
- **Mitigación**: Usamos eventos y sincronización eventual cuando es necesario actualizar datos duplicados"

---

## Calificación Final

### Modelo Actual: 8.5/10

**Fortalezas:**
- ✅ Eliminaste la tabla Ubicacion del diagrama
- ✅ Agrupación correcta de entidades por microservicio
- ✅ Campos completos y bien tipados
- ✅ Relaciones correctas entre entidades del mismo servicio
- ✅ Campos de auditoría (fecha_creacion, activo)

**Áreas de mejora:**
- ⚠️ Todavía existen referencias a ubicacion_id
- ⚠️ Contenedor tiene deposito_ID
- ⚠️ Faltan campos directos de ubicación en varias tablas

### Modelo Corregido: 9.5/10

Con las correcciones propuestas, tu modelo alcanzaría 9.5/10 y estaría perfectamente alineado con la arquitectura de microservicios implementada.

---

## Próximos Pasos

1. **Aplicar las correcciones del checklist** (Prioridad Alta)
2. **Actualizar el diagrama DER** con los cambios
3. **Validar con el código implementado** (comparar con las entidades Java)
4. **Preparar la defensa** usando las respuestas proporcionadas
5. **Crear scripts SQL** con el modelo corregido

---

## Conclusión

Tu modelo ha mejorado significativamente y está muy cerca de ser perfecto. Las correcciones necesarias son claras y específicas. Una vez aplicadas, tendrás un modelo de base de datos que:
- ✅ Está perfectamente alineado con la arquitectura de microservicios
- ✅ Respeta el principio de bases de datos independientes
- ✅ Minimiza el acoplamiento entre servicios
- ✅ Es defendible con argumentos sólidos

**Recomendación:** Aplica las correcciones del checklist de Prioridad Alta y estarás listo para la entrega y defensa del TPI.
