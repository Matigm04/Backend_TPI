# Preguntas Frecuentes para la Defensa del TPI

## Índice
1. [Arquitectura](#arquitectura)
2. [Modelo de Datos](#modelo-de-datos)
3. [Decisiones Técnicas](#decisiones-técnicas)
4. [Seguridad](#seguridad)
5. [Integraciones](#integraciones)
6. [Despliegue](#despliegue)
7. [Escalabilidad y Performance](#escalabilidad-y-performance)

---

## Arquitectura

### ¿Por qué eligieron microservicios en lugar de un monolito?

**Respuesta:**

Elegimos microservicios por varias razones:

1. **Requerimiento del TPI**: El enunciado especifica arquitectura de microservicios
2. **Separación de responsabilidades**: Cada servicio tiene un dominio claro (clientes, depósitos, rutas, etc.)
3. **Escalabilidad independiente**: Podemos escalar el servicio de rutas (más demandante) sin escalar todo el sistema
4. **Desarrollo paralelo**: Nuestro equipo de 4 personas pudo trabajar en servicios diferentes simultáneamente
5. **Resiliencia**: Si falla el servicio de tarifas, el resto del sistema sigue funcionando

**Reconocemos que para un proyecto pequeño, un monolito sería más simple**, pero los microservicios nos dan experiencia con arquitecturas modernas y cumplen con los objetivos de aprendizaje del curso.

---

### ¿Por qué 6 microservicios y no más o menos?

**Respuesta:**

Dividimos el sistema en 6 microservicios basándonos en **Bounded Contexts** de Domain-Driven Design:

1. **clientes-service**: Gestión de clientes (entidad independiente)
2. **depositos-service**: Gestión de ubicaciones de almacenamiento
3. **camiones-service**: Gestión de flota y transportistas
4. **tarifas-service**: Configuración de costos del sistema
5. **solicitudes-service**: Orquestación de solicitudes y contenedores
6. **rutas-service**: Cálculo y gestión de rutas con tramos

**Consideramos alternativas:**
- **Menos servicios**: Agrupar clientes + solicitudes → Rechazado porque mezcla responsabilidades
- **Más servicios**: Separar contenedores de solicitudes → Rechazado porque están muy acoplados

Cada servicio tiene:
- ✅ Responsabilidad única y clara
- ✅ Base de datos propia
- ✅ Puede desplegarse independientemente
- ✅ Tamaño manejable (no demasiado grande ni pequeño)

---

### ¿Cómo se comunican los microservicios entre sí?

**Respuesta:**

Usamos **comunicación REST síncrona** con `RestTemplate`:

**Ejemplo:** solicitudes-service consulta a clientes-service

\`\`\`java
@Service
public class ClienteClient {
    
    @Value("${clientes.service.url}")
    private String clientesServiceUrl;
    
    private final RestTemplate restTemplate;
    
    public ClienteDTO obtenerCliente(Long clienteId) {
        String url = clientesServiceUrl + "/api/clientes/" + clienteId;
        return restTemplate.getForObject(url, ClienteDTO.class);
    }
}
\`\`\`

**Ventajas:**
- Simple de implementar y debuggear
- Respuestas inmediatas
- Adecuado para el alcance del proyecto

**Desventajas reconocidas:**
- Acoplamiento temporal (si un servicio está caído, falla la llamada)
- No ideal para alta concurrencia

**Mitigación:**
- Implementamos timeouts y circuit breakers
- Manejo de errores con fallbacks
- Logging completo para troubleshooting

**Para futuras mejoras:** Consideraríamos mensajería asíncrona (RabbitMQ/Kafka) para eventos no críticos.

---

### ¿Qué pasa si un microservicio falla?

**Respuesta:**

Implementamos varios mecanismos de resiliencia:

1. **Circuit Breakers**: Si un servicio falla repetidamente, dejamos de llamarlo temporalmente
2. **Timeouts**: Configuramos tiempos máximos de espera (5 segundos)
3. **Fallbacks**: Respuestas por defecto en caso de error
4. **Healthchecks**: Docker reinicia contenedores no saludables
5. **Logging**: Registramos todos los errores para análisis

**Ejemplo de escenario:**

Si **clientes-service** está caído:
- ✅ **depositos-service** sigue funcionando normalmente
- ✅ **camiones-service** sigue funcionando normalmente
- ❌ **solicitudes-service** no puede crear nuevas solicitudes (requiere validar cliente)
- ✅ **solicitudes-service** puede consultar solicitudes existentes

**Mejora futura:** Implementar caché de datos críticos para reducir dependencias.

---

## Modelo de Datos

### ¿Por qué cada microservicio tiene su propia base de datos?

**Respuesta:**

Seguimos el principio de **Database per Service** de microservicios:

**Ventajas:**
1. **Autonomía**: Cada servicio puede evolucionar su esquema independientemente
2. **Escalabilidad**: Podemos escalar bases de datos específicas según la carga
3. **Resiliencia**: Si una BD falla, no afecta a todos los servicios
4. **Tecnología agnóstica**: En el futuro podríamos usar diferentes DBMS por servicio

**Implementación:**
- Una instancia de PostgreSQL con **múltiples bases de datos**:
  - `clientes_db`
  - `depositos_db`
  - `camiones_db`
  - `tarifas_db`
  - `solicitudes_db`
  - `rutas_db`

**Desventajas reconocidas:**
- No podemos hacer JOINs entre servicios
- Transacciones distribuidas son más complejas
- Posible duplicación de datos

**Mitigación:**
- Usamos referencias por ID entre servicios
- Implementamos consistencia eventual donde es aceptable
- Documentamos claramente las dependencias

---

### ¿Cómo manejan las relaciones entre entidades de diferentes servicios?

**Respuesta:**

Usamos **referencias externas por ID** sin Foreign Keys físicas:

**Ejemplo:** Solicitud referencia a Cliente

\`\`\`java
@Entity
public class Solicitud {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    // Referencia externa a clientes-service (NO FK física)
    private Long clienteId;
    
    // ... otros campos
}
\`\`\`

**Proceso:**
1. Cliente crea solicitud con `clienteId`
2. solicitudes-service **valida** que el cliente existe llamando a clientes-service
3. Si existe, guarda la solicitud con el `clienteId`
4. Para mostrar datos completos, hace JOIN a nivel de aplicación

**Ventajas:**
- Mantiene independencia de bases de datos
- Permite evolución independiente
- Evita acoplamiento a nivel de BD

**Desventajas:**
- Integridad referencial manejada por aplicación
- Más llamadas HTTP entre servicios

---

### ¿Por qué usaron Soft Delete en lugar de DELETE físico?

**Respuesta:**

Implementamos **Soft Delete** con campo `activo` por varias razones:

**Ventajas:**
1. **Auditoría**: Mantenemos historial completo de datos
2. **Recuperación**: Podemos "deshacer" eliminaciones
3. **Integridad**: Evitamos romper referencias en solicitudes antiguas
4. **Regulaciones**: Cumplimiento de retención de datos

**Implementación:**

\`\`\`java
@Service
public class ClienteService {
    
    public void eliminarCliente(Long id) {
        Cliente cliente = obtenerClientePorId(id);
        cliente.setActivo(false); // Soft delete
        clienteRepository.save(cliente);
    }
    
    public List<ClienteResponseDTO> listarClientes() {
        return clienteRepository.findByActivoTrue() // Solo activos
            .stream()
            .map(this::convertirADTO)
            .collect(Collectors.toList());
    }
}
\`\`\`

**Consideraciones:**
- Queries siempre filtran por `activo = true`
- Índices incluyen campo `activo` para performance
- Proceso de purga periódica para datos muy antiguos (futuro)

---

### ¿Por qué separaron Contenedor y Solicitud en entidades diferentes?

**Respuesta:**

Separamos **Contenedor** (objeto físico) y **Solicitud** (proceso de negocio) por:

**Razones de diseño:**
1. **Separación de conceptos**: Un contenedor es un objeto físico, una solicitud es un proceso
2. **Reutilización**: Un contenedor podría tener múltiples solicitudes en el futuro
3. **Trazabilidad**: Historial completo del contenedor independiente de solicitudes
4. **Flexibilidad**: Podemos agregar atributos a cada uno sin afectar al otro

**Modelo:**

\`\`\`
Solicitud (1) ←→ (1) Contenedor
- id                  - id
- numero              - identificacion
- clienteId           - peso_kg
- estado              - volumen_m3
- costoEstimado       - direccion_origen
- costoFinal          - latitud_origen
- tiempoEstimado      - longitud_origen
- tiempoReal          - direccion_destino
                      - latitud_destino
                      - longitud_destino
\`\`\`

**Alternativa considerada:** Contenedor como parte de Solicitud (embedded)
- ❌ Rechazada: Menos flexible, dificulta trazabilidad del contenedor

---

## Decisiones Técnicas

### ¿Por qué eligieron PostgreSQL y no MongoDB?

**Respuesta:**

Elegimos **PostgreSQL** (SQL) sobre **MongoDB** (NoSQL) por:

**Razones técnicas:**
1. **Modelo relacional**: Nuestros datos están altamente relacionados (rutas → tramos → camiones)
2. **ACID**: Necesitamos transacciones consistentes para operaciones críticas
3. **Integridad**: Constraints y validaciones a nivel de BD (UNIQUE, NOT NULL, CHECK)
4. **Queries complejas**: Frecuentes JOINs y agregaciones
5. **Experiencia del equipo**: Mayor familiaridad con SQL

**Ejemplo de query compleja:**

\`\`\`sql
-- Obtener todas las rutas con sus tramos y costos
SELECT r.id, r.solicitud_id, 
       COUNT(t.id) as cantidad_tramos,
       SUM(t.distancia_km) as distancia_total,
       SUM(t.costo_real) as costo_total
FROM ruta r
JOIN tramo t ON t.ruta_id = r.id
WHERE r.solicitud_id = ?
GROUP BY r.id;
\`\`\`

**Cuándo consideraríamos NoSQL:**
- Datos no estructurados (logs, eventos)
- Necesidad de escalamiento horizontal masivo
- Esquema muy variable

**Para este proyecto:** SQL es la mejor opción.

---

### ¿Por qué usaron RestTemplate y no WebClient?

**Respuesta:**

Usamos **RestTemplate** por:

**Ventajas:**
1. **Simplicidad**: API síncrona más fácil de entender
2. **Debugging**: Más sencillo rastrear llamadas
3. **Suficiente para el alcance**: No necesitamos programación reactiva
4. **Experiencia del equipo**: Mayor familiaridad

**Reconocemos que WebClient es más moderno:**
- ✅ No bloqueante (reactive)
- ✅ Mejor performance en alta concurrencia
- ✅ Recomendado por Spring

**Para futuras mejoras:** Migrar a WebClient si necesitamos:
- Alta concurrencia
- Programación reactiva
- Mejor performance

**Ejemplo de uso actual:**

\`\`\`java
@Service
public class ClienteClient {
    
    private final RestTemplate restTemplate;
    
    public ClienteDTO obtenerCliente(Long clienteId) {
        try {
            String url = clientesServiceUrl + "/api/clientes/" + clienteId;
            return restTemplate.getForObject(url, ClienteDTO.class);
        } catch (RestClientException e) {
            log.error("Error al obtener cliente: {}", clienteId, e);
            throw new ClienteNotFoundException("Cliente no encontrado");
        }
    }
}
\`\`\`

---

### ¿Por qué usaron DTOs en lugar de exponer las entidades directamente?

**Respuesta:**

Usamos **DTOs (Data Transfer Objects)** por:

**Ventajas:**
1. **Seguridad**: Ocultamos detalles internos del modelo
2. **Flexibilidad**: API independiente del modelo de dominio
3. **Versionado**: Podemos cambiar el modelo sin romper la API
4. **Optimización**: Evitamos over-fetching y under-fetching
5. **Validación**: Validaciones específicas para entrada vs salida

**Ejemplo:**

\`\`\`java
// Entidad (modelo interno)
@Entity
public class Cliente {
    private Long id;
    private String nombre;
    private String apellido;
    private String email;
    private String telefono;
    private String direccion;
    private Boolean activo;
    private LocalDateTime fechaCreacion;
    private LocalDateTime fechaActualizacion;
}

// Request DTO (entrada)
public class ClienteRequestDTO {
    @NotBlank
    private String nombre;
    
    @NotBlank
    private String apellido;
    
    @Email
    private String email;
    
    @NotBlank
    private String telefono;
    
    private String direccion;
    // NO incluye id, activo, fechas (generados por el sistema)
}

// Response DTO (salida)
public class ClienteResponseDTO {
    private Long id;
    private String nombre;
    private String apellido;
    private String email;
    private String telefono;
    private String direccion;
    private LocalDateTime fechaCreacion;
    // NO incluye activo (detalle interno)
}
\`\`\`

**Alternativa considerada:** Exponer entidades directamente
- ❌ Rechazada: Expone detalles internos, dificulta evolución

---

## Seguridad

### ¿Por qué eligieron Keycloak y no implementar autenticación propia?

**Respuesta:**

Elegimos **Keycloak** por:

**Ventajas:**
1. **Requerimiento del TPI**: Especificado en el enunciado
2. **Estándar OAuth2/OIDC**: Protocolo ampliamente adoptado
3. **Gestión centralizada**: Un solo lugar para usuarios y roles
4. **UI administrativa**: Interfaz web para gestión
5. **Seguridad probada**: Implementación madura y auditada
6. **Federación**: Permite integración con LDAP, Active Directory, etc.

**Alternativas consideradas:**

**Implementación propia con Spring Security:**
- ❌ Requiere implementar gestión de usuarios manualmente
- ❌ Mayor riesgo de vulnerabilidades
- ❌ Más tiempo de desarrollo

**Auth0 u Okta:**
- ❌ Servicios pagos
- ❌ Dependencia de terceros

**Keycloak es la mejor opción:** Open source, completo y cumple requerimientos.

---

### ¿Cómo funciona el flujo de autenticación?

**Respuesta:**

**Flujo completo:**

\`\`\`
1. Usuario → POST /auth/login (Keycloak)
   Body: { username, password }

2. Keycloak valida credenciales

3. Keycloak ← JWT Token
   {
     "access_token": "eyJhbGc...",
     "expires_in": 300,
     "refresh_token": "eyJhbGc...",
     "token_type": "Bearer"
   }

4. Usuario → GET /api/clientes (API Gateway)
   Header: Authorization: Bearer eyJhbGc...

5. API Gateway valida JWT con Keycloak

6. API Gateway → GET /api/clientes (clientes-service)
   Header: Authorization: Bearer eyJhbGc...

7. clientes-service valida JWT y extrae roles

8. clientes-service verifica permisos

9. clientes-service ← Respuesta con datos

10. Usuario ← Respuesta con datos
\`\`\`

**Validación de JWT:**

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
                .requestMatchers("/api/clientes/**")
                    .hasAnyRole("CLIENTE", "OPERADOR", "ADMIN")
                .anyRequest().authenticated()
            .and()
            .build();
    }
}
\`\`\`

---

### ¿Qué roles implementaron y qué puede hacer cada uno?

**Respuesta:**

Implementamos **4 roles** según el enunciado:

#### 1. CLIENTE
**Permisos:**
- ✅ Crear solicitudes de transporte
- ✅ Consultar sus propias solicitudes
- ✅ Ver estado de sus contenedores
- ❌ No puede ver solicitudes de otros clientes
- ❌ No puede gestionar depósitos, camiones o tarifas

#### 2. OPERADOR
**Permisos:**
- ✅ Todas las operaciones de CLIENTE
- ✅ Ver todas las solicitudes (de todos los clientes)
- ✅ Gestionar depósitos (CRUD)
- ✅ Gestionar camiones (CRUD)
- ✅ Gestionar tarifas (CRUD)
- ✅ Calcular rutas tentativas
- ✅ Asignar rutas a solicitudes
- ✅ Asignar camiones a tramos
- ❌ No puede iniciar/finalizar tramos (solo transportistas)

#### 3. TRANSPORTISTA
**Permisos:**
- ✅ Ver tramos asignados a su camión
- ✅ Iniciar tramo (registrar inicio de viaje)
- ✅ Finalizar tramo (registrar fin de viaje)
- ❌ No puede ver tramos de otros transportistas
- ❌ No puede gestionar depósitos, camiones o tarifas

#### 4. ADMIN
**Permisos:**
- ✅ Acceso completo al sistema
- ✅ Gestión de usuarios y roles en Keycloak
- ✅ Todas las operaciones de todos los servicios

**Implementación en endpoints:**

\`\`\`java
@RestController
@RequestMapping("/api/solicitudes")
public class SolicitudController {
    
    // Solo CLIENTE, OPERADOR, ADMIN
    @PostMapping
    @PreAuthorize("hasAnyRole('CLIENTE', 'OPERADOR', 'ADMIN')")
    public ResponseEntity<SolicitudResponseDTO> crearSolicitud(
        @RequestBody SolicitudRequestDTO request) {
        // ...
    }
    
    // Solo OPERADOR, ADMIN
    @GetMapping
    @PreAuthorize("hasAnyRole('OPERADOR', 'ADMIN')")
    public ResponseEntity<List<SolicitudResponseDTO>> listarTodasSolicitudes() {
        // ...
    }
}
\`\`\`

---

## Integraciones

### ¿Por qué eligieron Google Maps y no otra API de mapas?

**Respuesta:**

Elegimos **Google Maps Distance Matrix API** por:

**Ventajas:**
1. **Requerimiento del TPI**: Enunciado sugiere Google Maps
2. **Precisión**: Distancias reales por carretera (no línea recta)
3. **Confiabilidad**: API madura y bien documentada
4. **Información adicional**: Tiempo estimado, rutas alternativas
5. **Cuota gratuita**: $200 USD/mes suficiente para desarrollo

**Alternativas consideradas:**

**OpenStreetMap (OSRM):**
- ✅ Gratuito y open source
- ❌ Requiere self-hosting o servidores públicos menos confiables
- ❌ Menor precisión en algunas regiones

**Mapbox:**
- ✅ API similar a Google Maps
- ❌ Menor cuota gratuita
- ❌ Menos documentación en español

**Decisión:** Google Maps por balance entre costo, precisión y facilidad de uso.

---

### ¿Qué pasa si la API de Google Maps falla?

**Respuesta:**

Implementamos un **sistema de fallback** con Haversine:

**Flujo:**

\`\`\`java
@Service
public class DistanciaService {
    
    private final GoogleMapsService googleMapsService;
    
    public Double calcularDistancia(Double latOrigen, Double lonOrigen,
                                    Double latDestino, Double lonDestino) {
        try {
            // Intento 1: Google Maps API
            return googleMapsService.calcularDistancia(
                latOrigen, lonOrigen, latDestino, lonDestino
            );
        } catch (Exception e) {
            log.warn("Google Maps API falló, usando Haversine", e);
            // Fallback: Fórmula de Haversine (línea recta)
            return calcularDistanciaHaversine(
                latOrigen, lonOrigen, latDestino, lonDestino
            );
        }
    }
    
    private Double calcularDistanciaHaversine(...) {
        // Cálculo de distancia en línea recta
        // Menos preciso pero siempre disponible
    }
}
\`\`\`

**Ventajas del fallback:**
- ✅ Sistema nunca se cae por fallo de API externa
- ✅ Distancia aproximada mejor que ninguna distancia
- ✅ Logging para detectar problemas

**Desventajas:**
- ❌ Haversine calcula línea recta (no rutas reales)
- ❌ Puede subestimar distancias en zonas montañosas

**Mitigación:**
- Indicamos en logs cuando se usa fallback
- Monitoreo de tasa de fallos de Google Maps
- Caché de distancias calculadas previamente

---

### ¿Cómo manejan los costos de Google Maps API?

**Respuesta:**

**Cuota gratuita:**
- Google Maps ofrece **$200 USD/mes** de crédito gratuito
- Distance Matrix API: **$5 por 1000 requests**
- Cuota gratuita = **40,000 requests/mes**

**Para desarrollo y testing:** Más que suficiente

**Optimizaciones implementadas:**

1. **Caché de distancias:**
\`\`\`java
@Service
public class GoogleMapsService {
    
    private final Map<String, Double> cache = new ConcurrentHashMap<>();
    
    public Double calcularDistancia(...) {
        String key = generarClave(latOrigen, lonOrigen, latDestino, lonDestino);
        
        // Buscar en caché primero
        if (cache.containsKey(key)) {
            return cache.get(key);
        }
        
        // Llamar a API solo si no está en caché
        Double distancia = llamarGoogleMapsAPI(...);
        cache.put(key, distancia);
        return distancia;
    }
}
\`\`\`

2. **Redondeo de coordenadas:**
   - Redondeamos a 4 decimales (~11 metros de precisión)
   - Aumenta tasa de hits en caché

3. **Batch requests:**
   - Agrupamos múltiples consultas en una sola request
   - Reduce costos

**Para producción:**
- Considerar Redis para caché distribuido
- Monitoreo de uso mensual
- Alertas si nos acercamos al límite

---

## Despliegue

### ¿Por qué eligieron Docker y no desplegar directamente?

**Respuesta:**

Elegimos **Docker + Docker Compose** por:

**Ventajas:**
1. **Requerimiento del TPI**: Despliegue con docker-compose obligatorio
2. **Portabilidad**: Funciona igual en cualquier entorno (Windows, Mac, Linux)
3. **Aislamiento**: Cada servicio en su propio contenedor
4. **Reproducibilidad**: Mismo entorno en desarrollo y producción
5. **Facilidad**: Un solo comando para levantar todo el sistema
6. **Gestión de dependencias**: Docker maneja orden de inicio

**Comando único:**
\`\`\`bash
docker-compose up -d
\`\`\`

Levanta:
- PostgreSQL
- Keycloak
- 6 microservicios
- API Gateway

**Alternativas consideradas:**

**Despliegue manual:**
- ❌ Requiere instalar Java, PostgreSQL, Keycloak en cada máquina
- ❌ Configuración diferente por entorno
- ❌ Difícil de replicar

**Kubernetes:**
- ❌ Demasiado complejo para el alcance del proyecto
- ✅ Considerado para futuras mejoras en producción

---

### ¿Cómo manejan las variables de entorno?

**Respuesta:**

Usamos **archivo `.env`** para configuración:

**Estructura:**

\`\`\`env
# Base de datos
POSTGRES_USER=logistica_user
POSTGRES_PASSWORD=logistica_pass
POSTGRES_DB=logistica_db

# Keycloak
KEYCLOAK_ADMIN=admin
KEYCLOAK_ADMIN_PASSWORD=admin123

# Google Maps
GOOGLE_MAPS_API_KEY=AIzaSy...

# Puertos
API_GATEWAY_PORT=8080
CLIENTES_SERVICE_PORT=8081
# ...
\`\`\`

**Ventajas:**
1. **Seguridad**: Credenciales fuera del código
2. **Flexibilidad**: Fácil cambiar configuración por entorno
3. **Buenas prácticas**: Estándar de la industria
4. **Git ignore**: `.env` no se sube al repositorio

**Uso en docker-compose.yml:**

\`\`\`yaml
services:
  clientes-service:
    environment:
      - SPRING_DATASOURCE_URL=jdbc:postgresql://postgres:5432/${POSTGRES_DB}
      - SPRING_DATASOURCE_USERNAME=${POSTGRES_USER}
      - SPRING_DATASOURCE_PASSWORD=${POSTGRES_PASSWORD}
\`\`\`

**Para producción:**
- Usar secretos de Docker Swarm o Kubernetes
- Servicios de gestión de secretos (AWS Secrets Manager, HashiCorp Vault)

---

### ¿Cómo garantizan que los servicios se inicien en el orden correcto?

**Respuesta:**

Usamos **depends_on + healthchecks** en docker-compose:

**Configuración:**

\`\`\`yaml
services:
  postgres:
    image: postgres:15
    healthcheck:
      test: ["CMD-SHELL", "pg_isready -U ${POSTGRES_USER}"]
      interval: 10s
      timeout: 5s
      retries: 5
  
  clientes-service:
    depends_on:
      postgres:
        condition: service_healthy
    healthcheck:
      test: ["CMD", "curl", "-f", "http://localhost:8081/actuator/health"]
      interval: 30s
      timeout: 10s
      retries: 3
      start_period: 40s
  
  api-gateway:
    depends_on:
      clientes-service:
        condition: service_healthy
      depositos-service:
        condition: service_healthy
      # ... otros servicios
\`\`\`

**Orden de inicio:**

\`\`\`
1. postgres (espera a estar healthy)
2. keycloak (espera a postgres)
3. Microservicios (esperan a postgres)
4. API Gateway (espera a todos los microservicios)
\`\`\`

**Ventajas:**
- ✅ Evita errores de conexión al inicio
- ✅ Reinicio automático si un servicio falla
- ✅ Sistema robusto y confiable

---

## Escalabilidad y Performance

### ¿Cómo escalaría el sistema si crece la demanda?

**Respuesta:**

**Escalamiento horizontal por servicio:**

1. **Identificar cuellos de botella:**
   - Monitorear métricas (CPU, memoria, requests/segundo)
   - Identificar servicios más demandados

2. **Escalar servicios específicos:**

\`\`\`bash
# Escalar rutas-service a 3 instancias
docker-compose up -d --scale rutas-service=3
\`\`\`

3. **Load Balancer:**
   - Agregar NGINX o HAProxy delante de servicios
   - Distribuir carga entre instancias

**Arquitectura escalada:**

\`\`\`
                    ┌─────────────┐
                    │ Load Balancer│
                    └──────┬──────┘
                           │
        ┌──────────────────┼──────────────────┐
        │                  │                  │
   ┌────▼────┐       ┌────▼────┐       ┌────▼────┐
   │ Rutas-1 │       │ Rutas-2 │       │ Rutas-3 │
   └─────────┘       └─────────┘       └─────────┘
\`\`\`

4. **Bases de datos:**
   - Read replicas para consultas
   - Sharding para escrituras
   - Caché con Redis

5. **Caché distribuido:**
   - Redis para datos frecuentes (tarifas, depósitos)
   - Reduce carga en bases de datos

**Para producción:**
- Kubernetes para orquestación
- Auto-scaling basado en métricas
- CDN para contenido estático

---

### ¿Qué optimizaciones implementaron para mejorar performance?

**Respuesta:**

**1. Índices en base de datos:**

\`\`\`sql
-- Índice para búsquedas por email
CREATE INDEX idx_cliente_email ON cliente(email);

-- Índice para búsquedas por estado
CREATE INDEX idx_solicitud_estado ON solicitud(estado);

-- Índice compuesto para búsquedas geográficas
CREATE INDEX idx_deposito_coords ON deposito(latitud, longitud);

-- Índice para ordenar tramos de una ruta
CREATE INDEX idx_tramo_ruta_orden ON tramo(ruta_id, orden);
\`\`\`

**2. Paginación en listados:**

\`\`\`java
@GetMapping
public ResponseEntity<Page<ClienteResponseDTO>> listarClientes(
    @RequestParam(defaultValue = "0") int page,
    @RequestParam(defaultValue = "20") int size) {
    
    Pageable pageable = PageRequest.of(page, size);
    Page<Cliente> clientes = clienteRepository.findAll(pageable);
    // ...
}
\`\`\`

**3. Lazy Loading en JPA:**

\`\`\`java
@Entity
public class Ruta {
    @OneToMany(mappedBy = "ruta", fetch = FetchType.LAZY)
    private List<Tramo> tramos;
    // Solo carga tramos cuando se accede a ellos
}
\`\`\`

**4. Connection Pooling:**

\`\`\`yaml
spring:
  datasource:
    hikari:
      maximum-pool-size: 10
      minimum-idle: 5
      connection-timeout: 30000
\`\`\`

**5. Timeouts en RestTemplate:**

\`\`\`java
@Bean
public RestTemplate restTemplate() {
    SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
    factory.setConnectTimeout(5000);
    factory.setReadTimeout(5000);
    return new RestTemplate(factory);
}
\`\`\`

**6. Caché de distancias (Google Maps):**
- Evita llamadas repetidas a API externa
- Reduce costos y latencia

---

### ¿Cómo monitorearían el sistema en producción?

**Respuesta:**

**Herramientas propuestas:**

1. **Spring Boot Actuator:**
   - Endpoints de health, metrics, info
   - Ya implementado en todos los servicios

\`\`\`yaml
management:
  endpoints:
    web:
      exposure:
        include: health,metrics,info
\`\`\`

2. **Prometheus + Grafana:**
   - Prometheus: Recolección de métricas
   - Grafana: Visualización de dashboards

**Métricas clave:**
- Requests por segundo
- Latencia promedio
- Tasa de errores
- Uso de CPU y memoria
- Conexiones a BD

3. **ELK Stack (Elasticsearch, Logstash, Kibana):**
   - Logs centralizados de todos los servicios
   - Búsqueda y análisis de logs
   - Alertas en tiempo real

4. **Jaeger (Distributed Tracing):**
   - Rastreo de requests entre microservicios
   - Identificación de cuellos de botella
   - Visualización de flujos

**Alertas configuradas:**
- ⚠️ Tasa de errores > 5%
- ⚠️ Latencia > 2 segundos
- ⚠️ Uso de CPU > 80%
- ⚠️ Servicio caído

**Para este proyecto:**
- Implementamos logging completo
- Actuator habilitado
- Preparado para integrar herramientas de monitoreo

---

## Preguntas Finales

### ¿Qué fue lo más desafiante del proyecto?

**Respuesta:**

**Desafíos principales:**

1. **Comunicación entre microservicios:**
   - Manejar fallos de red
   - Implementar circuit breakers
   - Debugging de llamadas entre servicios

2. **Transacciones distribuidas:**
   - No podemos usar transacciones ACID entre servicios
   - Implementar consistencia eventual
   - Manejar rollbacks manuales

3. **Configuración de Keycloak:**
   - Curva de aprendizaje inicial
   - Integración con Spring Security
   - Configuración de roles y permisos

4. **Integración con Google Maps:**
   - Manejo de respuestas JSON complejas
   - Implementar fallback a Haversine
   - Optimizar costos con caché

5. **Docker Compose:**
   - Orden de inicio de servicios
   - Configuración de healthchecks
   - Gestión de variables de entorno

**Lecciones aprendidas:**
- Importancia de logging completo
- Necesidad de manejo robusto de errores
- Valor de la documentación clara
- Beneficios de patrones consistentes

---

### ¿Qué mejorarían si tuvieran más tiempo?

**Respuesta:**

**Mejoras prioritarias:**

1. **Testing completo:**
   - Tests unitarios (JUnit 5 + Mockito)
   - Tests de integración
   - Tests de contrato entre servicios
   - Tests de carga

2. **Mensajería asíncrona:**
   - RabbitMQ o Kafka para eventos
   - Desacoplar servicios temporalmente
   - Mejor para notificaciones

3. **Caché distribuido:**
   - Redis para datos frecuentes
   - Reducir carga en bases de datos
   - Mejorar tiempos de respuesta

4. **Observabilidad:**
   - ELK Stack para logs centralizados
   - Prometheus + Grafana para métricas
   - Jaeger para distributed tracing

5. **CI/CD:**
   - Pipeline automatizado
   - Tests automáticos en cada commit
   - Deployment automático a staging

6. **Service Discovery:**
   - Eureka o Consul
   - Registro dinámico de servicios
   - Facilitar escalamiento horizontal

7. **API Gateway avanzado:**
   - Rate limiting
   - Request/response transformation
   - Caché de respuestas

8. **Seguridad adicional:**
   - Encriptación de datos sensibles
   - Auditoría completa de operaciones
   - Protección contra ataques (OWASP Top 10)

---

### ¿Cómo dividieron el trabajo entre los 4 integrantes?

**Respuesta sugerida (adaptar a su realidad):**

**División por microservicios:**

- **Integrante 1**: clientes-service + depositos-service + documentación
- **Integrante 2**: camiones-service + tarifas-service + Docker
- **Integrante 3**: solicitudes-service + Keycloak + seguridad
- **Integrante 4**: rutas-service + API Gateway + Google Maps

**Trabajo colaborativo:**
- Definición de arquitectura: Todos juntos
- Modelo de datos (DER): Todos juntos
- Revisión de código: Peer review entre integrantes
- Testing: Todos contribuyeron
- Documentación: Distribuida por servicio

**Herramientas de colaboración:**
- Git + GitHub para control de versiones
- Branches por feature
- Pull requests con revisión
- Reuniones semanales de sincronización

---

**Fecha de creación:** Enero 2025  
**Versión:** 1.0  
**Propósito:** Guía para defensa del TPI
