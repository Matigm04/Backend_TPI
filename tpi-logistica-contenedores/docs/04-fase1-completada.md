# Fase 1 - Servicios Base Completados

## Estado: ✅ COMPLETADA

La Fase 1 del proyecto ha sido completada exitosamente. Todos los servicios base están implementados y listos para ser utilizados por los servicios de orquestación de la Fase 2.

## Servicios Implementados

### 1. clientes-service (Puerto 8081)
**Responsabilidad:** Gestión de clientes del sistema

**Endpoints principales:**
- `POST /api/clientes` - Crear cliente
- `GET /api/clientes` - Listar todos
- `GET /api/clientes/{id}` - Obtener por ID
- `GET /api/clientes/email/{email}` - Buscar por email
- `PUT /api/clientes/{id}` - Actualizar
- `DELETE /api/clientes/{id}` - Eliminar (soft delete)

**Características:**
- Validación de email único
- Validación de formato de teléfono
- Soft delete (desactivación lógica)
- Auditoría con timestamps

---

### 2. depositos-service (Puerto 8082)
**Responsabilidad:** Gestión de depósitos intermedios

**Endpoints principales:**
- `POST /api/depositos` - Crear depósito
- `GET /api/depositos` - Listar todos
- `GET /api/depositos/{id}` - Obtener por ID
- `GET /api/depositos/cercanos` - Buscar cercanos por coordenadas
- `PUT /api/depositos/{id}` - Actualizar
- `DELETE /api/depositos/{id}` - Eliminar (soft delete)

**Características:**
- Geolocalización con latitud/longitud
- Búsqueda de depósitos cercanos usando fórmula de Haversine
- Control de capacidad máxima
- Costo diario de estadía configurable

---

### 3. camiones-service (Puerto 8083)
**Responsabilidad:** Gestión de camiones y transportistas

**Endpoints principales:**
- `POST /api/camiones` - Crear camión
- `GET /api/camiones` - Listar todos
- `GET /api/camiones/{id}` - Obtener por ID
- `GET /api/camiones/disponibles` - Listar disponibles
- `GET /api/camiones/disponibles/capacidad` - Buscar por capacidad
- `PUT /api/camiones/{id}` - Actualizar
- `DELETE /api/camiones/{id}` - Eliminar (soft delete)
- `PATCH /api/camiones/{id}/asignar` - Asignar camión
- `PATCH /api/camiones/{id}/liberar` - Liberar camión

**Características:**
- Validación de dominio único
- Control de disponibilidad
- Búsqueda por capacidad de peso y volumen
- Consumo de combustible y costo por km
- Asignación/liberación de camiones

---

### 4. tarifas-service (Puerto 8084)
**Responsabilidad:** Gestión de tarifas y configuraciones

**Endpoints principales:**
- `POST /api/tarifas` - Crear tarifa
- `GET /api/tarifas` - Listar todas
- `GET /api/tarifas/{id}` - Obtener por ID
- `GET /api/tarifas/tipo/{tipo}` - Listar por tipo
- `GET /api/tarifas/vigente/{tipo}` - Obtener vigente por tipo
- `GET /api/tarifas/vigentes` - Listar todas vigentes
- `PUT /api/tarifas/{id}` - Actualizar
- `DELETE /api/tarifas/{id}` - Eliminar (soft delete)

**Tipos de tarifa:**
- `COSTO_KM_BASE` - Costo por kilómetro base
- `COSTO_COMBUSTIBLE_LITRO` - Precio del litro de combustible
- `CARGO_GESTION_FIJO` - Cargo fijo de gestión por tramo
- `ESTADIA_DEPOSITO` - Costo de estadía por día en depósito

**Características:**
- Sistema de vigencia con fechas desde/hasta
- Consulta de tarifas vigentes
- Múltiples tipos de tarifa
- Unidades configurables

---

## Arquitectura Común

Todos los servicios de la Fase 1 comparten:

### Estructura de capas
\`\`\`
Controller → Service → Repository → Model
\`\`\`

### Tecnologías
- Spring Boot 3.2.0
- Java 17
- JPA/Hibernate
- PostgreSQL (producción)
- H2 (desarrollo)
- Lombok
- Swagger/OpenAPI
- Maven

### Características comunes
- ✅ Validación de datos con Bean Validation
- ✅ Manejo global de excepciones
- ✅ DTOs para request/response
- ✅ Soft delete (desactivación lógica)
- ✅ Auditoría con timestamps
- ✅ Documentación Swagger
- ✅ Logs estructurados
- ✅ Dockerización
- ✅ Perfiles dev/prod

---

## Cómo ejecutar

### Modo desarrollo (H2)
\`\`\`bash
cd <servicio>-service
mvn spring-boot:run
\`\`\`

### Modo producción (PostgreSQL)
\`\`\`bash
cd <servicio>-service
mvn spring-boot:run -Dspring-boot.run.profiles=prod
\`\`\`

### Con Docker
\`\`\`bash
cd <servicio>-service
docker build -t <servicio>-service .
docker run -p <puerto>:8080 <servicio>-service
\`\`\`

---

## Documentación Swagger

Cada servicio expone su documentación en:
- **Swagger UI:** `http://localhost:<puerto>/swagger-ui.html`
- **OpenAPI JSON:** `http://localhost:<puerto>/api-docs`

---

## Próximos pasos - Fase 2

Con la Fase 1 completada, estamos listos para implementar los servicios de orquestación:

1. **solicitudes-service** - Gestión de solicitudes y contenedores
2. **rutas-service** - Cálculo de rutas y asignación de tramos

Estos servicios consumirán los endpoints de los servicios base para implementar la lógica de negocio compleja del sistema de logística.
