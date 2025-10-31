# Diagrama C4 - Nivel 2: Contenedores
## Sistema de Logística de Transporte de Contenedores

## Introducción al Modelo C4

El modelo C4 (Context, Containers, Components, Code) es un enfoque para visualizar la arquitectura de software. Este documento presenta el **Nivel 2: Contenedores**, que muestra los contenedores de alto nivel (aplicaciones, bases de datos, servicios) que componen el sistema y cómo se comunican entre sí.

---

## Diagrama C4 - Nivel 2: Contenedores

\`\`\`
┌─────────────────────────────────────────────────────────────────────────────────────────┐
│                                                                                          │
│                          Sistema de Logística de Contenedores                           │
│                                                                                          │
│  ┌────────────────────────────────────────────────────────────────────────────────┐    │
│  │                              USUARIOS EXTERNOS                                  │    │
│  │                                                                                 │    │
│  │  ┌──────────────┐    ┌──────────────┐    ┌──────────────┐                    │    │
│  │  │   Cliente    │    │  Operador/   │    │Transportista │                    │    │
│  │  │              │    │Administrador │    │              │                    │    │
│  │  │ [Persona]    │    │  [Persona]   │    │  [Persona]   │                    │    │
│  │  └──────┬───────┘    └──────┬───────┘    └──────┬───────┘                    │    │
│  │         │                   │                    │                             │    │
│  │         │ Solicita traslados│ Gestiona sistema   │ Registra viajes            │    │
│  │         │ Consulta estado   │ Asigna recursos    │ Inicia/finaliza tramos     │    │
│  └─────────┼───────────────────┼────────────────────┼─────────────────────────────┘    │
│            │                   │                    │                                   │
│            └───────────────────┴────────────────────┘                                   │
│                                │                                                        │
│                                │ HTTPS/JSON                                             │
│                                │ JWT Bearer Token                                       │
│                                ▼                                                        │
│  ┌─────────────────────────────────────────────────────────────────────────────────┐  │
│  │                         API Gateway [Contenedor]                                 │  │
│  │                         Spring Cloud Gateway                                     │  │
│  │                         Puerto: 8080                                             │  │
│  │                                                                                  │  │
│  │  • Enrutamiento centralizado a microservicios                                   │  │
│  │  • Validación de tokens JWT con Keycloak                                        │  │
│  │  • Rate limiting y circuit breakers                                             │  │
│  │  • Configuración de CORS                                                        │  │
│  │  • Logging global de peticiones                                                 │  │
│  └────────┬────────────────────────────────────────────────────────────────────────┘  │
│           │                                                                            │
│           │ Valida JWT                                                                 │
│           ├──────────────────────────────────────────────────────────────────┐        │
│           │                                                                   │        │
│           ▼                                                                   ▼        │
│  ┌─────────────────────────────────────────────────────────┐   ┌──────────────────┐  │
│  │         Keycloak [Contenedor Externo]                    │   │  Google Maps API │  │
│  │         Identity Provider                                │   │  [Sistema Ext.]  │  │
│  │         Puerto: 8180                                     │   │                  │  │
│  │                                                          │   │  • Distance      │  │
│  │  • Gestión de usuarios y roles                          │   │    Matrix API    │  │
│  │  • Emisión de tokens JWT                                │   │  • Cálculo de    │  │
│  │  • Autenticación OAuth2/OpenID Connect                  │   │    distancias    │  │
│  │  • Roles: CLIENTE, OPERADOR, TRANSPORTISTA, ADMIN       │   │    reales        │  │
│  └────────┬────────────────────────────────────────────────┘   └──────────▲───────┘  │
│           │                                                                 │          │
│           ▼                                                                 │          │
│  ┌─────────────────┐                                                       │          │
│  │  BD Keycloak    │                                                       │          │
│  │  [PostgreSQL]   │                                                       │          │
│  │  Puerto: 5433   │                                                       │          │
│  └─────────────────┘                                                       │          │
│                                                                             │          │
│           ┌─────────────────────────────────────────────────────────────────┘          │
│           │ Enruta peticiones HTTP/REST                                                │
│           │                                                                             │
│           ├──────────┬──────────┬──────────┬──────────┬──────────┐                    │
│           ▼          ▼          ▼          ▼          ▼          ▼                    │
│  ┌─────────────────────────────────────────────────────────────────────────────────┐  │
│  │                         CAPA DE MICROSERVICIOS                                   │  │
│  │                                                                                  │  │
│  │  ┌──────────────────┐  ┌──────────────────┐  ┌──────────────────┐             │  │
│  │  │ clientes-service │  │depositos-service │  │ camiones-service │             │  │
│  │  │  [Contenedor]    │  │  [Contenedor]    │  │  [Contenedor]    │             │  │
│  │  │  Spring Boot     │  │  Spring Boot     │  │  Spring Boot     │             │  │
│  │  │  Puerto: 8081    │  │  Puerto: 8082    │  │  Puerto: 8083    │             │  │
│  │  │                  │  │                  │  │                  │             │  │
│  │  │ • CRUD Clientes  │  │ • CRUD Depósitos │  │ • CRUD Camiones  │             │  │
│  │  │ • Validación     │  │ • Búsqueda por   │  │ • Gestión de     │             │  │
│  │  │   de datos       │  │   coordenadas    │  │   disponibilidad │             │  │
│  │  │ • Gestión de     │  │ • Capacidad de   │  │ • Asignación a   │             │  │
│  │  │   contacto       │  │   almacenamiento │  │   tramos         │             │  │
│  │  │                  │  │ • Costos estadía │  │ • Transportistas │             │  │
│  │  └────────┬─────────┘  └────────┬─────────┘  └────────┬─────────┘             │  │
│  │           │                     │                      │                        │  │
│  │           │ JPA/Hibernate       │ JPA/Hibernate        │ JPA/Hibernate         │  │
│  │           ▼                     ▼                      ▼                        │  │
│  │  ┌─────────────────┐   ┌─────────────────┐   ┌─────────────────┐             │  │
│  │  │  BD Clientes    │   │  BD Depositos   │   │  BD Camiones    │             │  │
│  │  │  [PostgreSQL]   │   │  [PostgreSQL]   │   │  [PostgreSQL]   │             │  │
│  │  │  Puerto: 5434   │   │  Puerto: 5435   │   │  Puerto: 5436   │             │  │
│  │  │                 │   │                 │   │                 │             │  │
│  │  │ Tablas:         │   │ Tablas:         │   │ Tablas:         │             │  │
│  │  │ • cliente       │   │ • deposito      │   │ • camion        │             │  │
│  │  └─────────────────┘   │ • ubicacion     │   │ • transportista │             │  │
│  │                        └─────────────────┘   └─────────────────┘             │  │
│  │                                                                                │  │
│  │  ┌──────────────────┐  ┌──────────────────────────────────────┐              │  │
│  │  │ tarifas-service  │  │    solicitudes-service               │              │  │
│  │  │  [Contenedor]    │  │      [Contenedor]                    │              │  │
│  │  │  Spring Boot     │  │      Spring Boot                     │              │  │
│  │  │  Puerto: 8084    │  │      Puerto: 8085                    │              │  │
│  │  │                  │  │                                       │              │  │
│  │  │ • CRUD Tarifas   │  │ • CRUD Solicitudes                   │              │  │
│  │  │ • Gestión de     │  │ • Gestión de Contenedores            │              │  │
│  │  │   vigencia       │  │ • Cambios de estado                  │              │  │
│  │  │ • Cálculo de     │  │ • Historial de seguimiento           │◄─────┐       │  │
│  │  │   costos         │  │ • Validación con clientes-service    │      │       │  │
│  │  └────────┬─────────┘  └────────┬─────────────────────────────┘      │       │  │
│  │           │                     │                                     │       │  │
│  │           │ JPA/Hibernate       │ JPA/Hibernate                       │       │  │
│  │           ▼                     ▼                                     │       │  │
│  │  ┌─────────────────┐   ┌─────────────────┐                           │       │  │
│  │  │  BD Tarifas     │   │ BD Solicitudes  │                           │       │  │
│  │  │  [PostgreSQL]   │   │  [PostgreSQL]   │                           │       │  │
│  │  │  Puerto: 5437   │   │  Puerto: 5438   │                           │       │  │
│  │  │                 │   │                 │                           │       │  │
│  │  │ Tablas:         │   │ Tablas:         │                           │       │  │
│  │  │ • tarifa        │   │ • solicitud     │                           │       │  │
│  │  └─────────────────┘   │ • contenedor    │                           │       │  │
│  │                        │ • historial_    │                           │       │  │
│  │                        │   estado_       │                           │       │  │
│  │                        │   contenedor    │                           │       │  │
│  │                        └─────────────────┘                           │       │  │
│  │                                                                       │       │  │
│  │  ┌────────────────────────────────────────────────────────────────┐  │       │  │
│  │  │              rutas-service [Contenedor]                        │  │       │  │
│  │  │              Spring Boot                                       │  │       │  │
│  │  │              Puerto: 8086                                      │  │       │  │
│  │  │                                                                │  │       │  │
│  │  │  • Cálculo de rutas tentativas                                │  │       │  │
│  │  │  • Gestión de tramos                                          │  │       │  │
│  │  │  • Asignación de camiones a tramos                            │  │       │  │
│  │  │  • Inicio/Fin de viajes                                       │  │       │  │
│  │  │  • Integración con Google Maps API                            │──┼───────┘  │  │
│  │  │  • Comunicación con: solicitudes, depositos, camiones, tarifas│  │          │  │
│  │  └────────┬───────────────────────────────────────────────────────┘  │          │  │
│  │           │                                                           │          │  │
│  │           │ JPA/Hibernate                                             │          │  │
│  │           ▼                                                           │          │  │
│  │  ┌─────────────────┐                                                 │          │  │
│  │  │   BD Rutas      │                                                 │          │  │
│  │  │  [PostgreSQL]   │                                                 │          │  │
│  │  │  Puerto: 5439   │                                                 │          │  │
│  │  │                 │                                                 │          │  │
│  │  │ Tablas:         │                                                 │          │  │
│  │  │ • ruta          │                                                 │          │  │
│  │  │ • tramo         │                                                 │          │  │
│  │  └─────────────────┘                                                 │          │  │
│  │                                                                       │          │  │
│  └───────────────────────────────────────────────────────────────────────┘          │  │
│                                                                                      │  │
└──────────────────────────────────────────────────────────────────────────────────────┘  │
\`\`\`

---

## Leyenda de Notación C4

### Tipos de Elementos

- **[Persona]**: Usuario humano del sistema
- **[Contenedor]**: Aplicación o servicio ejecutable (microservicio Spring Boot)
- **[Base de Datos]**: Sistema de almacenamiento de datos (PostgreSQL)
- **[Sistema Externo]**: Sistema fuera de nuestro control (Google Maps, Keycloak)

### Relaciones

- **→**: Comunicación/Dependencia
- **HTTPS/JSON**: Protocolo de comunicación
- **JPA/Hibernate**: Tecnología de acceso a datos
- **REST**: Estilo de API

---

## Descripción de Contenedores

### 1. API Gateway
**Tecnología:** Spring Cloud Gateway  
**Puerto:** 8080  
**Responsabilidad:** Punto único de entrada al sistema

**Funcionalidades:**
- Enrutamiento de peticiones a microservicios
- Validación de tokens JWT
- Rate limiting
- Circuit breakers
- CORS
- Logging centralizado

**Comunicación:**
- **Entrada:** HTTPS desde clientes (navegadores, apps móviles)
- **Salida:** HTTP a microservicios internos
- **Validación:** Keycloak para verificar JWT

---

### 2. clientes-service
**Tecnología:** Spring Boot 3.x + JPA/Hibernate  
**Puerto:** 8081  
**Base de Datos:** PostgreSQL (puerto 5434)

**Responsabilidad:** Gestión de clientes del sistema

**Funcionalidades:**
- CRUD de clientes
- Validación de datos de contacto
- Búsqueda por documento, email
- Gestión de estado activo/inactivo

**Endpoints principales:**
- `POST /api/clientes` - Registrar cliente
- `GET /api/clientes/{id}` - Obtener cliente
- `PUT /api/clientes/{id}` - Actualizar cliente
- `GET /api/clientes` - Listar clientes

**Comunicación:**
- **Entrada:** API Gateway
- **Salida:** Ninguna (servicio base)

---

### 3. depositos-service
**Tecnología:** Spring Boot 3.x + JPA/Hibernate  
**Puerto:** 8082  
**Base de Datos:** PostgreSQL (puerto 5435)

**Responsabilidad:** Gestión de depósitos intermedios

**Funcionalidades:**
- CRUD de depósitos
- Búsqueda por coordenadas geográficas
- Gestión de capacidad
- Cálculo de costos de estadía
- Control de horarios de operación

**Endpoints principales:**
- `POST /api/depositos` - Crear depósito
- `GET /api/depositos/cercanos` - Buscar depósitos cercanos
- `GET /api/depositos/{id}/contenedores` - Contenedores en depósito

**Comunicación:**
- **Entrada:** API Gateway, rutas-service
- **Salida:** Ninguna (servicio base)

---

### 4. camiones-service
**Tecnología:** Spring Boot 3.x + JPA/Hibernate  
**Puerto:** 8083  
**Base de Datos:** PostgreSQL (puerto 5436)

**Responsabilidad:** Gestión de flota de camiones y transportistas

**Funcionalidades:**
- CRUD de camiones y transportistas
- Gestión de disponibilidad
- Búsqueda por capacidad (peso/volumen)
- Asignación/liberación de camiones
- Validación de capacidades

**Endpoints principales:**
- `POST /api/camiones` - Registrar camión
- `GET /api/camiones/disponibles` - Buscar camiones disponibles
- `POST /api/camiones/{id}/asignar` - Asignar a tramo
- `GET /api/transportistas/{id}/tramos` - Tramos asignados

**Comunicación:**
- **Entrada:** API Gateway, rutas-service
- **Salida:** Ninguna (servicio base)

---

### 5. tarifas-service
**Tecnología:** Spring Boot 3.x + JPA/Hibernate  
**Puerto:** 8084  
**Base de Datos:** PostgreSQL (puerto 5437)

**Responsabilidad:** Gestión de tarifas y cálculo de costos

**Funcionalidades:**
- CRUD de tarifas
- Gestión de vigencia temporal
- Cálculo de costos estimados
- Cálculo de costos reales
- Tarifas por rango de peso/volumen

**Endpoints principales:**
- `POST /api/tarifas` - Crear tarifa
- `GET /api/tarifas/vigentes` - Obtener tarifas vigentes
- `POST /api/tarifas/calcular-estimado` - Calcular costo estimado
- `POST /api/tarifas/calcular-real` - Calcular costo real

**Comunicación:**
- **Entrada:** API Gateway, rutas-service
- **Salida:** Ninguna (servicio base)

---

### 6. solicitudes-service
**Tecnología:** Spring Boot 3.x + JPA/Hibernate  
**Puerto:** 8085  
**Base de Datos:** PostgreSQL (puerto 5438)

**Responsabilidad:** Gestión de solicitudes de traslado y contenedores

**Funcionalidades:**
- CRUD de solicitudes
- Gestión de contenedores
- Cambios de estado
- Historial de seguimiento
- Validación con clientes-service

**Endpoints principales:**
- `POST /api/solicitudes` - Crear solicitud
- `GET /api/solicitudes/{id}` - Obtener solicitud
- `PUT /api/solicitudes/{id}/estado` - Cambiar estado
- `GET /api/seguimiento/contenedor/{id}` - Seguimiento

**Comunicación:**
- **Entrada:** API Gateway, rutas-service
- **Salida:** clientes-service (validar cliente)

---

### 7. rutas-service
**Tecnología:** Spring Boot 3.x + JPA/Hibernate  
**Puerto:** 8086  
**Base de Datos:** PostgreSQL (puerto 5439)

**Responsabilidad:** Cálculo de rutas y gestión de tramos

**Funcionalidades:**
- Cálculo de rutas tentativas
- Gestión de tramos
- Asignación de camiones a tramos
- Inicio/fin de viajes
- Integración con Google Maps API
- Orquestación de múltiples servicios

**Endpoints principales:**
- `POST /api/rutas/calcular` - Calcular rutas tentativas
- `POST /api/rutas` - Crear ruta definitiva
- `POST /api/tramos/{id}/asignar-camion` - Asignar camión
- `POST /api/tramos/{id}/iniciar` - Iniciar viaje
- `POST /api/tramos/{id}/finalizar` - Finalizar viaje

**Comunicación:**
- **Entrada:** API Gateway
- **Salida:** 
  - solicitudes-service (obtener datos de solicitud)
  - depositos-service (buscar depósitos)
  - camiones-service (buscar camiones disponibles)
  - tarifas-service (obtener tarifas)
  - Google Maps API (calcular distancias)

---

### 8. Keycloak (Sistema Externo)
**Tecnología:** Keycloak Identity Provider  
**Puerto:** 8180  
**Base de Datos:** PostgreSQL (puerto 5433)

**Responsabilidad:** Autenticación y autorización

**Funcionalidades:**
- Gestión de usuarios
- Emisión de tokens JWT
- Gestión de roles (CLIENTE, OPERADOR, TRANSPORTISTA, ADMIN)
- OAuth2/OpenID Connect

**Comunicación:**
- **Entrada:** API Gateway (validación de tokens)
- **Salida:** Ninguna

---

### 9. Google Maps API (Sistema Externo)
**Tecnología:** Google Maps Distance Matrix API  
**Protocolo:** HTTPS/REST

**Responsabilidad:** Cálculo de distancias reales

**Funcionalidades:**
- Calcular distancia entre dos coordenadas
- Considerar rutas de carretera reales
- Tiempo estimado de viaje

**Comunicación:**
- **Entrada:** rutas-service
- **Salida:** Ninguna

---

## Patrones de Comunicación

### 1. Comunicación Síncrona (REST)
\`\`\`
Cliente → API Gateway → Microservicio → Base de Datos
\`\`\`

**Características:**
- Protocolo: HTTP/REST
- Formato: JSON
- Autenticación: JWT Bearer Token
- Timeout: 30 segundos

**Ejemplo:**
\`\`\`
POST /api/solicitudes
Authorization: Bearer eyJhbGc...
Content-Type: application/json

{
  "clienteId": 1,
  "contenedor": { ... }
}
\`\`\`

---

### 2. Base de Datos por Servicio
\`\`\`
Microservicio ←→ Base de Datos PostgreSQL exclusiva
\`\`\`

**Características:**
- Cada microservicio tiene su propia BD
- No hay acceso directo entre BDs
- Comunicación solo vía APIs REST

**Ventajas:**
- Independencia de datos
- Escalabilidad independiente
- Cambios de esquema sin afectar otros servicios

---

### 3. API Gateway Pattern
\`\`\`
Todos los clientes → API Gateway → Microservicios
\`\`\`

**Características:**
- Punto único de entrada
- Validación centralizada de JWT
- Enrutamiento inteligente
- Rate limiting y circuit breakers

---

## Flujo de Datos Completo

### Ejemplo: Crear Solicitud y Calcular Ruta

\`\`\`
1. Cliente envía solicitud
   ClienteUI → API Gateway → solicitudes-service
   
2. Solicitudes valida cliente
   solicitudes-service → clientes-service (GET /api/clientes/{id})
   
3. Solicitudes crea contenedor y solicitud
   solicitudes-service → BD Solicitudes
   
4. Operador calcula rutas
   OperadorUI → API Gateway → rutas-service
   
5. Rutas obtiene datos de solicitud
   rutas-service → solicitudes-service (GET /api/solicitudes/{id})
   
6. Rutas busca depósitos cercanos
   rutas-service → depositos-service (GET /api/depositos/cercanos)
   
7. Rutas calcula distancias reales
   rutas-service → Google Maps API (POST /distancematrix/json)
   
8. Rutas obtiene tarifas vigentes
   rutas-service → tarifas-service (GET /api/tarifas/vigentes)
   
9. Rutas calcula costos
   rutas-service → tarifas-service (POST /api/tarifas/calcular-estimado)
   
10. Rutas devuelve opciones
    rutas-service → API Gateway → OperadorUI
\`\`\`

---

## Decisiones de Arquitectura

### ¿Por qué 6 microservicios?

**Balance óptimo entre:**
- Complejidad manejable
- Beneficios de microservicios
- Requisitos del dominio

**Alternativas rechazadas:**
- **Monolito:** Menos escalable, despliegues riesgosos
- **Menos servicios (3-4):** Servicios muy grandes, menos granularidad
- **Más servicios (10+):** Complejidad operacional excesiva

---

### ¿Por qué API Gateway?

**Ventajas:**
- Punto único de entrada
- Autenticación centralizada
- Simplifica clientes
- Rate limiting y seguridad

**Alternativa rechazada:**
- **Acceso directo a microservicios:** Complejidad en clientes, seguridad distribuida

---

### ¿Por qué Base de Datos por Servicio?

**Ventajas:**
- Independencia de datos
- Escalabilidad independiente
- Tecnología específica por servicio

**Desventaja:**
- No hay transacciones ACID entre servicios
- **Mitigación:** Patrón Saga (futuro), compensación manual

---

### ¿Por qué Keycloak externo?

**Ventajas:**
- Especializado en autenticación
- Estándares OAuth2/OpenID Connect
- Gestión centralizada de usuarios
- Reutilizable por todos los servicios

**Alternativa rechazada:**
- **Autenticación propia:** Reinventar la rueda, menos seguro

---

## Escalabilidad

### Escalado Horizontal

Cada contenedor puede escalarse independientemente:

\`\`\`
API Gateway: 2-3 instancias (load balancer)
clientes-service: 1-2 instancias (carga baja)
depositos-service: 1-2 instancias (carga baja)
camiones-service: 1-2 instancias (carga media)
tarifas-service: 1-2 instancias (carga baja)
solicitudes-service: 2-3 instancias (carga alta)
rutas-service: 3-5 instancias (carga muy alta, cálculos complejos)
\`\`\`

### Escalado de Bases de Datos

\`\`\`
PostgreSQL: Read replicas para consultas
Particionamiento por fecha (solicitudes, historial)
Índices optimizados en campos de búsqueda frecuente
\`\`\`

---

## Resiliencia

### Circuit Breakers

API Gateway implementa circuit breakers para cada microservicio:

\`\`\`
Si un servicio falla:
1. Circuit breaker se abre
2. Peticiones fallan rápido (no esperan timeout)
3. Después de N segundos, intenta de nuevo
4. Si funciona, circuit breaker se cierra
\`\`\`

### Fallbacks

\`\`\`
Si rutas-service no puede calcular con Google Maps:
→ Usa cálculo de Haversine como fallback
\`\`\`

---

## Seguridad

### Autenticación

\`\`\`
1. Usuario se autentica en Keycloak
2. Keycloak emite JWT token
3. Cliente envía token en cada petición
4. API Gateway valida token con Keycloak
5. Si válido, enruta a microservicio
6. Microservicio valida roles en token
\`\`\`

### Autorización

\`\`\`
Roles en JWT:
- ROLE_CLIENTE: Acceso limitado a sus recursos
- ROLE_OPERADOR: Gestión de recursos
- ROLE_TRANSPORTISTA: Registro de viajes
- ROLE_ADMIN: Acceso total
\`\`\`

---

## Tecnologías Utilizadas

| Componente | Tecnología | Versión |
|------------|------------|---------|
| Microservicios | Spring Boot | 3.2.x |
| API Gateway | Spring Cloud Gateway | 4.1.x |
| Persistencia | JPA/Hibernate | 6.x |
| Base de Datos | PostgreSQL | 15.x |
| Autenticación | Keycloak | 23.x |
| Contenedores | Docker | 24.x |
| Orquestación | Docker Compose | 2.x |
| API Externa | Google Maps API | v1 |

---

## Conclusión

Este diagrama C4 Nivel 2 muestra la arquitectura de contenedores del sistema de logística de transporte de contenedores. La arquitectura de microservicios con API Gateway, bases de datos independientes y servicios externos (Keycloak, Google Maps) proporciona un sistema escalable, resiliente y mantenible que cumple con todos los requisitos del proyecto.
