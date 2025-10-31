# Sistema de Logística de Transporte de Contenedores

## Trabajo Práctico Integrador - Backend de Aplicaciones 2025

Este proyecto implementa un sistema backend basado en microservicios para gestionar la logística de transporte terrestre de contenedores utilizados en la construcción de viviendas.

---

## Documentación del Proyecto

### Diseño Inicial

1. **[DER - Modelo de Datos](docs/01-DER-modelo-datos.md)**
   - Diseño completo de la base de datos
   - Entidades, atributos y relaciones
   - Estrategia de bases de datos por microservicio

2. **[Arquitectura de Microservicios](docs/02-arquitectura-microservicios.md)**
   - Diseño de contenedores y servicios
   - Comunicación entre microservicios
   - Integración con Keycloak y Google Maps API
   - Configuración de Docker

3. **[Definición de Endpoints](docs/03-definicion-endpoints.md)**
   - Endpoints REST por microservicio
   - Roles y permisos de acceso
   - Ejemplos de request/response
   - Códigos de estado HTTP

### Implementación

4. **[Fase 2 - Servicios de Orquestación](docs/04-fase2-servicios-orquestacion.md)**
   - Solicitudes Service: Gestión de solicitudes y contenedores
   - Rutas Service: Cálculo de rutas y asignación de tramos
   - Comunicación entre microservicios
   - Flujo de operación completo

5. **[API Gateway](docs/05-api-gateway.md)**
   - Configuración del gateway central
   - Enrutamiento y circuit breakers
   - CORS y logging global
   - Endpoints de monitoreo

---

## Stack Tecnológico

- **Lenguaje:** Java 17+
- **Framework:** Spring Boot 3.2.0
- **Persistencia:** JPA/Hibernate, Spring Data JPA
- **Base de datos:** PostgreSQL (producción), H2 (desarrollo)
- **Seguridad:** Keycloak, Spring Security, JWT (pendiente)
- **API Gateway:** Spring Cloud Gateway
- **Documentación:** Swagger/OpenAPI
- **Testing:** JUnit 5, Mockito
- **Contenedorización:** Docker, Docker Compose
- **Build:** Maven
- **Utilidades:** Lombok, Bean Validation

---

## Microservicios Implementados

### Fase 1 - Servicios Base ✅

1. **Clientes Service** (Puerto 8081)
   - Gestión de clientes
   - CRUD completo con validaciones
   - Búsqueda por email y documento

2. **Depósitos Service** (Puerto 8082)
   - Gestión de depósitos
   - Búsqueda de depósitos cercanos por coordenadas
   - Cálculo de distancias con fórmula de Haversine

3. **Camiones Service** (Puerto 8083)
   - Gestión de camiones y transportistas
   - Control de disponibilidad
   - Búsqueda por capacidad (peso y volumen)

4. **Tarifas Service** (Puerto 8084)
   - Gestión de tarifas con vigencia temporal
   - Tipos: COSTO_KM_BASE, COSTO_COMBUSTIBLE_LITRO, CARGO_GESTION_FIJO, ESTADIA_DEPOSITO
   - Consulta de tarifas vigentes por tipo

### Fase 2 - Servicios de Orquestación ✅

5. **Solicitudes Service** (Puerto 8085)
   - Gestión de solicitudes de transporte
   - Gestión de contenedores
   - Estados: BORRADOR, PROGRAMADA, EN_TRANSITO, ENTREGADA
   - Seguimiento detallado para clientes
   - Comunicación con clientes-service

6. **Rutas Service** (Puerto 8086)
   - Cálculo de rutas tentativas
   - Gestión de tramos de transporte
   - Asignación de camiones a tramos
   - Inicio y finalización de tramos por transportistas
   - Cálculo de costos estimados y reales
   - Comunicación con múltiples servicios

### Fase 3 - Infraestructura ✅

7. **API Gateway** (Puerto 8080)
   - Punto de entrada único al sistema
   - Enrutamiento a todos los microservicios
   - Circuit breakers con fallbacks
   - CORS global configurado
   - Logging de peticiones y respuestas
   - Endpoints de monitoreo con Actuator

---

## Arquitectura del Sistema

