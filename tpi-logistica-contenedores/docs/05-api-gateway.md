# API Gateway - Fase 3

## Descripción General

El API Gateway es el punto de entrada único para todos los clientes del sistema. Implementa enrutamiento, balanceo de carga, circuit breakers y manejo centralizado de CORS.

**Puerto:** 8080  
**Tecnología:** Spring Cloud Gateway  

---

## Arquitectura

\`\`\`
Cliente/Frontend
       ↓
   API Gateway (8080)
       ↓
   ┌───┴───┬───────┬──────────┬──────────┬────────────┬────────────┐
   ↓       ↓       ↓          ↓          ↓            ↓            ↓
clientes depositos camiones tarifas solicitudes    rutas
 (8081)   (8082)   (8083)   (8084)    (8085)       (8086)
\`\`\`

---

## Rutas Configuradas

### Servicios de Negocio

| Ruta | Servicio Destino | Puerto | Descripción |
|------|------------------|--------|-------------|
| `/api/clientes/**` | clientes-service | 8081 | Gestión de clientes |
| `/api/depositos/**` | depositos-service | 8082 | Gestión de depósitos |
| `/api/camiones/**` | camiones-service | 8083 | Gestión de camiones |
| `/api/tarifas/**` | tarifas-service | 8084 | Gestión de tarifas |
| `/api/solicitudes/**` | solicitudes-service | 8085 | Gestión de solicitudes |
| `/api/contenedores/**` | solicitudes-service | 8085 | Gestión de contenedores |
| `/api/rutas/**` | rutas-service | 8086 | Gestión de rutas |
| `/api/tramos/**` | rutas-service | 8086 | Gestión de tramos |

### Documentación Swagger

| Ruta | Servicio | Descripción |
|------|----------|-------------|
| `/clientes/swagger-ui/**` | clientes-service | UI de Swagger |
| `/clientes/v3/api-docs/**` | clientes-service | OpenAPI JSON |
| `/depositos/swagger-ui/**` | depositos-service | UI de Swagger |
| `/depositos/v3/api-docs/**` | depositos-service | OpenAPI JSON |
| `/camiones/swagger-ui/**` | camiones-service | UI de Swagger |
| `/camiones/v3/api-docs/**` | camiones-service | OpenAPI JSON |
| `/tarifas/swagger-ui/**` | tarifas-service | UI de Swagger |
| `/tarifas/v3/api-docs/**` | tarifas-service | OpenAPI JSON |
| `/solicitudes/swagger-ui/**` | solicitudes-service | UI de Swagger |
| `/solicitudes/v3/api-docs/**` | solicitudes-service | OpenAPI JSON |
| `/rutas/swagger-ui/**` | rutas-service | UI de Swagger |
| `/rutas/v3/api-docs/**` | rutas-service | OpenAPI JSON |

---

## Características

### 1. Circuit Breakers

Cada ruta tiene configurado un circuit breaker que redirige a un endpoint de fallback cuando el servicio no está disponible:

\`\`\`yaml
filters:
  - name: CircuitBreaker
    args:
      name: clientesCircuitBreaker
      fallbackUri: forward:/fallback/clientes
\`\`\`

**Endpoints de Fallback:**
- `/fallback/clientes`
- `/fallback/depositos`
- `/fallback/camiones`
- `/fallback/tarifas`
- `/fallback/solicitudes`
- `/fallback/rutas`

**Respuesta de Fallback:**
\`\`\`json
{
  "timestamp": "2025-01-15T10:30:00",
  "status": 503,
  "error": "Service Unavailable",
  "message": "Clientes Service is currently unavailable. Please try again later."
}
\`\`\`

### 2. CORS Global

Configuración permisiva para desarrollo (debe ajustarse en producción):

\`\`\`yaml
globalcors:
  corsConfigurations:
    '[/**]':
      allowedOrigins: "*"
      allowedMethods: [GET, POST, PUT, DELETE, PATCH, OPTIONS]
      allowedHeaders: "*"
      exposedHeaders: [Authorization]
      maxAge: 3600
\`\`\`

### 3. Logging Global

Filtro que registra todas las peticiones y respuestas:

\`\`\`java
@Component
public class LoggingGlobalFilter implements GlobalFilter {
    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        log.info("Request: {} {}", method, uri);
        // ... procesa request ...
        log.info("Response: {} - Status: {}", uri, status);
    }
}
\`\`\`

### 4. Actuator Endpoints

Endpoints de monitoreo disponibles:

- `GET /actuator/health` - Estado de salud del gateway
- `GET /actuator/info` - Información de la aplicación
- `GET /actuator/gateway/routes` - Lista de rutas configuradas

---

## Configuración

### Variables de Entorno

Para producción, las URLs de los servicios deben configurarse mediante variables de entorno:

\`\`\`yaml
spring:
  cloud:
    gateway:
      routes:
        - id: clientes-service
          uri: ${CLIENTES_SERVICE_URL:http://localhost:8081}
\`\`\`

### Configuración de Timeout

\`\`\`yaml
spring:
  cloud:
    gateway:
      httpclient:
        connect-timeout: 5000
        response-timeout: 30s
\`\`\`

---

## Uso

### Acceso a través del Gateway

En lugar de acceder directamente a los servicios:
\`\`\`
❌ http://localhost:8081/api/clientes
\`\`\`

Los clientes deben usar el gateway:
\`\`\`
✅ http://localhost:8080/api/clientes
\`\`\`

### Ejemplo de Petición

\`\`\`bash
# Crear un cliente a través del gateway
curl -X POST http://localhost:8080/api/clientes \
  -H "Content-Type: application/json" \
  -d '{
    "nombre": "Juan",
    "apellido": "Pérez",
    "email": "juan@example.com",
    "telefono": "+54911234567"
  }'
\`\`\`

### Acceso a Swagger

\`\`\`bash
# Swagger del servicio de clientes
http://localhost:8080/clientes/swagger-ui/index.html

# Swagger del servicio de solicitudes
http://localhost:8080/solicitudes/swagger-ui/index.html
\`\`\`

---

## Seguridad (Pendiente)

En futuras versiones se integrará con Keycloak para:

1. **Autenticación JWT**: Validación de tokens en el gateway
2. **Autorización por Rol**: Filtros que verifican roles antes de enrutar
3. **Rate Limiting**: Límite de peticiones por usuario/IP

Ejemplo de configuración futura:
\`\`\`yaml
spring:
  security:
    oauth2:
      resourceserver:
        jwt:
          issuer-uri: http://localhost:8180/realms/logistica
\`\`\`

---

## Monitoreo y Logs

### Logs de Peticiones

\`\`\`
2025-01-15 10:30:00 - Request: POST http://localhost:8080/api/clientes
2025-01-15 10:30:01 - Response: http://localhost:8080/api/clientes - Status: 201
\`\`\`

### Health Check

\`\`\`bash
curl http://localhost:8080/actuator/health
\`\`\`

Respuesta:
\`\`\`json
{
  "status": "UP",
  "components": {
    "diskSpace": {"status": "UP"},
    "ping": {"status": "UP"}
  }
}
\`\`\`

### Rutas Activas

\`\`\`bash
curl http://localhost:8080/actuator/gateway/routes
\`\`\`

---

## Despliegue

### Ejecución Local

\`\`\`bash
cd api-gateway
mvn spring-boot:run
\`\`\`

### Docker

\`\`\`bash
cd api-gateway
docker build -t api-gateway:1.0.0 .
docker run -p 8080:8080 api-gateway:1.0.0
\`\`\`

### Docker Compose

El gateway se incluye en el docker-compose.yml principal:

\`\`\`yaml
services:
  api-gateway:
    build: ./api-gateway
    ports:
      - "8080:8080"
    depends_on:
      - clientes-service
      - depositos-service
      - camiones-service
      - tarifas-service
      - solicitudes-service
      - rutas-service
\`\`\`

---

## Troubleshooting

### Problema: Servicio no responde

**Síntoma:** Error 503 Service Unavailable

**Solución:**
1. Verificar que el servicio destino esté ejecutándose
2. Revisar logs del gateway: `docker logs api-gateway`
3. Verificar conectividad: `curl http://localhost:8081/actuator/health`

### Problema: CORS errors

**Síntoma:** Error de CORS en el navegador

**Solución:**
1. Verificar configuración de CORS en application.yml
2. Asegurar que el header `Origin` esté permitido
3. Revisar que los métodos HTTP estén en `allowedMethods`

### Problema: Timeout

**Síntoma:** Gateway timeout después de 30 segundos

**Solución:**
1. Aumentar `response-timeout` en configuración
2. Optimizar el servicio destino
3. Implementar cache para operaciones lentas

---

## Próximas Mejoras

1. **Service Discovery**: Integración con Eureka/Consul
2. **Load Balancing**: Balanceo entre múltiples instancias
3. **Rate Limiting**: Límite de peticiones por cliente
4. **API Versioning**: Soporte para múltiples versiones de API
5. **Request/Response Transformation**: Modificación de payloads
6. **Distributed Tracing**: Integración con Zipkin/Jaeger
