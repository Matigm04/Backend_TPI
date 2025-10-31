# Arquitectura de Microservicios - Modelo Implementado

## Diagrama de Arquitectura Implementada

\`\`\`
┌─────────────────────────────────────────────────────────────────────────┐
│                          CAPA DE CLIENTES                                │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐                  │
│  │  ClienteUI   │  │ OperadorUI   │  │TransportistaUI│                  │
│  │   (React)    │  │   (React)    │  │   (React)     │                  │
│  └──────┬───────┘  └──────┬───────┘  └──────┬────────┘                  │
│         │                 │                  │                           │
│         └─────────────────┴──────────────────┘                           │
│                           │                                              │
└───────────────────────────┼──────────────────────────────────────────────┘
                            │
                            ▼
┌─────────────────────────────────────────────────────────────────────────┐
│                       API GATEWAY (Puerto 8080)                          │
│  ┌────────────────────────────────────────────────────────────────┐    │
│  │  • Enrutamiento centralizado                                    │    │
│  │  • Validación JWT con Keycloak                                  │    │
│  │  • Rate limiting y circuit breakers                             │    │
│  │  • CORS y logging global                                        │    │
│  └────────────────────────────────────────────────────────────────┘    │
└───────────┬─────────────────────────────────────────────────────────────┘
            │
            ├──────────────┬──────────────┬──────────────┬──────────────┐
            ▼              ▼              ▼              ▼              ▼
┌─────────────────────────────────────────────────────────────────────────┐
│                    CAPA DE MICROSERVICIOS                                │
│                                                                          │
│  ┌──────────────────┐  ┌──────────────────┐  ┌──────────────────┐     │
│  │ clientes-service │  │depositos-service │  │ camiones-service │     │
│  │   Puerto 8081    │  │   Puerto 8082    │  │   Puerto 8083    │     │
│  ├──────────────────┤  ├──────────────────┤  ├──────────────────┤     │
│  │ • CRUD Clientes  │  │ • CRUD Depósitos │  │ • CRUD Camiones  │     │
│  │ • Validación     │  │ • Búsqueda por   │  │ • Disponibilidad │     │
│  │ • Contacto       │  │   coordenadas    │  │ • Capacidades    │     │
│  └────────┬─────────┘  └────────┬─────────┘  └────────┬─────────┘     │
│           │                     │                      │                │
│           ▼                     ▼                      ▼                │
│  ┌─────────────────┐   ┌─────────────────┐   ┌─────────────────┐     │
│  │  BD_Clientes    │   │  BD_Depositos   │   │  BD_Camiones    │     │
│  │  (PostgreSQL)   │   │  (PostgreSQL)   │   │  (PostgreSQL)   │     │
│  └─────────────────┘   └─────────────────┘   └─────────────────┘     │
│                                                                          │
│  ┌──────────────────┐  ┌──────────────────┐                            │
│  │ tarifas-service  │  │solicitudes-service│                           │
│  │   Puerto 8084    │  │   Puerto 8085    │                            │
│  ├──────────────────┤  ├──────────────────┤                            │
│  │ • CRUD Tarifas   │  │ • CRUD Solicitudes│                           │
│  │ • Vigencia       │  │ • Contenedores   │◄──────┐                    │
│  │ • Tipos tarifa   │  │ • Estados        │       │                    │
│  └────────┬─────────┘  └────────┬─────────┘       │                    │
│           │                     │                  │                    │
│           ▼                     ▼                  │                    │
│  ┌─────────────────┐   ┌─────────────────┐        │                    │
│  │  BD_Tarifas     │   │ BD_Solicitudes  │        │                    │
│  │  (PostgreSQL)   │   │  (PostgreSQL)   │        │                    │
│  └─────────────────┘   └─────────────────┘        │                    │
│                                                     │                    │
│  ┌──────────────────────────────────────────────┐  │                    │
│  │         rutas-service (Puerto 8086)          │  │                    │
│  ├──────────────────────────────────────────────┤  │                    │
│  │ • Cálculo de rutas tentativas                │  │                    │
│  │ • Gestión de tramos                          │  │                    │
│  │ • Asignación de camiones                     │  │                    │
│  │ • Inicio/Fin de viajes                       │  │                    │
│  │ • Integración Google Maps API                │──┘                    │
│  └────────┬─────────────────────────────────────┘                       │
│           │                                                              │
│           ▼                                                              │
│  ┌─────────────────┐                                                    │
│  │   BD_Rutas      │                                                    │
│  │  (PostgreSQL)   │                                                    │
│  └─────────────────┘                                                    │
│                                                                          │
└─────────────────────────────────────────────────────────────────────────┘

┌─────────────────────────────────────────────────────────────────────────┐
│                    SERVICIOS DE INFRAESTRUCTURA                          │
│                                                                          │
│  ┌──────────────────┐  ┌──────────────────┐                            │
│  │    Keycloak      │  │  Google Maps API │                            │
│  │  Puerto 8180     │  │    (Externa)     │                            │
│  ├──────────────────┤  └──────────────────┘                            │
│  │ • Autenticación  │                                                   │
│  │ • Autorización   │                                                   │
│  │ • Gestión roles  │                                                   │
│  │ • JWT tokens     │                                                   │
│  └────────┬─────────┘                                                   │
│           │                                                              │
│           ▼                                                              │
│  ┌─────────────────┐                                                    │
│  │  BD_Keycloak    │                                                    │
│  │  (PostgreSQL)   │                                                    │
│  └─────────────────┘                                                    │
└─────────────────────────────────────────────────────────────────────────┘
\`\`\`

## Resumen de Microservicios Implementados

| Microservicio | Puerto | Base de Datos | Responsabilidad Principal |
|---------------|--------|---------------|---------------------------|
| **api-gateway** | 8080 | - | Enrutamiento, autenticación, CORS |
| **clientes-service** | 8081 | BD_Clientes | Gestión de clientes |
| **depositos-service** | 8082 | BD_Depositos | Gestión de depósitos |
| **camiones-service** | 8083 | BD_Camiones | Gestión de camiones y transportistas |
| **tarifas-service** | 8084 | BD_Tarifas | Gestión de tarifas y configuraciones |
| **solicitudes-service** | 8085 | BD_Solicitudes | Gestión de solicitudes y contenedores |
| **rutas-service** | 8086 | BD_Rutas | Cálculo de rutas, tramos y asignaciones |

## Comunicación entre Microservicios

\`\`\`
solicitudes-service
    ├─► clientes-service (validar cliente existe)
    └─► rutas-service (crear ruta para solicitud)

rutas-service
    ├─► solicitudes-service (obtener datos de solicitud)
    ├─► depositos-service (obtener depósitos disponibles)
    ├─► camiones-service (buscar camiones disponibles)
    ├─► tarifas-service (obtener tarifas vigentes)
    └─► Google Maps API (calcular distancias reales)
\`\`\`

## Patrones de Comunicación

### 1. Comunicación Síncrona (REST)
- **Uso:** Operaciones que requieren respuesta inmediata
- **Implementación:** RestTemplate
- **Ejemplos:**
  - Validar que un cliente existe antes de crear solicitud
  - Obtener tarifas vigentes para calcular costos
  - Consultar disponibilidad de camiones

### 2. Base de Datos por Microservicio
- **Patrón:** Database per Service
- **Ventajas:**
  - Independencia de datos
  - Escalabilidad independiente
  - Tecnología específica por servicio
- **Implementación:** 7 bases de datos PostgreSQL independientes

### 3. API Gateway Pattern
- **Responsabilidades:**
  - Punto único de entrada
  - Autenticación centralizada
  - Enrutamiento inteligente
  - Rate limiting
  - Circuit breakers

## Decisiones de Diseño

### ¿Por qué 6 microservicios y no menos?

**Opción rechazada:** Tener un solo "ServicioLogistica" que maneje depósitos, camiones y rutas.

**Razones para separar:**

1. **Principio de Responsabilidad Única (SRP)**
   - Cada servicio tiene una responsabilidad clara y acotada
   - Facilita el mantenimiento y evolución

2. **Escalabilidad Independiente**
   - `rutas-service` puede recibir más carga (cálculos complejos)
   - `depositos-service` y `camiones-service` son más estables
   - Podemos escalar solo lo que necesitamos

3. **Despliegue Independiente**
   - Cambios en lógica de rutas no afectan a depósitos
   - Actualizaciones sin downtime de todo el sistema

4. **Equipos Independientes**
   - Diferentes equipos pueden trabajar en paralelo
   - Menos conflictos de código

### ¿Por qué no un "ServicioIntegracionesExternas"?

**Opción rechazada:** Crear un microservicio dedicado solo para integraciones externas.

**Razones:**

1. **Acoplamiento Innecesario**
   - Solo `rutas-service` usa Google Maps
   - Agregar un servicio intermedio añade latencia

2. **Complejidad sin Beneficio**
   - No hay lógica de negocio en la integración
   - Es solo una llamada HTTP a API externa

3. **Mejor Práctica**
   - Las integraciones externas deben estar en el servicio que las necesita
   - Facilita el testing y mocking

### ¿Por qué Keycloak como servicio separado?

**Razones:**

1. **Servicio de Infraestructura**
   - No es parte de la lógica de negocio
   - Es un proveedor de identidad federada

2. **Reutilización**
   - Todos los microservicios lo usan
   - Gestión centralizada de usuarios y roles

3. **Seguridad**
   - Especializado en autenticación/autorización
   - Cumple estándares OAuth2 y OpenID Connect

## Endpoints por Rol

### Cliente
\`\`\`
POST   /api/solicitudes              - Crear solicitud
GET    /api/solicitudes/{id}         - Ver mi solicitud
GET    /api/solicitudes/{id}/estado  - Seguimiento
\`\`\`

### Operador/Administrador
\`\`\`
# Gestión de recursos
POST   /api/clientes
POST   /api/depositos
POST   /api/camiones
POST   /api/tarifas

# Operaciones
GET    /api/rutas/tentativas          - Calcular rutas
POST   /api/rutas/{id}/asignar        - Asignar ruta
POST   /api/tramos/{id}/asignar-camion - Asignar camión
GET    /api/solicitudes?estado=...    - Filtrar solicitudes
\`\`\`

### Transportista
\`\`\`
GET    /api/tramos/mis-asignaciones   - Ver mis tramos
POST   /api/tramos/{id}/iniciar       - Iniciar viaje
POST   /api/tramos/{id}/finalizar     - Finalizar viaje
\`\`\`

## Flujo Completo de una Solicitud

\`\`\`
1. Cliente crea solicitud
   ClienteUI → API Gateway → solicitudes-service
   
2. Solicitudes valida cliente
   solicitudes-service → clientes-service
   
3. Operador calcula rutas tentativas
   OperadorUI → API Gateway → rutas-service
   rutas-service → depositos-service (obtener depósitos)
   rutas-service → Google Maps API (calcular distancias)
   rutas-service → tarifas-service (obtener tarifas)
   
4. Operador asigna ruta
   OperadorUI → API Gateway → rutas-service
   rutas-service → solicitudes-service (actualizar estado)
   
5. Operador asigna camión a tramo
   OperadorUI → API Gateway → rutas-service
   rutas-service → camiones-service (verificar disponibilidad)
   rutas-service → camiones-service (marcar como ocupado)
   
6. Transportista inicia viaje
   TransportistaUI → API Gateway → rutas-service
   rutas-service → camiones-service (obtener datos camión)
   
7. Transportista finaliza viaje
   TransportistaUI → API Gateway → rutas-service
   rutas-service → camiones-service (liberar camión)
   rutas-service → solicitudes-service (actualizar estado)
\`\`\`

## Ventajas de esta Arquitectura

### 1. Escalabilidad
- Cada servicio escala independientemente
- `rutas-service` puede tener más instancias por carga de cálculos

### 2. Resiliencia
- Fallo en un servicio no tumba todo el sistema
- Circuit breakers previenen cascadas de fallos

### 3. Mantenibilidad
- Código organizado por dominio
- Fácil localizar y corregir bugs

### 4. Flexibilidad Tecnológica
- Cada servicio puede usar tecnología diferente si es necesario
- Actualmente todos usan Spring Boot, pero podrían cambiar

### 5. Despliegue Continuo
- Despliegues independientes sin downtime
- Rollback granular por servicio

## Desventajas y Mitigaciones

### 1. Complejidad Operacional
**Problema:** Más servicios = más complejidad de despliegue
**Mitigación:** Docker Compose para desarrollo, Kubernetes para producción

### 2. Latencia de Red
**Problema:** Comunicación entre servicios añade latencia
**Mitigación:** 
- Caché de datos frecuentes
- Comunicación asíncrona donde sea posible

### 3. Transacciones Distribuidas
**Problema:** No hay transacciones ACID entre servicios
**Mitigación:** 
- Patrón Saga (no implementado aún)
- Compensación manual en caso de errores

### 4. Testing Complejo
**Problema:** Tests de integración requieren múltiples servicios
**Mitigación:**
- Tests unitarios con mocks
- Tests de contrato entre servicios
- Ambiente de staging completo

## Comparación con Alternativas

### Alternativa 1: Monolito
\`\`\`
Ventajas:
- Más simple de desarrollar inicialmente
- Transacciones ACID nativas
- Menos latencia

Desventajas:
- Escalabilidad limitada
- Despliegues riesgosos (todo o nada)
- Acoplamiento alto
\`\`\`

### Alternativa 2: Menos Microservicios (3-4)
\`\`\`
Ejemplo: ServicioLogistica que incluya depósitos, camiones y rutas

Ventajas:
- Menos complejidad operacional
- Menos comunicación entre servicios

Desventajas:
- Servicios más grandes y complejos
- Menos granularidad en escalado
- Mayor acoplamiento
\`\`\`

### Alternativa 3: Más Microservicios (10+)
\`\`\`
Ejemplo: Separar contenedores-service de solicitudes-service

Ventajas:
- Máxima granularidad
- Máxima independencia

Desventajas:
- Complejidad operacional muy alta
- Overhead de comunicación
- Overkill para el dominio
\`\`\`

## Conclusión

La arquitectura de 6 microservicios + API Gateway representa el **balance óptimo** entre:
- Complejidad manejable
- Beneficios de microservicios
- Requisitos del dominio
- Capacidad del equipo

Cada servicio tiene una responsabilidad clara, puede escalar independientemente, y la comunicación entre ellos está bien definida y justificada.
