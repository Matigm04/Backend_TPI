# Modelo de Base de Datos - Sistema de Logística de Contenedores

## 1. Diagrama Entidad-Relación (DER)

### Entidades Principales

#### 1.1 Cliente
\`\`\`sql
CREATE TABLE Cliente (
    cliente_id BIGINT PRIMARY KEY AUTO_INCREMENT,
    nombre VARCHAR(100) NOT NULL,
    apellido VARCHAR(100) NOT NULL,
    email VARCHAR(150) NOT NULL UNIQUE,
    telefono VARCHAR(20) NOT NULL,
    documento_tipo VARCHAR(10) NOT NULL,
    documento_numero VARCHAR(20) NOT NULL UNIQUE,
    direccion VARCHAR(255) NOT NULL,
    fecha_ingreso TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    activo BOOLEAN DEFAULT TRUE
);

CREATE INDEX idx_cliente_email ON Cliente(email);
CREATE INDEX idx_cliente_documento ON Cliente(documento_numero);
\`\`\`

#### 1.2 Contenedor
\`\`\`sql
CREATE TABLE Contenedor (
    contenedor_id BIGINT PRIMARY KEY AUTO_INCREMENT,
    codigo_identificacion VARCHAR(50) UNIQUE NOT NULL,
    peso_kg DECIMAL(10,2) NOT NULL,
    volumen_m3 DECIMAL(10,2) NOT NULL,
    largo_m DECIMAL(5,2),
    ancho_m DECIMAL(5,2),
    alto_m DECIMAL(5,2),
    estado VARCHAR(30) NOT NULL, -- EN_ORIGEN, EN_TRANSITO, EN_DEPOSITO, ENTREGADO
    descripcion TEXT,
    fecha_creacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    CONSTRAINT chk_peso CHECK (peso_kg > 0),
    CONSTRAINT chk_volumen CHECK (volumen_m3 > 0)
);

CREATE INDEX idx_contenedor_codigo ON Contenedor(codigo_identificacion);
CREATE INDEX idx_contenedor_estado ON Contenedor(estado);
\`\`\`

#### 1.3 Solicitud
\`\`\`sql
CREATE TABLE Solicitud (
    solicitud_id BIGINT PRIMARY KEY AUTO_INCREMENT,
    numero_solicitud VARCHAR(20) UNIQUE NOT NULL,
    cliente_id BIGINT NOT NULL,
    contenedor_id BIGINT NOT NULL,
    
    -- Ubicación de origen
    direccion_origen VARCHAR(255) NOT NULL,
    latitud_origen DECIMAL(10,8) NOT NULL,
    longitud_origen DECIMAL(11,8) NOT NULL,
    
    -- Ubicación de destino
    direccion_destino VARCHAR(255) NOT NULL,
    latitud_destino DECIMAL(10,8) NOT NULL,
    longitud_destino DECIMAL(11,8) NOT NULL,
    
    -- Estado y costos
    estado VARCHAR(30) NOT NULL, -- BORRADOR, PROGRAMADA, EN_TRANSITO, ENTREGADA
    costo_estimado DECIMAL(10,2),
    tiempo_estimado_horas INTEGER,
    costo_final DECIMAL(10,2),
    tiempo_real_horas INTEGER,
    
    -- Auditoría
    fecha_solicitud TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    fecha_entrega_estimada TIMESTAMP,
    fecha_entrega_real TIMESTAMP,
    observaciones TEXT,
    activo BOOLEAN DEFAULT TRUE,
    
    FOREIGN KEY (cliente_id) REFERENCES Cliente(cliente_id),
    FOREIGN KEY (contenedor_id) REFERENCES Contenedor(contenedor_id),
    
    CONSTRAINT chk_coordenadas_origen CHECK (
        latitud_origen BETWEEN -90 AND 90 AND 
        longitud_origen BETWEEN -180 AND 180
    ),
    CONSTRAINT chk_coordenadas_destino CHECK (
        latitud_destino BETWEEN -90 AND 90 AND 
        longitud_destino BETWEEN -180 AND 180
    )
);

CREATE INDEX idx_solicitud_numero ON Solicitud(numero_solicitud);
CREATE INDEX idx_solicitud_cliente ON Solicitud(cliente_id);
CREATE INDEX idx_solicitud_estado ON Solicitud(estado);
CREATE INDEX idx_solicitud_fecha ON Solicitud(fecha_solicitud);
\`\`\`

#### 1.4 Ruta
\`\`\`sql
CREATE TABLE Ruta (
    ruta_id BIGINT PRIMARY KEY AUTO_INCREMENT,
    solicitud_id BIGINT UNIQUE NOT NULL,
    cantidad_tramos INTEGER NOT NULL DEFAULT 0,
    cantidad_depositos INTEGER NOT NULL DEFAULT 0,
    distancia_total_km DECIMAL(10,2),
    costo_total_estimado DECIMAL(10,2),
    costo_total_real DECIMAL(10,2),
    tiempo_estimado_horas INTEGER,
    fecha_creacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    estado VARCHAR(30) DEFAULT 'ESTIMADA', -- ESTIMADA, ASIGNADA, EN_CURSO, COMPLETADA
    
    FOREIGN KEY (solicitud_id) REFERENCES Solicitud(solicitud_id),
    
    CONSTRAINT chk_cantidad_tramos CHECK (cantidad_tramos >= 0),
    CONSTRAINT chk_cantidad_depositos CHECK (cantidad_depositos >= 0)
);

CREATE INDEX idx_ruta_solicitud ON Ruta(solicitud_id);
CREATE INDEX idx_ruta_estado ON Ruta(estado);
\`\`\`

#### 1.5 Tramo
\`\`\`sql
CREATE TABLE Tramo (
    tramo_id BIGINT PRIMARY KEY AUTO_INCREMENT,
    ruta_id BIGINT NOT NULL,
    numero_orden INTEGER NOT NULL,
    
    -- Origen del tramo
    origen_tipo VARCHAR(20) NOT NULL, -- ORIGEN, DEPOSITO
    origen_id BIGINT, -- ID del depósito si aplica
    origen_direccion VARCHAR(255) NOT NULL,
    origen_latitud DECIMAL(10,8) NOT NULL,
    origen_longitud DECIMAL(11,8) NOT NULL,
    
    -- Destino del tramo
    destino_tipo VARCHAR(20) NOT NULL, -- DEPOSITO, DESTINO
    destino_id BIGINT, -- ID del depósito si aplica
    destino_direccion VARCHAR(255) NOT NULL,
    destino_latitud DECIMAL(10,8) NOT NULL,
    destino_longitud DECIMAL(11,8) NOT NULL,
    
    -- Tipo de tramo
    tipo_tramo VARCHAR(30) NOT NULL, -- ORIGEN_DEPOSITO, DEPOSITO_DEPOSITO, DEPOSITO_DESTINO, ORIGEN_DESTINO
    
    -- Estado y métricas
    estado VARCHAR(30) NOT NULL DEFAULT 'ESTIMADO', -- ESTIMADO, ASIGNADO, INICIADO, FINALIZADO
    distancia_km DECIMAL(10,2),
    costo_aproximado DECIMAL(10,2),
    costo_real DECIMAL(10,2),
    
    -- Camión asignado
    camion_id BIGINT,
    
    -- Fechas
    fecha_hora_inicio_estimada TIMESTAMP,
    fecha_hora_fin_estimada TIMESTAMP,
    fecha_hora_inicio_real TIMESTAMP,
    fecha_hora_fin_real TIMESTAMP,
    
    observaciones TEXT,
    activo BOOLEAN DEFAULT TRUE,
    
    FOREIGN KEY (ruta_id) REFERENCES Ruta(ruta_id),
    FOREIGN KEY (camion_id) REFERENCES Camion(camion_id),
    
    CONSTRAINT chk_numero_orden CHECK (numero_orden > 0)
);

CREATE INDEX idx_tramo_ruta ON Tramo(ruta_id);
CREATE INDEX idx_tramo_camion ON Tramo(camion_id);
CREATE INDEX idx_tramo_estado ON Tramo(estado);
CREATE INDEX idx_tramo_orden ON Tramo(ruta_id, numero_orden);
\`\`\`

#### 1.6 Deposito
\`\`\`sql
CREATE TABLE Deposito (
    deposito_id BIGINT PRIMARY KEY AUTO_INCREMENT,
    nombre VARCHAR(100) NOT NULL,
    direccion VARCHAR(255) NOT NULL,
    latitud DECIMAL(10,8) NOT NULL,
    longitud DECIMAL(11,8) NOT NULL,
    capacidad_maxima_contenedores INTEGER,
    contenedores_actuales INTEGER DEFAULT 0,
    costo_estadia_diario DECIMAL(10,2) NOT NULL,
    horario_apertura TIME,
    horario_cierre TIME,
    observaciones TEXT,
    activo BOOLEAN DEFAULT TRUE,
    
    CONSTRAINT chk_coordenadas CHECK (
        latitud BETWEEN -90 AND 90 AND 
        longitud BETWEEN -180 AND 180
    ),
    CONSTRAINT chk_capacidad CHECK (
        contenedores_actuales <= capacidad_maxima_contenedores
    )
);

CREATE INDEX idx_deposito_ubicacion ON Deposito(latitud, longitud);
CREATE INDEX idx_deposito_activo ON Deposito(activo);
\`\`\`

#### 1.7 Camion
\`\`\`sql
CREATE TABLE Camion (
    camion_id BIGINT PRIMARY KEY AUTO_INCREMENT,
    dominio VARCHAR(20) UNIQUE NOT NULL,
    marca VARCHAR(50),
    modelo VARCHAR(50),
    anio INTEGER,
    
    -- Capacidades
    capacidad_peso_kg DECIMAL(10,2) NOT NULL,
    capacidad_volumen_m3 DECIMAL(10,2) NOT NULL,
    
    -- Costos
    consumo_combustible_km_litro DECIMAL(5,2) NOT NULL,
    costo_base_por_km DECIMAL(10,2) NOT NULL,
    
    -- Estado
    disponible BOOLEAN DEFAULT TRUE,
    activo BOOLEAN DEFAULT TRUE,
    
    -- Transportista
    transportista_id BIGINT NOT NULL,
    
    observaciones TEXT,
    fecha_ingreso TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    FOREIGN KEY (transportista_id) REFERENCES Transportista(transportista_id),
    
    CONSTRAINT chk_capacidades CHECK (
        capacidad_peso_kg > 0 AND 
        capacidad_volumen_m3 > 0
    ),
    CONSTRAINT chk_consumo CHECK (consumo_combustible_km_litro > 0)
);

CREATE INDEX idx_camion_dominio ON Camion(dominio);
CREATE INDEX idx_camion_disponible ON Camion(disponible, activo);
CREATE INDEX idx_camion_transportista ON Camion(transportista_id);
\`\`\`

#### 1.8 Transportista
\`\`\`sql
CREATE TABLE Transportista (
    transportista_id BIGINT PRIMARY KEY AUTO_INCREMENT,
    nombre VARCHAR(100) NOT NULL,
    apellido VARCHAR(100) NOT NULL,
    documento_tipo VARCHAR(10) NOT NULL,
    documento_numero VARCHAR(20) NOT NULL UNIQUE,
    telefono VARCHAR(20) NOT NULL,
    email VARCHAR(150),
    licencia_conducir VARCHAR(50) NOT NULL,
    fecha_vencimiento_licencia DATE,
    activo BOOLEAN DEFAULT TRUE,
    fecha_ingreso TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_transportista_documento ON Transportista(documento_numero);
CREATE INDEX idx_transportista_activo ON Transportista(activo);
\`\`\`

#### 1.9 Tarifa
\`\`\`sql
CREATE TABLE Tarifa (
    tarifa_id BIGINT PRIMARY KEY AUTO_INCREMENT,
    concepto VARCHAR(50) NOT NULL,
    descripcion VARCHAR(255),
    valor DECIMAL(10,2) NOT NULL,
    unidad VARCHAR(20) NOT NULL, -- KM, LITRO, DIA, FIJO
    
    -- Rangos (opcionales, para tarifas por peso/volumen)
    rango_peso_min_kg DECIMAL(10,2),
    rango_peso_max_kg DECIMAL(10,2),
    rango_volumen_min_m3 DECIMAL(10,2),
    rango_volumen_max_m3 DECIMAL(10,2),
    
    -- Vigencia
    fecha_vigencia_desde DATE NOT NULL,
    fecha_vigencia_hasta DATE,
    
    activo BOOLEAN DEFAULT TRUE
);

CREATE INDEX idx_tarifa_concepto ON Tarifa(concepto);
CREATE INDEX idx_tarifa_vigencia ON Tarifa(fecha_vigencia_desde, fecha_vigencia_hasta);
CREATE INDEX idx_tarifa_activo ON Tarifa(activo);
\`\`\`

#### 1.10 HistorialEstadoContenedor
\`\`\`sql
CREATE TABLE HistorialEstadoContenedor (
    historial_id BIGINT PRIMARY KEY AUTO_INCREMENT,
    contenedor_id BIGINT NOT NULL,
    estado_anterior VARCHAR(30),
    estado_nuevo VARCHAR(30) NOT NULL,
    ubicacion_id BIGINT, -- Puede ser depósito o ubicación específica
    tramo_id BIGINT,
    fecha_hora TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    descripcion TEXT,
    usuario_registro VARCHAR(100),
    
    FOREIGN KEY (contenedor_id) REFERENCES Contenedor(contenedor_id),
    FOREIGN KEY (tramo_id) REFERENCES Tramo(tramo_id)
);

CREATE INDEX idx_historial_contenedor ON HistorialEstadoContenedor(contenedor_id);
CREATE INDEX idx_historial_fecha ON HistorialEstadoContenedor(fecha_hora);
CREATE INDEX idx_historial_tramo ON HistorialEstadoContenedor(tramo_id);
\`\`\`

---

## 2. Análisis de tu Modelo vs Modelo Propuesto

### ✅ Fortalezas de tu modelo:

1. **Entidad Ubicacion separada**: Buena normalización, evita duplicación de datos de ubicaciones
2. **HistorialEstadoContenedor**: Excelente para tracking y auditoría
3. **Transportista separado de Camion**: Correcta separación de responsabilidades
4. **Campos de auditoría**: Incluye fecha_creacion, fecha_actualizacion en varias tablas
5. **Estructura general sólida**: Todas las entidades principales están presentes

### ⚠️ Áreas de mejora sugeridas:

#### 1. **Entidad Ubicacion - Simplificar**
Tu modelo tiene muchos campos en Ubicacion:
- `tipo`, `nombre`, `direccion`, `latitud`, `longitud`, `provincia`, `codigo_postal`, `activo`

**Problema**: Mezcla conceptos de diferentes tipos de ubicaciones (depósitos, orígenes, destinos)

**Sugerencia**: 
- Mantener Ubicacion simple: solo `latitud`, `longitud`, `direccion`
- O eliminar Ubicacion y poner coordenadas directamente en las entidades que las usan
- Los campos como `provincia`, `codigo_postal` son útiles pero podrían estar en Deposito específicamente

#### 2. **Solicitud - Falta numero_solicitud**
Tu modelo tiene `solicitud_id` pero falta `numero_solicitud` (VARCHAR)

**Problema**: El enunciado menciona "número de solicitud" como identificador de negocio

**Sugerencia**: Agregar `numero_solicitud VARCHAR(20) UNIQUE NOT NULL`

#### 3. **Contenedor - Estado actual**
Tienes HistorialEstadoContenedor pero no veo un campo `estado` en Contenedor

**Problema**: Para consultas rápidas del estado actual, necesitas el campo en la tabla principal

**Sugerencia**: Agregar `estado VARCHAR(30) NOT NULL` en Contenedor

#### 4. **Tramo - Verificar campos completos**
Necesitas verificar que Tramo tenga:
- `numero_orden` (para ordenar tramos en la ruta)
- `tipo_tramo` (ORIGEN_DEPOSITO, DEPOSITO_DEPOSITO, etc.)
- Fechas estimadas y reales separadas

#### 5. **Camion - Campo disponible**
Debe tener un campo para saber si está libre u ocupado

**Sugerencia**: Agregar `disponible BOOLEAN DEFAULT TRUE`

#### 6. **Índices**
Asegúrate de tener índices en:
- Claves foráneas
- Campos usados en WHERE frecuentemente (estado, fecha, etc.)
- Campos de búsqueda (email, documento, dominio, etc.)

---

## 3. Modelo Recomendado Final

### Opción A: Con entidad Ubicacion (más normalizado)

\`\`\`
Cliente (1) ----< (N) Solicitud (1) ---- (1) Contenedor
                        |
                        | (1)
                        |
                      Ruta (1) ----< (N) Tramo
                                          |
                                          | (N)
                                          |
                                        Camion (N) ---- (1) Transportista

Deposito (N) ----< (N) Tramo (usa origen_id o destino_id)

Contenedor (1) ----< (N) HistorialEstadoContenedor

Tarifa (tabla independiente para configuración)
\`\`\`

### Opción B: Sin entidad Ubicacion (más simple, recomendada)

\`\`\`
Cliente (1) ----< (N) Solicitud (1) ---- (1) Contenedor
                        |
                        | (1)
                        |
                      Ruta (1) ----< (N) Tramo
                                          |
                                          | (N)
                                          |
                                        Camion (N) ---- (1) Transportista

Deposito (N) ----< (N) Tramo (referencia opcional)

Contenedor (1) ----< (N) HistorialEstadoContenedor

Tarifa (tabla independiente)
\`\`\`

**Recomendación**: Opción B es más simple y suficiente para el proyecto.

---

## 4. Decisiones de Diseño Clave

### 4.1 ¿Una base de datos o múltiples?

**Tu modelo parece usar una sola base de datos**

**Ventajas**:
- ✅ Más simple de implementar
- ✅ Transacciones ACID entre todas las tablas
- ✅ Joins directos entre tablas
- ✅ Más fácil de mantener para un proyecto académico

**Desventajas**:
- ❌ Menos escalable
- ❌ Acoplamiento entre microservicios

**Alternativa - Base de datos por microservicio**:
\`\`\`
clientes-service     → db_clientes (Cliente)
depositos-service    → db_depositos (Deposito)
camiones-service     → db_camiones (Camion, Transportista)
tarifas-service      → db_tarifas (Tarifa)
solicitudes-service  → db_solicitudes (Solicitud, Contenedor, HistorialEstadoContenedor)
rutas-service        → db_rutas (Ruta, Tramo)
\`\`\`

**Recomendación para el TPI**: 
- Usar **base de datos por microservicio** para demostrar comprensión de arquitectura de microservicios
- Es lo que esperan los docentes según el enunciado
- Demuestra mejor separación de responsabilidades

### 4.2 ¿Cómo manejar las relaciones entre microservicios?

Si usas bases de datos separadas, no puedes usar FOREIGN KEY entre servicios.

**Solución**:
- Guardar solo el ID de la entidad externa (ej: `cliente_id` en Solicitud)
- Validar existencia mediante llamada REST al otro microservicio
- Mantener consistencia eventual

**Ejemplo**:
\`\`\`java
// En SolicitudService
public Solicitud crearSolicitud(SolicitudRequestDTO request) {
    // Validar que el cliente existe llamando a clientes-service
    ClienteResponseDTO cliente = clienteServiceClient.obtenerCliente(request.getClienteId());
    
    if (cliente == null) {
        throw new ClienteNotFoundException("Cliente no encontrado");
    }
    
    // Crear la solicitud con el cliente_id
    Solicitud solicitud = new Solicitud();
    solicitud.setClienteId(request.getClienteId());
    // ... resto de la lógica
}
\`\`\`

---

## 5. Recomendaciones Finales

### Para tu modelo actual:

1. **Simplificar Ubicacion** o eliminarla y poner coordenadas directamente
2. **Agregar `numero_solicitud`** en Solicitud
3. **Agregar `estado`** en Contenedor
4. **Agregar `disponible`** en Camion
5. **Verificar que Tramo** tenga todos los campos necesarios
6. **Agregar índices** en campos clave
7. **Decidir**: ¿Una BD o múltiples? (Recomiendo múltiples para el TPI)

### Para la defensa:

Prepara respuestas para:
- ¿Por qué elegiste una BD vs múltiples?
- ¿Cómo manejas las relaciones entre microservicios?
- ¿Por qué separaste Transportista de Camion?
- ¿Cómo funciona el HistorialEstadoContenedor?
- ¿Qué índices agregaste y por qué?
- ¿Cómo garantizas la integridad referencial sin FOREIGN KEY entre servicios?

---

## 6. Scripts SQL de Creación

Ver archivo: `scripts/create-database-schema.sql` (próximo a crear)

---

## 7. Diagrama Visual

Tu diagrama en DrawIO está bien estructurado visualmente. Sugerencias:
- Resaltar las claves primarias (PK) en negrita
- Usar colores diferentes para entidades principales vs secundarias
- Agregar cardinalidad en las relaciones (1:N, N:M, etc.)
- Indicar qué campos son UNIQUE, NOT NULL

---

## Conclusión

Tu modelo está **muy bien encaminado** y tiene una estructura sólida. Con los ajustes sugeridos, tendrás un modelo robusto y defendible para el TPI.

**Próximos pasos**:
1. Ajustar el modelo según las sugerencias
2. Decidir: ¿Una BD o múltiples?
3. Crear scripts SQL de creación
4. Documentar decisiones de diseño
5. Preparar la defensa con argumentos sólidos
