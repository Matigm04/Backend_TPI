# Arquitectura de Microservicios - Sistema de Logística de Contenedores

## Visión General

La arquitectura propuesta sigue el patrón de microservicios con las siguientes características:

- **Desacoplamiento:** Cada microservicio es independiente y autónomo
- **Base de datos por servicio:** Cada microservicio gestiona su propia base de datos
- **Comunicación:** REST API entre servicios y API Gateway como punto de entrada único
- **Seguridad:** Keycloak como Identity Provider con JWT
- **Contenedorización:** Cada servicio en su propio contenedor Docker

---

## Diagrama de Arquitectura

\`\`\`
┌─────────────────────────────────────────────────────────────────┐
│                         CLIENTES EXTERNOS                        │
│                    (Postman, Apps, Browsers)                     │
└────────────────────────────────┬────────────────────────────────┘
                                 │
                                 ▼
                    ┌────────────────────────┐
                    │     API GATEWAY        │
                    │   (Spring Cloud GW)    │
                    │  - Routing             │
                    │  - Load Balancing      │
                    │  - JWT Validation      │
                    └────────────┬───────────┘
                                 │
                 ┌───────────────┼───────────────┐
                 │               │               │
                 ▼               ▼               ▼
    ┌──────────────────┐ ┌─────────────┐ ┌──────────────────┐
    │   SOLICITUDES    │ │   FLOTA     │ │   RUTAS          │
    │   SERVICE        │ │   SERVICE   │ │   SERVICE        │
    └────────┬─────────┘ └──────┬──────┘ └────────┬─────────┘
             │                  │                  │
             ▼                  ▼                  ▼
    ┌──────────────────┐ ┌─────────────┐ ┌──────────────────┐
    │   PostgreSQL     │ │ PostgreSQL  │ │   PostgreSQL     │
    │   (Solicitudes)  │ │  (Flota)    │ │   (Rutas)        │
    └──────────────────┘ └─────────────┘ └──────────────────┘

                 ┌───────────────┼───────────────┐
                 │               │               │
                 ▼               ▼               ▼
    ┌──────────────────┐ ┌─────────────┐ ┌──────────────────┐
    │   CLIENTES       │ │  TARIFAS    │ │   SEGUIMIENTO    │
    │   SERVICE        │ │  SERVICE    │ │   SERVICE        │
    └────────┬─────────┘ └──────┬──────┘ └────────┬─────────┘
             │                  │                  │
             ▼                  ▼                  ▼
    ┌──────────────────┐ ┌─────────────┐ ┌──────────────────┐
    │   PostgreSQL     │ │ PostgreSQL  │ │   PostgreSQL     │
    │   (Clientes)     │ │ (Tarifas)   │ │  (Seguimiento)   │
    └──────────────────┘ └─────────────┘ └──────────────────┘

                         ┌─────────────┐
                         │  KEYCLOAK   │
                         │   (Auth)    │
                         └─────────────┘

                         ┌─────────────┐
                         │ Google Maps │
                         │     API     │
                         └─────────────┘
\`\`\`

---

## Microservicios Propuestos

### 1. API Gateway
**Puerto:** 8080  
**Tecnología:** Spring Cloud Gateway  
**Responsabilidad:** Punto de entrada único al sistema

**Funciones:**
- Enrutamiento de peticiones a microservicios
- Validación de tokens JWT
- Rate limiting y throttling
- CORS configuration
- Logging centralizado de requests

**No tiene base de datos propia**

---

### 2. Servicio de Clientes
**Puerto:** 8081  
**Base de datos:** PostgreSQL (clientes_db)  
**Responsabilidad:** Gestión de clientes

**Entidades:**
- Cliente

**Funcionalidades:**
- CRUD de clientes
- Validación de datos de contacto
- Búsqueda y filtrado de clientes
- Historial de solicitudes por cliente

**Endpoints principales:**
- `POST /api/clientes` - Registrar cliente
- `GET /api/clientes/{id}` - Obtener cliente
- `PUT /api/clientes/{id}` - Actualizar cliente
- `GET /api/clientes` - Listar clientes

---

### 3. Servicio de Solicitudes
**Puerto:** 8082  
**Base de datos:** PostgreSQL (solicitudes_db)  
**Responsabilidad:** Gestión de solicitudes de traslado y contenedores

**Entidades:**
- Solicitud
- Contenedor
- Ubicacion (origen/destino)

**Funcionalidades:**
- Crear solicitud de traslado (incluye creación de contenedor)
- Gestionar estados de solicitudes
- Consultar solicitudes por cliente
- Filtrar solicitudes por estado
- Calcular costos y tiempos estimados
- Registrar costos y tiempos reales

**Comunicación con otros servicios:**
- Valida existencia de cliente (Servicio de Clientes)
- Solicita cálculo de ruta (Servicio de Rutas)
- Consulta tarifas (Servicio de Tarifas)

**Endpoints principales:**
- `POST /api/solicitudes` - Crear solicitud
- `GET /api/solicitudes/{id}` - Obtener solicitud
- `PUT /api/solicitudes/{id}/estado` - Cambiar estado
- `GET /api/solicitudes/cliente/{clienteId}` - Solicitudes por cliente
- `GET /api/solicitudes` - Listar con filtros

---

### 4. Servicio de Rutas
**Puerto:** 8083  
**Base de datos:** PostgreSQL (rutas_db)  
**Responsabilidad:** Planificación y gestión de rutas y tramos

**Entidades:**
- Ruta
- Tramo
- Deposito
- Ubicacion (depósitos)

**Funcionalidades:**
- Calcular rutas óptimas (con o sin depósitos)
- Gestionar depósitos
- Crear y gestionar tramos
- Integración con Google Maps API para calcular distancias
- Consultar rutas tentativas
- Asignar ruta definitiva a solicitud

**Integración externa:**
- Google Maps Directions API

**Endpoints principales:**
- `POST /api/rutas/calcular` - Calcular ruta tentativa
- `POST /api/rutas` - Crear ruta definitiva
- `GET /api/rutas/{id}` - Obtener ruta
- `GET /api/rutas/solicitud/{solicitudId}` - Ruta por solicitud
- `POST /api/depositos` - Crear depósito
- `GET /api/depositos` - Listar depósitos
- `GET /api/depositos/{id}/contenedores` - Contenedores en depósito

---

### 5. Servicio de Flota
**Puerto:** 8084  
**Base de datos:** PostgreSQL (flota_db)  
**Responsabilidad:** Gestión de camiones, transportistas y asignaciones

**Entidades:**
- Camion
- Transportista

**Funcionalidades:**
- CRUD de camiones
- CRUD de transportistas
- Consultar disponibilidad de camiones
- Validar capacidad de carga (peso y volumen)
- Asignar camión a tramo
- Registrar inicio y fin de tramo
- Calcular consumo de combustible

**Endpoints principales:**
- `POST /api/camiones` - Registrar camión
- `GET /api/camiones` - Listar camiones
- `GET /api/camiones/disponibles` - Camiones disponibles
- `GET /api/camiones/{id}` - Obtener camión
- `PUT /api/camiones/{id}` - Actualizar camión
- `POST /api/camiones/{id}/asignar-tramo` - Asignar a tramo
- `POST /api/transportistas` - Registrar transportista
- `GET /api/transportistas` - Listar transportistas
- `GET /api/transportistas/{id}/tramos` - Tramos asignados

---

### 6. Servicio de Tarifas
**Puerto:** 8085  
**Base de datos:** PostgreSQL (tarifas_db)  
**Responsabilidad:** Gestión de tarifas y cálculo de costos

**Entidades:**
- Tarifa

**Funcionalidades:**
- CRUD de tarifas
- Calcular costo estimado de traslado
- Calcular costo real de traslado
- Gestionar tarifas por rango de peso/volumen
- Configurar precio de combustible
- Configurar costos de estadía en depósitos

**Endpoints principales:**
- `POST /api/tarifas` - Crear tarifa
- `GET /api/tarifas` - Listar tarifas
- `PUT /api/tarifas/{id}` - Actualizar tarifa
- `POST /api/tarifas/calcular-estimado` - Calcular costo estimado
- `POST /api/tarifas/calcular-real` - Calcular costo real
- `GET /api/tarifas/vigentes` - Tarifas vigentes

---

### 7. Servicio de Seguimiento
**Puerto:** 8086  
**Base de datos:** PostgreSQL (seguimiento_db)  
**Responsabilidad:** Seguimiento y auditoría de estados

**Entidades:**
- HistorialEstadoContenedor

**Funcionalidades:**
- Registrar cambios de estado de contenedores
- Consultar historial de seguimiento
- Obtener ubicación actual de contenedor
- Timeline de eventos de una solicitud
- Notificaciones de cambios de estado (futuro)

**Endpoints principales:**
- `POST /api/seguimiento/registrar` - Registrar evento
- `GET /api/seguimiento/contenedor/{contenedorId}` - Historial de contenedor
- `GET /api/seguimiento/solicitud/{solicitudId}` - Timeline de solicitud
- `GET /api/seguimiento/contenedor/{contenedorId}/ubicacion-actual` - Ubicación actual

---

## Comunicación entre Microservicios

### Patrón de Comunicación: REST API Síncrona

**Ejemplo de flujo: Crear Solicitud**

1. Cliente → API Gateway → Servicio de Solicitudes
2. Servicio de Solicitudes → Servicio de Clientes (validar cliente)
3. Servicio de Solicitudes → Servicio de Rutas (calcular ruta tentativa)
4. Servicio de Solicitudes → Servicio de Tarifas (calcular costo estimado)
5. Servicio de Solicitudes → Servicio de Seguimiento (registrar evento)
6. Respuesta al cliente con solicitud creada

**Ejemplo de flujo: Asignar Camión a Tramo**

1. Operador → API Gateway → Servicio de Flota
2. Servicio de Flota → Servicio de Rutas (obtener datos del tramo)
3. Servicio de Flota → Validar capacidad del camión
4. Servicio de Flota → Actualizar disponibilidad del camión
5. Servicio de Flota → Servicio de Rutas (actualizar tramo con camión asignado)
6. Servicio de Flota → Servicio de Seguimiento (registrar asignación)
7. Respuesta al operador

---

## Seguridad con Keycloak

### Configuración de Roles

**Roles en Keycloak:**
- `ROLE_CLIENTE`
- `ROLE_OPERADOR`
- `ROLE_TRANSPORTISTA`
- `ROLE_ADMIN`

### Flujo de Autenticación

1. Usuario se autentica en Keycloak
2. Keycloak devuelve JWT con roles
3. Cliente envía JWT en header `Authorization: Bearer <token>`
4. API Gateway valida el token con Keycloak
5. API Gateway extrae roles y los pasa al microservicio
6. Microservicio valida autorización con `@PreAuthorize`

### Configuración en cada Microservicio

\`\`\`java
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {
    // Configuración de Spring Security con Keycloak
    // Validación de JWT
    // Extracción de roles
}
\`\`\`

---

## Despliegue con Docker

### Estructura de docker-compose.yml

\`\`\`yaml
version: '3.8'

services:
  # Bases de datos
  postgres-clientes:
    image: postgres:15
    environment:
      POSTGRES_DB: clientes_db
      
  postgres-solicitudes:
    image: postgres:15
    environment:
      POSTGRES_DB: solicitudes_db
      
  # ... más bases de datos
  
  # Keycloak
  keycloak:
    image: quay.io/keycloak/keycloak:latest
    ports:
      - "8180:8080"
      
  # Microservicios
  api-gateway:
    build: ./api-gateway
    ports:
      - "8080:8080"
      
  servicio-clientes:
    build: ./servicio-clientes
    depends_on:
      - postgres-clientes
      
  # ... más servicios
\`\`\`

---

## Ventajas de esta Arquitectura

1. **Escalabilidad independiente:** Cada servicio puede escalar según demanda
2. **Despliegue independiente:** Cambios en un servicio no afectan a otros
3. **Tecnologías específicas:** Cada servicio puede usar la mejor tecnología para su caso
4. **Resiliencia:** Fallo en un servicio no tumba todo el sistema
5. **Equipos independientes:** Diferentes equipos pueden trabajar en diferentes servicios
6. **Mantenibilidad:** Código más pequeño y enfocado por servicio

---

## Consideraciones de Implementación

1. **Manejo de transacciones distribuidas:** Usar patrón Saga si es necesario
2. **Consistencia eventual:** Aceptar que los datos pueden no estar sincronizados inmediatamente
3. **Circuit Breaker:** Implementar con Resilience4j para manejar fallos
4. **Logging centralizado:** Usar ELK stack o similar (futuro)
5. **Monitoreo:** Implementar health checks y métricas (Spring Actuator)
6. **API Documentation:** Swagger/OpenAPI en cada microservicio