\`\`\`
Cliente/Frontend
       ↓
   API Gateway (8080)
       ↓
   ┌───┴───┬───────┬──────────┬──────────┬────────────┬────────────┐
   ↓       ↓       ↓          ↓          ↓            ↓            ↓
clientes depositos camiones tarifas solicitudes    rutas
 (8081)   (8082)   (8083)   (8084)    (8085)       (8086)
   ↓       ↓       ↓          ↓          ↓            ↓
PostgreSQL PostgreSQL PostgreSQL PostgreSQL PostgreSQL PostgreSQL
\`\`\`

---

## Roles del Sistema

- **ROLE_CLIENTE:** Puede crear solicitudes y consultar el estado de sus contenedores
- **ROLE_OPERADOR:** Gestiona depósitos, camiones, rutas y asignaciones
- **ROLE_TRANSPORTISTA:** Registra inicio y fin de tramos asignados
- **ROLE_ADMIN:** Acceso completo al sistema y configuración de tarifas

---

## Reglas de Negocio Implementadas

1. ✅ Un camión no puede transportar contenedores que superen su capacidad de peso o volumen
2. ✅ Validación de disponibilidad de camiones antes de asignación
3. ✅ Gestión de estados de solicitudes con transiciones válidas
4. ✅ Gestión de estados de tramos: ESTIMADO → ASIGNADO → INICIADO → FINALIZADO
5. ✅ Cálculo de costos estimados basado en tarifas vigentes
6. ✅ Cálculo de costos reales al finalizar tramos
7. ✅ Liberación automática de camiones al finalizar tramos
8. ✅ Actualización de estado de solicitud según progreso de tramos
9. ✅ Búsqueda de depósitos cercanos para optimización de rutas
10. ✅ Validación de clientes existentes al crear solicitudes

---

## Ejecución del Proyecto

### Requisitos Previos

- Java 17+
- Maven 3.8+
- Docker y Docker Compose
- PostgreSQL (o usar H2 para desarrollo)

### Ejecución Individual de Servicios

Cada servicio puede ejecutarse independientemente en modo desarrollo con H2:

\`\`\`bash
# Ejemplo: Clientes Service
cd clientes-service
mvn spring-boot:run
\`\`\`

### Ejecución con Docker

\`\`\`bash
# Construir imagen de un servicio
cd clientes-service
docker build -t clientes-service:1.0.0 .
docker run -p 8081:8081 clientes-service:1.0.0
\`\`\`

### Acceso a Swagger UI

Cada servicio expone su documentación Swagger:

- Clientes: http://localhost:8080/clientes/swagger-ui/index.html
- Depósitos: http://localhost:8080/depositos/swagger-ui/index.html
- Camiones: http://localhost:8080/camiones/swagger-ui/index.html
- Tarifas: http://localhost:8080/tarifas/swagger-ui/index.html
- Solicitudes: http://localhost:8080/solicitudes/swagger-ui/index.html
- Rutas: http://localhost:8080/rutas/swagger-ui/index.html

---

## Próximos Pasos

### Pendientes de Implementación

- [ ] Docker Compose completo con todos los servicios
- [ ] Integración con Google Maps API para cálculo real de distancias
- [ ] Configuración de Keycloak para autenticación y autorización
- [ ] Implementación de seguridad JWT en todos los servicios
- [ ] Tests de integración end-to-end
- [ ] Colección de pruebas completa (Postman/Bruno)
- [ ] Scripts de inicialización de datos de prueba
- [ ] Monitoreo y observabilidad (logs centralizados)

### Mejoras Futuras

- [ ] Circuit breakers con Resilience4j
- [ ] Comunicación asíncrona con mensajería (RabbitMQ/Kafka)
- [ ] Cache distribuido con Redis
- [ ] Service Discovery con Eureka
- [ ] Distributed Tracing con Zipkin/Jaeger
- [ ] Patrón Saga para transacciones distribuidas

---

## Estructura del Proyecto

\`\`\`
tpi-logistica/
├── api-gateway/                 # API Gateway (Spring Cloud Gateway)
├── clientes-service/            # Gestión de clientes
├── depositos-service/           # Gestión de depósitos
├── camiones-service/            # Gestión de camiones
├── tarifas-service/             # Gestión de tarifas
├── solicitudes-service/         # Gestión de solicitudes y contenedores
├── rutas-service/               # Cálculo de rutas y tramos
├── docs/                        # Documentación del proyecto
│   ├── 01-DER-modelo-datos.md
│   ├── 02-arquitectura-microservicios.md
│   ├── 03-definicion-endpoints.md
│   ├── 04-fase2-servicios-orquestacion.md
│   └── 05-api-gateway.md
├── docker-compose.yml           # (Pendiente)
└── README.md
\`\`\`

---

## Autores

[Nombres de los integrantes del grupo]

---

## Licencia

Proyecto académico - Backend de Aplicaciones 2025
