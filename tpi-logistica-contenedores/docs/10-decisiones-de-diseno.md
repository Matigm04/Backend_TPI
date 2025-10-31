# Decisiones de Diseño - TPI Logística de Contenedores

## Índice
1. [Introducción](#introducción)
2. [Arquitectura de Microservicios](#arquitectura-de-microservicios)
3. [Modelo de Datos (DER)](#modelo-de-datos-der)
4. [Decisiones Técnicas](#decisiones-técnicas)
5. [Patrones y Buenas Prácticas](#patrones-y-buenas-prácticas)
6. [Seguridad](#seguridad)
7. [Integraciones Externas](#integraciones-externas)
8. [Infraestructura y Despliegue](#infraestructura-y-despliegue)
9. [Alternativas Consideradas](#alternativas-consideradas)
10. [Conclusiones](#conclusiones)

---

## Introducción

Este documento explica las decisiones de diseño tomadas para el desarrollo del sistema de logística de transporte de contenedores. Cada decisión fue evaluada considerando los requerimientos funcionales, las restricciones técnicas del proyecto, la escalabilidad, el mantenimiento y las buenas prácticas de desarrollo de software.

**Equipo:** 4 integrantes  
**Objetivo:** Sistema backend con microservicios para gestión de logística de contenedores

---

## Arquitectura de Microservicios

### Decisión: Arquitectura basada en 6 microservicios + API Gateway

#### Microservicios implementados:

1. **clientes-service** (Puerto 8081)
2. **depositos-service** (Puerto 8082)
3. **camiones-service** (Puerto 8083)
4. **tarifas-service** (Puerto 8084)
5. **solicitudes-service** (Puerto 8085)
6. **rutas-service** (Puerto 8086)
7. **api-gateway** (Puerto 8080)

### ¿Por qué esta división?

#### 1. Separación por Bounded Context (DDD)

Cada microservicio representa un **contexto delimitado** del dominio de negocio:

- **clientes-service**: Gestión de clientes y sus datos de contacto
- **depositos-service**: Gestión de ubicaciones de almacenamiento temporal
- **camiones-service**: Gestión de flota y transportistas
- **tarifas-service**: Configuración de costos y tarifas del sistema
- **solicitudes-service**: Orquestación de solicitudes de transporte y contenedores
- **rutas-service**: Cálculo y gestión de rutas con tramos

**Justificación:**
- Cada servicio tiene una **responsabilidad única y bien definida**
- Permite que equipos diferentes trabajen en servicios diferentes sin conflictos
- Facilita el escalamiento independiente de cada componente

#### 2. Independencia de Datos

**Decisión:** Cada microservicio tiene su propia base de datos PostgreSQL

**Ventajas:**
- **Autonomía**: Cada servicio puede evolucionar su esquema sin afectar a otros
- **Escalabilidad**: Podemos escalar bases de datos específicas según la carga
- **Resiliencia**: Si una base de datos falla, no afecta a todos los servicios
- **Tecnología agnóstica**: En el futuro podríamos usar diferentes DBMS por servicio

**Desventajas consideradas:**
- Mayor complejidad en transacciones distribuidas
- Necesidad de comunicación entre servicios para obtener datos relacionados
- Duplicación potencial de algunos datos

**Mitigación:**
- Usamos comunicación REST síncrona para consultas simples
- Implementamos patrones de resiliencia (circuit breakers, timeouts)
- Mantenemos referencias por ID entre servicios

#### 3. API Gateway como Punto de Entrada Único

**Decisión:** Implementar un API Gateway con Spring Cloud Gateway

**Ventajas:**
- **Punto de entrada único**: Simplifica el acceso desde clientes externos
- **Enrutamiento centralizado**: Facilita la gestión de rutas
- **Seguridad centralizada**: Validación de JWT en un solo lugar
- **Cross-cutting concerns**: Logging, CORS, rate limiting centralizados
- **Abstracción**: Los clientes no necesitan conocer la topología interna

**Alternativas consideradas:**
- **Sin Gateway**: Cada servicio expuesto directamente
  - ❌ Rechazada: Mayor complejidad para clientes, seguridad distribuida
- **Kong o NGINX**: Gateways externos
  - ❌ Rechazada: Mayor complejidad de configuración, preferimos stack Java

---

## Modelo de Datos (DER)

### Decisión: Modelo relacional normalizado con bases de datos separadas

### Estructura por Microservicio

#### 1. clientes-service

**Entidad: Cliente**

\`\`\`
Cliente
├── id (PK, BIGINT, AUTO_INCREMENT)
├── nombre (VARCHAR(100), NOT NULL)
├── apellido (VARCHAR(100), NOT NULL)
├── email (VARCHAR(150), UNIQUE, NOT NULL)
├── telefono (VARCHAR(20), NOT NULL)
├── direccion (VARCHAR(255))
├── activo (BOOLEAN, DEFAULT true)
├── fecha_creacion (TIMESTAMP)
└── fecha_actualizacion (TIMESTAMP)

Índices:
- PRIMARY KEY (id)
- UNIQUE INDEX (email)
- INDEX (activo)
\`\`\`

**Justificación:**
- Modelo simple y autocontenido
- Email único para evitar duplicados
- Soft delete con campo `activo`
- Auditoría con fechas de creación/actualización

#### 2. depositos-service

**Entidad: Deposito**

\`\`\`
Deposito
├── id (PK, BIGINT, AUTO_INCREMENT)
├── nombre (VARCHAR(100), NOT NULL)
├── direccion (VARCHAR(255), NOT NULL)
├── latitud (DOUBLE, NOT NULL)
├── longitud (DOUBLE, NOT NULL)
├── costo_diario_estadia (DECIMAL(10,2), NOT NULL)
├── capacidad_maxima (INTEGER)
├── capacidad_actual (INTEGER, DEFAULT 0)
├── activo (BOOLEAN, DEFAULT true)
├── fecha_creacion (TIMESTAMP)
└── fecha_actualizacion (TIMESTAMP)

Índices:
- PRIMARY KEY (id)
- INDEX (activo)
- INDEX (latitud, longitud) -- Para búsquedas geográficas
\`\`\`

**Justificación:**
- Geolocalización precisa con latitud/longitud
- Control de capacidad para evitar sobrecarga
- Costo diario configurable por depósito
- Índice compuesto para búsquedas geográficas eficientes

#### 3. camiones-service

**Entidad: Camion**

\`\`\`
Camion
├── id (PK, BIGINT, AUTO_INCREMENT)
├── dominio (VARCHAR(20), UNIQUE, NOT NULL)
├── nombre_transportista (VARCHAR(150), NOT NULL)
├── telefono_transportista (VARCHAR(20), NOT NULL)
├── capacidad_peso_kg (DECIMAL(10,2), NOT NULL)
├── capacidad_volumen_m3 (DECIMAL(10,2), NOT NULL)
├── consumo_combustible_km_litro (DECIMAL(5,2), NOT NULL)
├── costo_por_km (DECIMAL(10,2), NOT NULL)
├── disponible (BOOLEAN, DEFAULT true)
├── activo (BOOLEAN, DEFAULT true)
├── fecha_creacion (TIMESTAMP)
└── fecha_actualizacion (TIMESTAMP)

Índices:
- PRIMARY KEY (id)
- UNIQUE INDEX (dominio)
- INDEX (disponible, activo) -- Para búsquedas de camiones disponibles
\`\`\`

**Justificación:**
- Dominio/patente único para identificación
- Capacidades en unidades estándar (kg, m³)
- Consumo de combustible para cálculo de costos reales
- Costo por km diferenciado por camión
- Flag de disponibilidad para asignación de tramos

#### 4. tarifas-service

**Entidad: Tarifa**

\`\`\`
Tarifa
├── id (PK, BIGINT, AUTO_INCREMENT)
├── tipo (VARCHAR(50), NOT NULL)
├── descripcion (VARCHAR(255))
├── valor (DECIMAL(10,2), NOT NULL)
├── unidad (VARCHAR(20), NOT NULL)
├── vigencia_desde (DATE, NOT NULL)
├── vigencia_hasta (DATE)
├── activo (BOOLEAN, DEFAULT true)
├── fecha_creacion (TIMESTAMP)
└── fecha_actualizacion (TIMESTAMP)

Tipos de Tarifa:
- COSTO_KM_BASE
- COSTO_COMBUSTIBLE_LITRO
- CARGO_GESTION_FIJO
- ESTADIA_DEPOSITO

Índices:
- PRIMARY KEY (id)
- INDEX (tipo, activo)
- INDEX (vigencia_desde, vigencia_hasta) -- Para búsquedas por vigencia
\`\`\`

**Justificación:**
- Sistema flexible de tarifas por tipo
- Vigencia temporal para cambios de precios
- Permite múltiples tarifas activas simultáneamente
- Unidad configurable ($/km, $/litro, $/día, etc.)

#### 5. solicitudes-service

**Entidades: Solicitud y Contenedor**

\`\`\`
Contenedor
├── id (PK, BIGINT, AUTO_INCREMENT)
├── identificacion (VARCHAR(50), UNIQUE, NOT NULL)
├── peso_kg (DECIMAL(10,2), NOT NULL)
├── volumen_m3 (DECIMAL(10,2), NOT NULL)
├── direccion_origen (VARCHAR(255), NOT NULL)
├── latitud_origen (DOUBLE, NOT NULL)
├── longitud_origen (DOUBLE, NOT NULL)
├── direccion_destino (VARCHAR(255), NOT NULL)
├── latitud_destino (DOUBLE, NOT NULL)
├── longitud_destino (DOUBLE, NOT NULL)
├── fecha_creacion (TIMESTAMP)
└── fecha_actualizacion (TIMESTAMP)

Solicitud
├── id (PK, BIGINT, AUTO_INCREMENT)
├── numero (VARCHAR(20), UNIQUE, NOT NULL)
├── cliente_id (BIGINT, NOT NULL) -- Referencia externa a clientes-service
├── contenedor_id (BIGINT, NOT NULL, FK)
├── estado (VARCHAR(20), NOT NULL)
├── costo_estimado (DECIMAL(10,2))
├── tiempo_estimado_horas (INTEGER)
├── costo_final (DECIMAL(10,2))
├── tiempo_real_horas (INTEGER)
├── fecha_creacion (TIMESTAMP)
├── fecha_actualizacion (TIMESTAMP)
└── FOREIGN KEY (contenedor_id) REFERENCES Contenedor(id)

Estados de Solicitud:
- BORRADOR
- PROGRAMADA
- EN_TRANSITO
- ENTREGADA

Índices:
- PRIMARY KEY (id)
- UNIQUE INDEX (numero)
- INDEX (cliente_id)
- INDEX (estado)
- INDEX (fecha_creacion)
\`\`\`

**Justificación:**
- Separación entre Contenedor (objeto físico) y Solicitud (proceso de negocio)
- Contenedor con identificación única para trazabilidad
- Geolocalización completa de origen y destino
- Estados claros del ciclo de vida de la solicitud
- Costos y tiempos estimados vs reales para análisis de desempeño
- Referencia externa a cliente_id (no FK física para mantener independencia)

#### 6. rutas-service

**Entidades: Ruta y Tramo**

\`\`\`
Ruta
├── id (PK, BIGINT, AUTO_INCREMENT)
├── solicitud_id (BIGINT, NOT NULL) -- Referencia externa a solicitudes-service
├── cantidad_tramos (INTEGER, NOT NULL)
├── cantidad_depositos (INTEGER, NOT NULL)
├── distancia_total_km (DECIMAL(10,2))
├── costo_estimado (DECIMAL(10,2))
├── tiempo_estimado_horas (INTEGER)
├── fecha_creacion (TIMESTAMP)
└── fecha_actualizacion (TIMESTAMP)

Tramo
├── id (PK, BIGINT, AUTO_INCREMENT)
├── ruta_id (BIGINT, NOT NULL, FK)
├── orden (INTEGER, NOT NULL)
├── origen_tipo (VARCHAR(20), NOT NULL)
├── origen_id (BIGINT)
├── origen_direccion (VARCHAR(255), NOT NULL)
├── origen_latitud (DOUBLE, NOT NULL)
├── origen_longitud (DOUBLE, NOT NULL)
├── destino_tipo (VARCHAR(20), NOT NULL)
├── destino_id (BIGINT)
├── destino_direccion (VARCHAR(255), NOT NULL)
├── destino_latitud (DOUBLE, NOT NULL)
├── destino_longitud (DOUBLE, NOT NULL)
├── tipo_tramo (VARCHAR(30), NOT NULL)
├── estado (VARCHAR(20), NOT NULL)
├── distancia_km (DECIMAL(10,2))
├── costo_aproximado (DECIMAL(10,2))
├── costo_real (DECIMAL(10,2))
├── fecha_hora_inicio (TIMESTAMP)
├── fecha_hora_fin (TIMESTAMP)
├── camion_id (BIGINT) -- Referencia externa a camiones-service
├── fecha_creacion (TIMESTAMP)
└── fecha_actualizacion (TIMESTAMP)
└── FOREIGN KEY (ruta_id) REFERENCES Ruta(id)

Tipos de Origen/Destino:
- ORIGEN
- DEPOSITO
- DESTINO

Tipos de Tramo:
- ORIGEN_DEPOSITO
- DEPOSITO_DEPOSITO
- DEPOSITO_DESTINO
- ORIGEN_DESTINO

Estados de Tramo:
- ESTIMADO
- ASIGNADO
- INICIADO
- FINALIZADO

Índices:
- PRIMARY KEY (id)
- INDEX (ruta_id, orden) -- Para ordenar tramos de una ruta
- INDEX (estado)
- INDEX (camion_id)
\`\`\`

**Justificación:**
- Ruta como contenedor de tramos secuenciales
- Tramo con información completa de origen y destino
- Flexibilidad para diferentes tipos de tramos
- Estados para seguimiento del progreso
- Orden explícito de tramos en la ruta
- Costos aproximados vs reales para análisis
- Referencias externas a solicitud_id y camion_id

### Decisiones Clave del Modelo de Datos

#### 1. Referencias Externas vs Foreign Keys

**Decisión:** Usar referencias por ID sin Foreign Keys físicas entre microservicios

**Justificación:**
- Mantiene la **independencia de las bases de datos**
- Evita acoplamiento a nivel de base de datos
- Permite que cada servicio evolucione independientemente
- La integridad referencial se maneja a nivel de aplicación

**Ejemplo:**
\`\`\`java
// En Solicitud
private Long clienteId; // Referencia a clientes-service, NO FK física
\`\`\`

#### 2. Soft Delete

**Decisión:** Usar campo `activo` en lugar de eliminar registros

**Justificación:**
- Mantiene historial completo
- Permite auditoría y trazabilidad
- Facilita recuperación de datos
- Cumple con regulaciones de retención de datos

#### 3. Auditoría Temporal

**Decisión:** Incluir `fecha_creacion` y `fecha_actualizacion` en todas las entidades

**Justificación:**
- Trazabilidad de cambios
- Análisis temporal de datos
- Debugging y troubleshooting
- Cumplimiento de auditoría

#### 4. Tipos de Datos

**Decisión:** 
- `DECIMAL` para valores monetarios y medidas precisas
- `DOUBLE` para coordenadas geográficas
- `VARCHAR` con límites razonables
- `TIMESTAMP` para fechas con hora

**Justificación:**
- `DECIMAL` evita errores de redondeo en cálculos financieros
- `DOUBLE` es estándar para coordenadas GPS
- Límites en VARCHAR previenen abusos de almacenamiento
- `TIMESTAMP` incluye zona horaria

---

## Decisiones Técnicas

### 1. Stack Tecnológico

#### Java + Spring Boot

**Decisión:** Java 17 con Spring Boot 3.x

**Justificación:**
- **Requerimiento del curso**: Uso obligatorio de Java y Spring
- **Ecosistema maduro**: Amplia documentación y comunidad
- **Spring Boot**: Configuración por convención, reduce boilerplate
- **Spring Data JPA**: Simplifica acceso a datos
- **Spring Cloud Gateway**: Solución nativa para API Gateway

#### PostgreSQL

**Decisión:** PostgreSQL como DBMS principal

**Justificación:**
- **Open source**: Sin costos de licencia
- **Robusto**: Excelente para aplicaciones empresariales
- **Soporte geoespacial**: Extensión PostGIS para futuras mejoras
- **ACID compliant**: Garantiza integridad de datos
- **Ampliamente usado**: Fácil encontrar recursos y soporte

**Alternativas consideradas:**
- **MySQL**: Similar, pero PostgreSQL tiene mejor soporte para tipos de datos complejos
- **MongoDB**: NoSQL, rechazada por requerimiento de modelo relacional
- **H2**: Solo para desarrollo/testing

#### Maven

**Decisión:** Maven como gestor de dependencias

**Justificación:**
- **Requerimiento del curso**: Uso obligatorio
- **Estándar de la industria**: Ampliamente adoptado
- **Gestión de dependencias**: Manejo automático de versiones
- **Build reproducible**: Garantiza consistencia entre entornos

### 2. Arquitectura en Capas

**Decisión:** Arquitectura en 4 capas por microservicio

\`\`\`
Controller (API REST)
    ↓
Service (Lógica de negocio)
    ↓
Repository (Acceso a datos)
    ↓
Model (Entidades JPA)
\`\`\`

**Justificación:**
- **Separación de responsabilidades**: Cada capa tiene un propósito claro
- **Testabilidad**: Fácil crear tests unitarios por capa
- **Mantenibilidad**: Cambios en una capa no afectan a otras
- **Reusabilidad**: Lógica de negocio independiente de la capa de presentación

**Componentes adicionales:**
- **DTOs**: Separación entre modelo de dominio y API
- **Mappers**: Conversión entre entidades y DTOs
- **Exception Handlers**: Manejo centralizado de errores

### 3. Comunicación entre Microservicios

**Decisión:** Comunicación REST síncrona con RestTemplate

**Justificación:**
- **Simplicidad**: Fácil de implementar y entender
- **Debugging**: Más sencillo rastrear llamadas
- **Consistencia**: Todos los servicios usan el mismo protocolo
- **Adecuado para el alcance**: El sistema no requiere alta concurrencia

**Alternativas consideradas:**
- **Mensajería asíncrona (RabbitMQ, Kafka)**: 
  - ❌ Mayor complejidad para el alcance del proyecto
  - ✅ Considerada para futuras mejoras
- **gRPC**: 
  - ❌ Mayor curva de aprendizaje
  - ❌ Menos documentación en español

**Patrones implementados:**
- **Circuit Breaker**: Timeouts y manejo de fallos
- **Fallback**: Respuestas por defecto en caso de error
- **Retry**: Reintentos automáticos con backoff

### 4. Validación de Datos

**Decisión:** Validación en múltiples niveles

**Niveles de validación:**
1. **DTOs**: Anotaciones de Bean Validation (`@NotNull`, `@Size`, etc.)
2. **Service**: Validaciones de negocio complejas
3. **Base de datos**: Constraints (UNIQUE, NOT NULL, CHECK)

**Justificación:**
- **Defensa en profundidad**: Múltiples capas de protección
- **Mensajes claros**: Errores específicos por nivel
- **Integridad**: Garantiza datos consistentes

### 5. Manejo de Errores

**Decisión:** Exception Handlers globales con respuestas estandarizadas

**Estructura de respuesta de error:**
\`\`\`json
{
  "timestamp": "2025-01-15T10:30:00",
  "status": 404,
  "error": "Not Found",
  "message": "Cliente no encontrado con ID: 123",
  "path": "/api/clientes/123"
}
\`\`\`

**Justificación:**
- **Consistencia**: Todas las APIs responden igual ante errores
- **Debugging**: Información completa para troubleshooting
- **UX**: Mensajes claros para el cliente de la API

---

## Patrones y Buenas Prácticas

### 1. Repository Pattern

**Implementación:** Spring Data JPA con interfaces Repository

\`\`\`java
public interface ClienteRepository extends JpaRepository<Cliente, Long> {
    Optional<Cliente> findByEmail(String email);
    List<Cliente> findByActivoTrue();
}
\`\`\`

**Ventajas:**
- Abstracción del acceso a datos
- Queries derivadas automáticamente
- Reducción de código boilerplate

### 2. DTO Pattern

**Implementación:** DTOs separados para Request y Response

\`\`\`java
// Request DTO - Solo datos necesarios para crear/actualizar
public class ClienteRequestDTO {
    private String nombre;
    private String apellido;
    private String email;
    // ...
}

// Response DTO - Incluye datos calculados y referencias
public class ClienteResponseDTO {
    private Long id;
    private String nombre;
    private String apellido;
    private String email;
    private LocalDateTime fechaCreacion;
    // ...
}
\`\`\`

**Ventajas:**
- Oculta detalles internos del modelo
- Evita over-fetching y under-fetching
- Permite evolución independiente de API y modelo

### 3. Service Layer Pattern

**Implementación:** Lógica de negocio en capa Service

\`\`\`java
@Service
public class ClienteService {
    
    @Transactional
    public ClienteResponseDTO crearCliente(ClienteRequestDTO request) {
        // Validaciones de negocio
        // Transformaciones
        // Persistencia
        // Retorno de DTO
    }
}
\`\`\`

**Ventajas:**
- Transacciones gestionadas por Spring
- Lógica reutilizable
- Fácil de testear con mocks

### 4. Builder Pattern (Lombok)

**Implementación:** Uso de `@Builder` de Lombok

\`\`\`java
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Cliente {
    // ...
}

// Uso
Cliente cliente = Cliente.builder()
    .nombre("Juan")
    .apellido("Pérez")
    .email("juan@example.com")
    .build();
\`\`\`

**Ventajas:**
- Código más legible
- Inmutabilidad opcional
- Reduce boilerplate

### 5. Soft Delete Pattern

**Implementación:** Campo `activo` en lugar de DELETE físico

\`\`\`java
@Service
public class ClienteService {
    
    public void eliminarCliente(Long id) {
        Cliente cliente = obtenerClientePorId(id);
        cliente.setActivo(false);
        clienteRepository.save(cliente);
    }
}
\`\`\`

**Ventajas:**
- Mantiene historial
- Permite recuperación
- Auditoría completa

---

## Seguridad

### Decisión: Keycloak + JWT

**Implementación:**
- **Keycloak**: Identity Provider (IdP) centralizado
- **JWT**: Tokens para autenticación stateless
- **Spring Security**: Validación de tokens en cada microservicio

### Arquitectura de Seguridad

\`\`\`
Cliente → API Gateway → Keycloak (validación) → Microservicio
                ↓
            JWT Token
\`\`\`

### Roles Implementados

1. **CLIENTE**
   - Crear solicitudes
   - Consultar sus propias solicitudes
   - Ver estado de contenedores

2. **OPERADOR**
   - Todas las operaciones de CLIENTE
   - Gestionar depósitos, camiones, tarifas
   - Asignar rutas y camiones
   - Ver todas las solicitudes

3. **TRANSPORTISTA**
   - Ver tramos asignados
   - Iniciar/finalizar tramos
   - Actualizar estado de transporte

4. **ADMIN**
   - Acceso completo al sistema
   - Gestión de usuarios y roles

### Justificación de Keycloak

**Ventajas:**
- **Open source**: Sin costos de licencia
- **Estándar OAuth2/OIDC**: Protocolo ampliamente adoptado
- **Gestión centralizada**: Un solo lugar para usuarios y roles
- **Federación**: Permite integración con LDAP, Active Directory, etc.
- **UI administrativa**: Interfaz web para gestión

**Alternativas consideradas:**
- **Spring Security básico**: 
  - ❌ Requiere implementar gestión de usuarios manualmente
- **Auth0**: 
  - ❌ Servicio pago
- **Okta**: 
  - ❌ Servicio pago

### Implementación de Seguridad

**En API Gateway:**
\`\`\`java
@Configuration
public class SecurityConfig {
    
    @Bean
    public SecurityWebFilterChain securityWebFilterChain(ServerHttpSecurity http) {
        return http
            .oauth2ResourceServer()
            .jwt()
            .and()
            .authorizeExchange()
            .pathMatchers("/api/clientes/**").hasAnyRole("CLIENTE", "OPERADOR", "ADMIN")
            .pathMatchers("/api/depositos/**").hasAnyRole("OPERADOR", "ADMIN")
            // ...
            .build();
    }
}
\`\`\`

**En cada microservicio:**
\`\`\`java
@Configuration
@EnableWebSecurity
public class SecurityConfig {
    
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) {
        return http
            .oauth2ResourceServer()
            .jwt()
            .jwtAuthenticationConverter(jwtAuthenticationConverter())
            .and()
            .authorizeHttpRequests()
            .requestMatchers("/api/clientes/**").hasAnyRole("CLIENTE", "OPERADOR", "ADMIN")
            // ...
            .build();
    }
}
\`\`\`

---

## Integraciones Externas

### 1. Google Maps Distance Matrix API

**Decisión:** Integración con Google Maps para cálculo de distancias reales

**Justificación:**
- **Requerimiento obligatorio**: Especificado en el enunciado
- **Precisión**: Distancias reales por carretera vs línea recta
- **Confiabilidad**: API madura y bien documentada
- **Información adicional**: Tiempo estimado de viaje, rutas alternativas

**Implementación:**
\`\`\`java
@Service
public class GoogleMapsService {
    
    public Double calcularDistancia(Double latOrigen, Double lonOrigen, 
                                    Double latDestino, Double lonDestino) {
        // Llamada a Google Maps Distance Matrix API
        // Parsing de respuesta JSON
        // Extracción de distancia en kilómetros
    }
}
\`\`\`

**Fallback:**
- Si la API falla, se usa cálculo de Haversine (línea recta)
- Logging de errores para monitoreo
- Configuración de timeouts y reintentos

**Costos:**
- Google Maps ofrece $200 USD de crédito mensual gratuito
- Suficiente para desarrollo y testing
- Para producción, considerar optimizaciones (caché, batch requests)

### 2. Alternativas Consideradas

**OpenStreetMap (OSRM):**
- ✅ Gratuito y open source
- ❌ Requiere self-hosting o uso de servidores públicos (menos confiables)
- ❌ Menor precisión en algunas regiones

**Mapbox:**
- ✅ API similar a Google Maps
- ❌ Menor cuota gratuita
- ❌ Menos documentación en español

**Decisión final:** Google Maps por balance entre costo, precisión y facilidad de uso

---

## Infraestructura y Despliegue

### Decisión: Docker + Docker Compose

**Justificación:**
- **Requerimiento del TPI**: Despliegue con docker-compose
- **Portabilidad**: Funciona igual en cualquier entorno
- **Aislamiento**: Cada servicio en su propio contenedor
- **Reproducibilidad**: Mismo entorno en desarrollo y producción
- **Facilidad**: Un solo comando para levantar todo el sistema

### Arquitectura de Contenedores

\`\`\`
docker-compose.yml
├── postgres (Base de datos compartida)
├── keycloak (Identity Provider)
├── api-gateway (Puerto 8080)
├── clientes-service (Puerto 8081)
├── depositos-service (Puerto 8082)
├── camiones-service (Puerto 8083)
├── tarifas-service (Puerto 8084)
├── solicitudes-service (Puerto 8085)
└── rutas-service (Porto 8086)
\`\`\`

### Decisiones de Configuración

#### 1. Base de Datos Compartida vs Separada

**Decisión:** Una instancia de PostgreSQL con múltiples bases de datos

**Justificación:**
- **Simplicidad**: Más fácil de gestionar en desarrollo
- **Recursos**: Menor consumo de memoria
- **Costos**: Más económico en producción
- **Independencia lógica**: Cada servicio tiene su propia BD

**Para producción:**
- Considerar instancias separadas para servicios críticos
- Usar servicios gestionados (AWS RDS, Google Cloud SQL)

#### 2. Healthchecks

**Decisión:** Implementar healthchecks en todos los servicios

\`\`\`yaml
healthcheck:
  test: ["CMD", "curl", "-f", "http://localhost:8081/actuator/health"]
  interval: 30s
  timeout: 10s
  retries: 3
  start_period: 40s
\`\`\`

**Justificación:**
- **Resiliencia**: Docker reinicia contenedores no saludables
- **Dependencias**: Servicios esperan a que dependencias estén listas
- **Monitoreo**: Facilita detección de problemas

#### 3. Redes Docker

**Decisión:** Red interna para comunicación entre servicios

\`\`\`yaml
networks:
  logistica-network:
    driver: bridge
\`\`\`

**Justificación:**
- **Aislamiento**: Servicios no expuestos directamente al exterior
- **Resolución de nombres**: Servicios se comunican por nombre de contenedor
- **Seguridad**: Solo API Gateway expuesto públicamente

#### 4. Volúmenes

**Decisión:** Volúmenes para persistencia de datos

\`\`\`yaml
volumes:
  postgres-data:
  keycloak-data:
\`\`\`

**Justificación:**
- **Persistencia**: Datos sobreviven a reinicios de contenedores
- **Performance**: Mejor rendimiento que bind mounts
- **Backup**: Fácil de respaldar y restaurar

### Variables de Entorno

**Decisión:** Archivo `.env` para configuración

**Justificación:**
- **Seguridad**: Credenciales fuera del código
- **Flexibilidad**: Fácil cambiar configuración por entorno
- **Buenas prácticas**: Estándar de la industria

---

## Alternativas Consideradas

### 1. Monolito vs Microservicios

**Alternativa:** Aplicación monolítica

**Ventajas del monolito:**
- Más simple de desarrollar inicialmente
- Menos overhead de comunicación
- Transacciones más simples
- Deployment más sencillo

**Por qué elegimos microservicios:**
- ✅ **Requerimiento del TPI**: Arquitectura de microservicios obligatoria
- ✅ **Escalabilidad**: Podemos escalar servicios independientemente
- ✅ **Mantenibilidad**: Equipos pueden trabajar en paralelo
- ✅ **Resiliencia**: Fallo de un servicio no tumba todo el sistema
- ✅ **Aprendizaje**: Experiencia con arquitecturas modernas

### 2. REST vs GraphQL

**Alternativa:** GraphQL para APIs

**Ventajas de GraphQL:**
- Cliente solicita exactamente lo que necesita
- Menos endpoints
- Introspección automática

**Por qué elegimos REST:**
- ✅ **Simplicidad**: Más fácil de implementar y entender
- ✅ **Estándar**: Ampliamente adoptado
- ✅ **Herramientas**: Mejor soporte en Spring Boot
- ✅ **Documentación**: Swagger/OpenAPI bien establecido
- ✅ **Caché**: Más fácil de cachear con HTTP

### 3. SQL vs NoSQL

**Alternativa:** MongoDB u otra base NoSQL

**Ventajas de NoSQL:**
- Esquema flexible
- Escalabilidad horizontal
- Mejor para datos no estructurados

**Por qué elegimos SQL (PostgreSQL):**
- ✅ **Modelo relacional**: Datos altamente relacionados (rutas, tramos, solicitudes)
- ✅ **ACID**: Necesitamos transacciones consistentes
- ✅ **Integridad**: Constraints y validaciones a nivel de BD
- ✅ **Queries complejas**: JOINs y agregaciones frecuentes
- ✅ **Experiencia del equipo**: Mayor familiaridad con SQL

### 4. Comunicación Síncrona vs Asíncrona

**Alternativa:** Mensajería asíncrona (RabbitMQ, Kafka)

**Ventajas de mensajería:**
- Desacoplamiento temporal
- Mayor resiliencia
- Mejor para alta concurrencia

**Por qué elegimos REST síncrono:**
- ✅ **Simplicidad**: Más fácil de implementar y debuggear
- ✅ **Alcance del proyecto**: No requiere alta concurrencia
- ✅ **Consistencia inmediata**: Respuestas en tiempo real
- ✅ **Menos infraestructura**: No requiere message broker

**Nota:** Para futuras mejoras, consideraríamos mensajería para:
- Notificaciones de cambio de estado
- Eventos de auditoría
- Procesamiento batch

---

## Conclusiones

### Fortalezas del Diseño

1. **Arquitectura clara y bien definida**
   - Separación de responsabilidades por microservicio
   - Cada servicio con propósito único

2. **Escalabilidad**
   - Servicios independientes escalables por separado
   - Bases de datos separadas por servicio

3. **Mantenibilidad**
   - Código organizado en capas
   - Patrones consistentes en todos los servicios
   - Documentación completa

4. **Seguridad robusta**
   - Autenticación centralizada con Keycloak
   - Autorización por roles
   - Tokens JWT stateless

5. **Integración con servicios externos**
   - Google Maps para cálculos precisos
   - Fallback a Haversine para resiliencia

6. **Despliegue simplificado**
   - Docker Compose para todo el stack
   - Un comando para levantar el sistema completo

### Áreas de Mejora Futuras

1. **Mensajería asíncrona**
   - Implementar RabbitMQ o Kafka para eventos
   - Desacoplar servicios temporalmente

2. **Caché distribuido**
   - Redis para cachear consultas frecuentes
   - Mejorar performance de APIs

3. **Service Discovery**
   - Eureka o Consul para registro dinámico
   - Facilitar escalamiento horizontal

4. **Circuit Breakers avanzados**
   - Resilience4j para patrones de resiliencia
   - Mejor manejo de fallos en cascada

5. **Observabilidad**
   - ELK Stack para logs centralizados
   - Prometheus + Grafana para métricas
   - Jaeger para distributed tracing

6. **Testing**
   - Tests de integración entre servicios
   - Tests de carga y performance
   - Tests de contrato (Pact)

7. **CI/CD**
   - Pipeline automatizado
   - Tests automáticos en cada commit
   - Deployment automático a staging

### Cumplimiento de Requerimientos

✅ **Requerimientos funcionales**: Todos implementados  
✅ **Arquitectura de microservicios**: 6 servicios + Gateway  
✅ **Seguridad con Keycloak**: Implementada  
✅ **Integración con API externa**: Google Maps  
✅ **Documentación con Swagger**: En todos los servicios  
✅ **Despliegue con Docker**: docker-compose completo  
✅ **Bases de datos separadas**: Una por servicio  
✅ **Roles y permisos**: Cliente, Operador, Transportista, Admin  

---

## Guía para el Video de Presentación

### Estructura Sugerida (10-15 minutos)

#### 1. Introducción (1 min)
- Presentación del equipo (4 integrantes)
- Objetivo del proyecto
- Alcance del sistema

#### 2. Arquitectura de Microservicios (3 min)
- Diagrama de contenedores
- Explicar cada microservicio y su responsabilidad
- Justificar la división elegida
- Mostrar comunicación entre servicios

#### 3. Modelo de Datos (3 min)
- Mostrar DER completo
- Explicar entidades principales
- Justificar decisión de bases de datos separadas
- Explicar relaciones y referencias externas

#### 4. Decisiones Técnicas (3 min)
- Stack tecnológico (Java, Spring Boot, PostgreSQL)
- Patrones implementados (Repository, DTO, Service Layer)
- Seguridad con Keycloak
- Integración con Google Maps

#### 5. Infraestructura (2 min)
- Docker y Docker Compose
- Cómo levantar el sistema
- Healthchecks y resiliencia

#### 6. Endpoints y Documentación (2 min)
- Mostrar Swagger UI
- Ejemplos de endpoints por rol
- Demostración rápida de una API

#### 7. Conclusiones (1 min)
- Fortalezas del diseño
- Lecciones aprendidas
- Próximos pasos

### Tips para el Video

1. **Preparar diapositivas visuales**
   - Diagramas claros y legibles
   - Código solo cuando sea necesario
   - Capturas de Swagger UI

2. **Dividir responsabilidades**
   - Integrante 1: Introducción + Arquitectura
   - Integrante 2: Modelo de Datos
   - Integrante 3: Decisiones Técnicas + Seguridad
   - Integrante 4: Infraestructura + Demo + Conclusiones

3. **Practicar antes de grabar**
   - Ensayar transiciones entre integrantes
   - Verificar tiempos
   - Probar audio y video

4. **Mostrar, no solo contar**
   - Diagramas en lugar de texto
   - Demos en vivo (si es posible)
   - Ejemplos concretos

5. **Anticipar preguntas**
   - ¿Por qué microservicios y no monolito?
   - ¿Por qué PostgreSQL y no MongoDB?
   - ¿Por qué REST y no GraphQL?
   - ¿Cómo manejan transacciones distribuidas?

---

## Recursos Adicionales

### Documentos del Proyecto

- `01-DER-modelo-datos.md`: DER detallado
- `02-arquitectura-microservicios.md`: Arquitectura completa
- `03-definicion-endpoints.md`: Todos los endpoints documentados
- `08-keycloak-configuracion.md`: Guía de seguridad
- `09-google-maps-integracion.md`: Integración con API externa

### Diagramas

Crear los siguientes diagramas para el video:

1. **Diagrama de Contenedores (C4)**
   - Todos los microservicios
   - Bases de datos
   - Keycloak
   - API Gateway
   - Cliente externo

2. **Diagrama de Secuencia**
   - Flujo de creación de solicitud
   - Flujo de cálculo de ruta
   - Flujo de autenticación

3. **DER Completo**
   - Todas las entidades
   - Relaciones
   - Cardinalidades

4. **Diagrama de Despliegue**
   - Contenedores Docker
   - Redes
   - Volúmenes
   - Puertos

### Herramientas Recomendadas

- **Diagramas**: draw.io, Lucidchart, PlantUML
- **Video**: OBS Studio, Zoom, Google Meet
- **Edición**: DaVinci Resolve, iMovie, Camtasia
- **Presentaciones**: Google Slides, PowerPoint, Canva

---

**Fecha de creación:** Enero 2025  
**Versión:** 1.0  
**Equipo:** 4 integrantes  
**Proyecto:** TPI Backend de Aplicaciones 2025
