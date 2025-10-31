# Análisis de Coherencia: Modelo de Servicios vs Base de Datos

## Introducción

Este documento analiza la coherencia entre:
1. La arquitectura de microservicios implementada (6 servicios + gateway)
2. El modelo de base de datos diseñado por el equipo
3. El código Java Spring Boot implementado

## Resumen Ejecutivo

**Estado general:** ⚠️ **Inconsistencia detectada**

**Problema principal:** El modelo de base de datos tiene una entidad `Ubicacion` centralizada que **NO existe en el código implementado** y que **viola el principio de bases de datos independientes** en microservicios.

---

## Mapeo: Microservicios → Entidades de BD

### 1. clientes-service (Puerto 8081)

**Entidades en el código:**
\`\`\`java
- Cliente (id, nombre, apellido, email, telefono, documento_tipo, documento_numero, direccion, activo)
\`\`\`

**Entidades en tu modelo de BD:**
\`\`\`
✅ Cliente (coincide)
\`\`\`

**Coherencia:** ✅ **CORRECTO**

---

### 2. depositos-service (Puerto 8082)

**Entidades en el código:**
\`\`\`java
- Deposito (id, nombre, direccion, latitud, longitud, costoDiarioEstadia, capacidadMaxima, capacidadActual, activo)
\`\`\`

**Entidades en tu modelo de BD:**
\`\`\`
✅ Deposito
❌ Ubicacion (relacionada con Deposito mediante ubicacion_Destino_ID)
\`\`\`

**Coherencia:** ⚠️ **INCONSISTENCIA DETECTADA**

**Problema:** 
- En el código, `Deposito` tiene sus propios campos de ubicación (latitud, longitud, direccion)
- En tu modelo, `Deposito` se relaciona con una entidad `Ubicacion` separada
- Esto crea **acoplamiento entre microservicios** si Ubicacion es compartida

**Solución:**
- Eliminar la relación con Ubicacion
- Mantener los campos de ubicación directamente en Deposito

---

### 3. camiones-service (Puerto 8083)

**Entidades en el código:**
\`\`\`java
- Camion (id, dominio, nombreTransportista, telefono, capacidadPeso, capacidadVolumen, 
         consumoCombustible, costoPorKm, disponible, activo)
\`\`\`

**Entidades en tu modelo de BD:**
\`\`\`
✅ Camion
✅ Transportista (separado)
\`\`\`

**Coherencia:** ⚠️ **DIFERENCIA DE DISEÑO**

**En el código:** Transportista está embebido en Camion (nombreTransportista, telefono)

**En tu modelo:** Transportista es una entidad separada

**Análisis:**
- Tu modelo es **más normalizado** (mejor para múltiples transportistas por camión)
- El código actual es **más simple** (suficiente si cada camión tiene un solo transportista)

**Recomendación:** 
Si en el negocio real un transportista puede manejar múltiples camiones o un camión puede ser manejado por múltiples transportistas, **tu modelo es superior**. Deberías actualizar el código para separar la entidad Transportista.

---

### 4. tarifas-service (Puerto 8084)

**Entidades en el código:**
\`\`\`java
- Tarifa (id, concepto, descripcion, valor, unidad, rangoDesde, rangoHasta, 
         vigenciaDesde, vigenciaHasta, activo)
\`\`\`

**Entidades en tu modelo de BD:**
\`\`\`
✅ Tarifa (coincide perfectamente)
\`\`\`

**Coherencia:** ✅ **CORRECTO**

---

### 5. solicitudes-service (Puerto 8085)

**Entidades en el código:**
\`\`\`java
- Solicitud (id, numeroSolicitud, clienteId, contenedor, estado, costoEstimado, 
            tiempoEstimado, costoFinal, tiempoReal, fechaCreacion, fechaActualizacion)
            
- Contenedor (id, identificacion, peso, volumen, estado, direccionOrigen, latitudOrigen,
              longitudOrigen, direccionDestino, latitudDestino, longitudDestino)
\`\`\`

**Entidades en tu modelo de BD:**
\`\`\`
✅ Solicitud
✅ Contenedor
✅ HistorialEstadoContenedor
❌ Ubicacion (relacionada con Solicitud mediante ubicacion_origen_ID y ubicacion_destino_ID)
\`\`\`

**Coherencia:** ⚠️ **INCONSISTENCIA DETECTADA**

**Problemas:**
1. **Falta `numeroSolicitud`** en tu modelo de Solicitud (campo crítico según enunciado)
2. **Ubicacion separada:** En el código, las coordenadas están directamente en Contenedor
3. **HistorialEstadoContenedor:** Existe en tu modelo pero NO en el código actual

**Soluciones:**
1. Agregar campo `numero_solicitud VARCHAR(50) UNIQUE NOT NULL` en Solicitud
2. Eliminar relaciones con Ubicacion, mantener campos directamente en Contenedor
3. Considerar implementar HistorialEstadoContenedor para tracking (mejora tu modelo)

---

### 6. rutas-service (Puerto 8086)

**Entidades en el código:**
\`\`\`java
- Ruta (id, solicitudId, cantidadTramos, cantidadDepositos, distanciaTotal, 
        costoEstimado, tiempoEstimado, fechaCreacion)
        
- Tramo (id, ruta, origenTipo, origenId, origenDireccion, origenLatitud, origenLongitud,
         destinoTipo, destinoId, destinoDireccion, destinoLatitud, destinoLongitud,
         tipoTramo, estado, distancia, costoAproximado, costoReal, 
         fechaHoraInicio, fechaHoraFin, camionId)
\`\`\`

**Entidades en tu modelo de BD:**
\`\`\`
✅ Ruta
✅ Tramo
❌ Ubicacion (relacionada con Tramo mediante ubicacion_origen_ID y ubicacion_Destino_ID)
\`\`\`

**Coherencia:** ⚠️ **INCONSISTENCIA DETECTADA**

**Problema:** 
- En el código, `Tramo` tiene campos de ubicación embebidos (origenLatitud, origenLongitud, etc.)
- En tu modelo, `Tramo` se relaciona con `Ubicacion` separada

**Solución:**
- Eliminar relaciones con Ubicacion
- Mantener campos de ubicación directamente en Tramo

---

## Problema Principal: Entidad Ubicacion Compartida

### ¿Por qué es un problema?

La entidad `Ubicacion` en tu modelo se relaciona con múltiples entidades de **diferentes microservicios**:

\`\`\`
Ubicacion ← Deposito (depositos-service)
Ubicacion ← Solicitud (solicitudes-service)  
Ubicacion ← Tramo (rutas-service)
\`\`\`

**Esto viola principios fundamentales de microservicios:**

1. **Acoplamiento de bases de datos:** Múltiples servicios dependerían de la misma tabla
2. **Pérdida de autonomía:** Un cambio en Ubicacion afecta a todos los servicios
3. **Imposibilidad de escalar independientemente:** Todos comparten la misma BD
4. **Violación de bounded contexts:** Cada servicio debe tener su propio modelo de datos

### ¿Qué hacer con Ubicacion?

**Opción 1: Eliminar la entidad Ubicacion (RECOMENDADO)**

Embeber los campos de ubicación directamente en cada entidad:

\`\`\`sql
-- En depositos-service
CREATE TABLE deposito (
    id BIGINT PRIMARY KEY,
    nombre VARCHAR(100),
    direccion VARCHAR(255),
    latitud DECIMAL(10,8),
    longitud DECIMAL(11,8),
    -- ... otros campos
);

-- En solicitudes-service (dentro de Contenedor)
CREATE TABLE contenedor (
    id BIGINT PRIMARY KEY,
    direccion_origen VARCHAR(255),
    latitud_origen DECIMAL(10,8),
    longitud_origen DECIMAL(11,8),
    direccion_destino VARCHAR(255),
    latitud_destino DECIMAL(10,8),
    longitud_destino DECIMAL(11,8),
    -- ... otros campos
);

-- En rutas-service
CREATE TABLE tramo (
    id BIGINT PRIMARY KEY,
    origen_direccion VARCHAR(255),
    origen_latitud DECIMAL(10,8),
    origen_longitud DECIMAL(11,8),
    destino_direccion VARCHAR(255),
    destino_latitud DECIMAL(10,8),
    destino_longitud DECIMAL(11,8),
    -- ... otros campos
);
\`\`\`

**Ventajas:**
- ✅ Cada microservicio es completamente independiente
- ✅ No hay acoplamiento entre servicios
- ✅ Más simple de implementar y mantener
- ✅ Alineado con el código actual

**Desventajas:**
- ❌ Duplicación de datos de ubicación
- ❌ Menos normalizado

**Opción 2: Ubicacion como entidad dentro de cada servicio**

Si realmente necesitas normalizar ubicaciones (por ejemplo, para evitar duplicados de direcciones), crea una tabla `ubicacion` **dentro de cada microservicio** que la necesite:

\`\`\`sql
-- En depositos-service
CREATE TABLE ubicacion_deposito (
    id BIGINT PRIMARY KEY,
    direccion VARCHAR(255),
    latitud DECIMAL(10,8),
    longitud DECIMAL(11,8)
);

CREATE TABLE deposito (
    id BIGINT PRIMARY KEY,
    nombre VARCHAR(100),
    ubicacion_id BIGINT REFERENCES ubicacion_deposito(id),
    -- ... otros campos
);
\`\`\`

**Ventajas:**
- ✅ Normalización dentro del bounded context
- ✅ Independencia entre microservicios

**Desventajas:**
- ❌ Más complejo
- ❌ Posible over-engineering para este caso de uso

---

## Modelo de BD Corregido por Microservicio

### Base de Datos: clientes_db

\`\`\`sql
CREATE TABLE cliente (
    cliente_id BIGINT PRIMARY KEY AUTO_INCREMENT,
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

### Base de Datos: depositos_db

\`\`\`sql
CREATE TABLE deposito (
    deposito_id BIGINT PRIMARY KEY AUTO_INCREMENT,
    nombre VARCHAR(100) NOT NULL,
    direccion VARCHAR(255) NOT NULL,
    latitud DECIMAL(10,8) NOT NULL,
    longitud DECIMAL(11,8) NOT NULL,
    costo_diario_estadia DECIMAL(10,2) NOT NULL,
    capacidad_maxima INTEGER,
    capacidad_actual INTEGER DEFAULT 0,
    activo BOOLEAN DEFAULT TRUE,
    fecha_creacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
\`\`\`

### Base de Datos: camiones_db

\`\`\`sql
-- Opción A: Transportista embebido (código actual)
CREATE TABLE camion (
    camion_id BIGINT PRIMARY KEY AUTO_INCREMENT,
    dominio VARCHAR(20) UNIQUE NOT NULL,
    nombre_transportista VARCHAR(100) NOT NULL,
    telefono VARCHAR(20) NOT NULL,
    capacidad_peso_kg DECIMAL(10,2) NOT NULL,
    capacidad_volumen_m3 DECIMAL(10,2) NOT NULL,
    consumo_combustible_km_litro DECIMAL(5,2) NOT NULL,
    costo_por_km DECIMAL(10,2) NOT NULL,
    disponible BOOLEAN DEFAULT TRUE,
    activo BOOLEAN DEFAULT TRUE
);

-- Opción B: Transportista separado (tu modelo - MEJOR)
CREATE TABLE transportista (
    transportista_id BIGINT PRIMARY KEY AUTO_INCREMENT,
    nombre VARCHAR(100) NOT NULL,
    apellido VARCHAR(100) NOT NULL,
    documento_tipo VARCHAR(10) NOT NULL,
    documento_numero VARCHAR(20) NOT NULL UNIQUE,
    telefono VARCHAR(20) NOT NULL,
    email VARCHAR(150),
    licencia_conducir VARCHAR(50) NOT NULL,
    fecha_vencimiento_licencia DATE,
    activo BOOLEAN DEFAULT TRUE
);

CREATE TABLE camion (
    camion_id BIGINT PRIMARY KEY AUTO_INCREMENT,
    dominio VARCHAR(20) UNIQUE NOT NULL,
    transportista_id BIGINT REFERENCES transportista(transportista_id),
    capacidad_peso_kg DECIMAL(10,2) NOT NULL,
    capacidad_volumen_m3 DECIMAL(10,2) NOT NULL,
    consumo_combustible_km_litro DECIMAL(5,2) NOT NULL,
    costo_por_km DECIMAL(10,2) NOT NULL,
    disponible BOOLEAN DEFAULT TRUE,
    activo BOOLEAN DEFAULT TRUE
);
\`\`\`

### Base de Datos: tarifas_db

\`\`\`sql
CREATE TABLE tarifa (
    tarifa_id BIGINT PRIMARY KEY AUTO_INCREMENT,
    concepto VARCHAR(50) NOT NULL,
    descripcion VARCHAR(255),
    valor DECIMAL(10,2) NOT NULL,
    unidad VARCHAR(20) NOT NULL,
    rango_peso_min_kg DECIMAL(10,2),
    rango_peso_max_kg DECIMAL(10,2),
    rango_volumen_min_m3 DECIMAL(10,2),
    rango_volumen_max_m3 DECIMAL(10,2),
    vigencia_desde DATE,
    vigencia_hasta DATE,
    activo BOOLEAN DEFAULT TRUE
);
\`\`\`

### Base de Datos: solicitudes_db

\`\`\`sql
CREATE TABLE contenedor (
    contenedor_id BIGINT PRIMARY KEY AUTO_INCREMENT,
    codigo_identificacion VARCHAR(50) UNIQUE NOT NULL,
    peso_kg DECIMAL(10,2) NOT NULL,
    volumen_m3 DECIMAL(10,2) NOT NULL,
    largo_m DECIMAL(5,2),
    ancho_m DECIMAL(5,2),
    alto_m DECIMAL(5,2),
    estado VARCHAR(30),
    descripcion TEXT,
    -- Ubicación de origen
    direccion_origen VARCHAR(255) NOT NULL,
    latitud_origen DECIMAL(10,8) NOT NULL,
    longitud_origen DECIMAL(11,8) NOT NULL,
    -- Ubicación de destino
    direccion_destino VARCHAR(255) NOT NULL,
    latitud_destino DECIMAL(10,8) NOT NULL,
    longitud_destino DECIMAL(11,8) NOT NULL,
    fecha_creacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE solicitud (
    solicitud_id BIGINT PRIMARY KEY AUTO_INCREMENT,
    numero_solicitud VARCHAR(50) UNIQUE NOT NULL, -- ⚠️ FALTABA EN TU MODELO
    cliente_id BIGINT NOT NULL,
    contenedor_id BIGINT REFERENCES contenedor(contenedor_id),
    estado VARCHAR(30) NOT NULL,
    costo_estimado DECIMAL(10,2),
    tiempo_estimado_horas INTEGER,
    costo_final DECIMAL(10,2),
    tiempo_real_horas INTEGER,
    fecha_solicitud DATE,
    fecha_programada DATE,
    fecha_creacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    fecha_actualizacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

-- MEJORA: Agregar historial de estados (de tu modelo)
CREATE TABLE historial_estado_contenedor (
    historial_id BIGINT PRIMARY KEY AUTO_INCREMENT,
    contenedor_id BIGINT REFERENCES contenedor(contenedor_id),
    estado_anterior VARCHAR(30),
    estado_nuevo VARCHAR(30),
    tramo_id BIGINT, -- Referencia al tramo actual
    fecha_hora TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    observaciones TEXT,
    usuario_registro VARCHAR(100)
);
\`\`\`

### Base de Datos: rutas_db

\`\`\`sql
CREATE TABLE ruta (
    ruta_id BIGINT PRIMARY KEY AUTO_INCREMENT,
    solicitud_id BIGINT UNIQUE NOT NULL,
    cantidad_tramos INTEGER NOT NULL DEFAULT 0,
    cantidad_depositos INTEGER NOT NULL DEFAULT 0,
    distancia_total_km DECIMAL(10,2),
    costo_total_estimado DECIMAL(10,2),
    costo_total_real DECIMAL(10,2),
    tiempo_estimado_horas INTEGER,
    estado VARCHAR(30),
    fecha_creacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE tramo (
    tramo_id BIGINT PRIMARY KEY AUTO_INCREMENT,
    ruta_id BIGINT REFERENCES ruta(ruta_id),
    numero_orden INTEGER NOT NULL,
    -- Origen
    origen_tipo VARCHAR(20) NOT NULL, -- 'ORIGEN', 'DEPOSITO'
    origen_id BIGINT,
    origen_direccion VARCHAR(255),
    origen_latitud DECIMAL(10,8),
    origen_longitud DECIMAL(11,8),
    -- Destino
    destino_tipo VARCHAR(20) NOT NULL, -- 'DEPOSITO', 'DESTINO'
    destino_id BIGINT,
    destino_direccion VARCHAR(255),
    destino_latitud DECIMAL(10,8),
    destino_longitud DECIMAL(11,8),
    -- Detalles del tramo
    tipo_tramo VARCHAR(30),
    estado VARCHAR(30),
    distancia_km DECIMAL(10,2),
    costo_aproximado DECIMAL(10,2),
    costo_real DECIMAL(10,2),
    fecha_hora_inicio TIMESTAMP,
    fecha_hora_fin TIMESTAMP,
    fecha_hora_inicio_estimado TIMESTAMP,
    fecha_hora_fin_estimado TIMESTAMP,
    camion_id BIGINT,
    observaciones TEXT,
    activo BOOLEAN DEFAULT TRUE
);
\`\`\`

---

## Checklist de Correcciones

### Correcciones Críticas (DEBEN hacerse)

- [ ] **Eliminar entidad Ubicacion compartida**
- [ ] **Agregar campo `numero_solicitud` en tabla Solicitud**
- [ ] **Embeber campos de ubicación en Deposito**
- [ ] **Embeber campos de ubicación en Contenedor (origen y destino)**
- [ ] **Embeber campos de ubicación en Tramo (origen y destino)**

### Mejoras Recomendadas (DEBERÍAN hacerse)

- [ ] **Separar entidad Transportista de Camion** (tu modelo es mejor)
- [ ] **Implementar tabla HistorialEstadoContenedor** (excelente para tracking)
- [ ] **Agregar campo `disponible` en Camion** (falta en tu modelo)
- [ ] **Agregar índices en campos de búsqueda frecuente**

### Mejoras Opcionales (PODRÍAN hacerse)

- [ ] Agregar campos de auditoría en todas las tablas (created_by, updated_by)
- [ ] Agregar soft delete en lugar de campo `activo`
- [ ] Considerar particionamiento de tablas grandes (historial)

---

## Diagrama Corregido: Distribución de Entidades por Microservicio

\`\`\`
┌─────────────────────────────────────────────────────────────────────┐
│                         API GATEWAY (Puerto 8080)                    │
│                     Enrutamiento y Autenticación                     │
└─────────────────────────────────────────────────────────────────────┘
                                    │
        ┌───────────────────────────┼───────────────────────────┐
        │                           │                           │
┌───────▼────────┐         ┌────────▼────────┐        ┌────────▼────────┐
│ clientes-      │         │ depositos-      │        │ camiones-       │
│ service        │         │ service         │        │ service         │
│ (8081)         │         │ (8082)          │        │ (8083)          │
├────────────────┤         ├─────────────────┤        ├─────────────────┤
│ BD: clientes_db│         │ BD: depositos_db│        │ BD: camiones_db │
├────────────────┤         ├─────────────────┤        ├─────────────────┤
│ • Cliente      │         │ • Deposito      │        │ • Transportista │
│                │         │   - direccion   │        │ • Camion        │
│                │         │   - latitud     │        │   - disponible  │
│                │         │   - longitud    │        │                 │
└────────────────┘         └─────────────────┘        └─────────────────┘

        ┌───────────────────────────┼───────────────────────────┐
        │                           │                           │
┌───────▼────────┐         ┌────────▼────────┐        ┌────────▼────────┐
│ tarifas-       │         │ solicitudes-    │        │ rutas-          │
│ service        │         │ service         │        │ service         │
│ (8084)         │         │ (8085)          │        │ (8086)          │
├────────────────┤         ├─────────────────┤        ├─────────────────┤
│ BD: tarifas_db │         │ BD:solicitudes  │        │ BD: rutas_db    │
├────────────────┤         ├─────────────────┤        ├─────────────────┤
│ • Tarifa       │         │ • Solicitud     │        │ • Ruta          │
│   - rangos     │         │   - numero_sol  │        │ • Tramo         │
│   - vigencia   │         │ • Contenedor    │        │   - origen_*    │
│                │         │   - origen_*    │        │   - destino_*   │
│                │         │   - destino_*   │        │                 │
│                │         │ • HistorialEst  │        │                 │
└────────────────┘         └─────────────────┘        └─────────────────┘

Leyenda:
• origen_* = origen_direccion, origen_latitud, origen_longitud
• destino_* = destino_direccion, destino_latitud, destino_longitud
\`\`\`

---

## Respuesta a tu Pregunta

> "¿Están bien el modelado de servicios (archivo 14) y el modelo de base de datos en relación al sistema construido?"

**Respuesta corta:** ⚠️ **Parcialmente correcto, con inconsistencias importantes**

**Respuesta detallada:**

### Modelado de Servicios (Archivo 14) ✅

El modelado de servicios está **CORRECTO** y alineado con el código:
- 6 microservicios funcionales + API Gateway
- Separación clara de responsabilidades
- Comunicación bien definida
- Alineado con el código implementado

### Modelo de Base de Datos ⚠️

Tu modelo de BD tiene **fortalezas** pero también **problemas críticos**:

**Fortalezas (8/10):**
- ✅ Todas las entidades principales están presentes
- ✅ Relaciones bien definidas
- ✅ Separación de Transportista (mejor que el código actual)
- ✅ HistorialEstadoContenedor (excelente adición)
- ✅ Campos de auditoría

**Problemas críticos:**
- ❌ **Entidad Ubicacion compartida** viola principios de microservicios
- ❌ **Falta `numero_solicitud`** en Solicitud (requerimiento del enunciado)
- ❌ **Falta `disponible`** en Camion (necesario para asignación)
- ❌ **No alineado con el código** en cuanto a ubicaciones

### Calificación Final

| Aspecto | Calificación | Comentario |
|---------|--------------|------------|
| Modelado de Servicios | 9.5/10 | Excelente, alineado con código |
| Modelo de BD - Estructura | 8/10 | Buena normalización y relaciones |
| Modelo de BD - Coherencia con Microservicios | 5/10 | Ubicacion compartida es problema crítico |
| Modelo de BD - Completitud | 7/10 | Faltan campos críticos |
| **TOTAL** | **7.4/10** | Bueno, pero necesita correcciones |

---

## Recomendaciones Finales

### Para la Entrega Inicial

1. **Actualizar el DER eliminando Ubicacion compartida**
2. **Agregar campos faltantes** (numero_solicitud, disponible)
3. **Crear un DER por microservicio** mostrando independencia de BDs
4. **Documentar la decisión** de embeber ubicaciones vs normalizarlas

### Para la Defensa

**Pregunta esperada:** "¿Por qué no normalizaron las ubicaciones en una tabla separada?"

**Respuesta preparada:**
> "Consideramos crear una entidad Ubicacion centralizada para normalizar direcciones y coordenadas. Sin embargo, esto violaría el principio de bases de datos independientes en microservicios, creando acoplamiento entre servicios. Decidimos embeber los campos de ubicación directamente en cada entidad (Deposito, Contenedor, Tramo) para mantener la autonomía de cada microservicio. Esto permite que cada servicio escale independientemente y no dependa de otros para operaciones básicas. La duplicación de datos es un trade-off aceptable en arquitecturas de microservicios para lograr desacoplamiento."

### Para el Código

Si quieren mejorar el código para alinearlo con tu modelo:

1. **Separar Transportista de Camion** (tu modelo es mejor)
2. **Implementar HistorialEstadoContenedor** (excelente para tracking)
3. **Agregar numero_solicitud** en Solicitud

---

## Conclusión

Tu modelo de base de datos muestra **buen entendimiento de normalización y diseño relacional**, pero necesita ajustes para alinearse con los **principios de microservicios** y el **código implementado**.

Las correcciones son **menores y rápidas de hacer**. Con los ajustes sugeridos, tendrás un modelo **9/10** listo para defender.

**Próximos pasos:**
1. Aplicar correcciones del checklist
2. Crear DER separado por microservicio
3. Actualizar documentación
4. Preparar respuestas para la defensa
