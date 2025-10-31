# Fase 2 - Servicios de Orquestación

## Descripción General

La Fase 2 implementa los servicios de orquestación que coordinan las operaciones complejas del sistema de logística. Estos servicios se comunican con los servicios base de la Fase 1 para gestionar solicitudes de transporte y calcular rutas óptimas.

---

## 5. Solicitudes Service

**Puerto:** 8085  
**Base de datos:** PostgreSQL (solicitudes_db)  
**Responsabilidad:** Gestión de solicitudes de transporte y contenedores

### Entidades

#### Contenedor
\`\`\`java
- id: Long (PK)
- identificacion: String (único)
- peso: BigDecimal (kg)
- volumen: BigDecimal (m³)
- direccionOrigen: String
- latitudOrigen: Double
- longitudOrigen: Double
- direccionDestino: String
- latitudDestino: Double
- longitudDestino: Double
- activo: Boolean
- fechaCreacion: LocalDateTime
- fechaActualizacion: LocalDateTime
\`\`\`

#### Solicitud
\`\`\`java
- id: Long (PK)
- numero: String (único, generado automáticamente)
- clienteId: Long (referencia a clientes-service)
- contenedor: Contenedor (OneToOne)
- estado: EstadoSolicitud (BORRADOR, PROGRAMADA, EN_TRANSITO, ENTREGADA)
- costoEstimado: BigDecimal
- tiempoEstimadoHoras: Integer
- costoFinal: BigDecimal
- tiempoRealHoras: Integer
- fechaSolicitud: LocalDateTime
- fechaInicio: LocalDateTime
- fechaEntrega: LocalDateTime
- observaciones: String
- activo: Boolean
\`\`\`

### Endpoints

| Método | Endpoint | Descripción | Rol |
|--------|----------|-------------|-----|
| POST | `/api/solicitudes` | Crear nueva solicitud | CLIENTE |
| GET | `/api/solicitudes` | Listar todas las solicitudes | ADMIN, OPERADOR |
| GET | `/api/solicitudes/{id}` | Obtener solicitud por ID | CLIENTE, ADMIN, OPERADOR |
| PUT | `/api/solicitudes/{id}` | Actualizar solicitud | ADMIN, OPERADOR |
| DELETE | `/api/solicitudes/{id}` | Eliminar solicitud | ADMIN |
| GET | `/api/solicitudes/numero/{numero}` | Buscar por número | CLIENTE, ADMIN, OPERADOR |
| GET | `/api/solicitudes/cliente/{clienteId}` | Listar por cliente | CLIENTE, ADMIN, OPERADOR |
| GET | `/api/solicitudes/estado/{estado}` | Filtrar por estado | ADMIN, OPERADOR |
| PATCH | `/api/solicitudes/{id}/estado` | Cambiar estado | ADMIN, OPERADOR |
| GET | `/api/solicitudes/{id}/seguimiento` | Seguimiento detallado | CLIENTE, ADMIN, OPERADOR |
| GET | `/api/contenedores/{id}` | Obtener contenedor por ID | ADMIN, OPERADOR |

### Comunicación con otros servicios

- **clientes-service**: Valida la existencia del cliente al crear una solicitud
- **rutas-service**: Recibe actualizaciones de estado cuando se asignan rutas

### Reglas de negocio

1. Al crear una solicitud, se genera automáticamente un número único (formato: SOL-YYYYMMDD-XXXX)
2. El estado inicial de una solicitud es BORRADOR
3. Solo se puede cambiar el estado siguiendo el flujo: BORRADOR → PROGRAMADA → EN_TRANSITO → ENTREGADA
4. El cliente debe existir en clientes-service antes de crear la solicitud
5. El contenedor debe tener coordenadas válidas de origen y destino
6. El peso y volumen del contenedor deben ser mayores a cero

---

## 6. Rutas Service

**Puerto:** 8086  
**Base de datos:** PostgreSQL (rutas_db)  
**Responsabilidad:** Cálculo de rutas, gestión de tramos y asignación de camiones

### Entidades

#### Ruta
\`\`\`java
- id: Long (PK)
- solicitudId: Long (referencia a solicitudes-service)
- cantidadTramos: Integer
- cantidadDepositos: Integer
- distanciaTotal: BigDecimal (km)
- costoEstimado: BigDecimal
- tiempoEstimadoHoras: Integer
- fechaCreacion: LocalDateTime
- activo: Boolean
\`\`\`

#### Tramo
\`\`\`java
- id: Long (PK)
- ruta: Ruta (ManyToOne)
- numeroTramo: Integer
- origenTipo: String (ORIGEN/DEPOSITO)
- origenId: Long (null si es origen de solicitud)
- origenDireccion: String
- origenLatitud: Double
- origenLongitud: Double
- destinoTipo: String (DEPOSITO/DESTINO)
- destinoId: Long (null si es destino de solicitud)
- destinoDireccion: String
- destinoLatitud: Double
- destinoLongitud: Double
- tipoTramo: TipoTramo (ORIGEN_DEPOSITO, DEPOSITO_DEPOSITO, DEPOSITO_DESTINO, ORIGEN_DESTINO)
- estado: EstadoTramo (ESTIMADO, ASIGNADO, INICIADO, FINALIZADO)
- distancia: BigDecimal (km)
- costoAproximado: BigDecimal
- costoReal: BigDecimal
- fechaHoraEstimadaInicio: LocalDateTime
- fechaHoraInicio: LocalDateTime
- fechaHoraEstimadaFin: LocalDateTime
- fechaHoraFin: LocalDateTime
- camionId: Long (referencia a camiones-service)
- transportistaNombre: String
- activo: Boolean
\`\`\`

### Endpoints

| Método | Endpoint | Descripción | Rol |
|--------|----------|-------------|-----|
| POST | `/api/rutas/calcular` | Calcular rutas tentativas | ADMIN, OPERADOR |
| POST | `/api/rutas/asignar` | Asignar ruta a solicitud | ADMIN, OPERADOR |
| GET | `/api/rutas/{id}` | Obtener ruta por ID | ADMIN, OPERADOR |
| GET | `/api/rutas/solicitud/{solicitudId}` | Obtener ruta de solicitud | CLIENTE, ADMIN, OPERADOR |
| DELETE | `/api/rutas/{id}` | Eliminar ruta | ADMIN |
| GET | `/api/tramos/{id}` | Obtener tramo por ID | ADMIN, OPERADOR, TRANSPORTISTA |
| GET | `/api/tramos/ruta/{rutaId}` | Listar tramos de ruta | ADMIN, OPERADOR, TRANSPORTISTA |
| PATCH | `/api/tramos/{id}/asignar-camion` | Asignar camión a tramo | ADMIN, OPERADOR |
| PATCH | `/api/tramos/{id}/iniciar` | Iniciar tramo | TRANSPORTISTA |
| PATCH | `/api/tramos/{id}/finalizar` | Finalizar tramo | TRANSPORTISTA |
| GET | `/api/tramos/camion/{camionId}` | Tramos de un camión | TRANSPORTISTA |
| GET | `/api/tramos/estado/{estado}` | Filtrar por estado | ADMIN, OPERADOR |

### Comunicación con otros servicios

- **solicitudes-service**: Obtiene datos del contenedor y actualiza estado de solicitud
- **depositos-service**: Consulta depósitos disponibles y sus ubicaciones
- **camiones-service**: Valida disponibilidad y capacidad de camiones
- **tarifas-service**: Obtiene tarifas vigentes para cálculo de costos
- **Google Maps API**: Calcula distancias reales entre puntos (pendiente de integración)

### Reglas de negocio

1. **Cálculo de rutas tentativas:**
   - Busca depósitos cercanos entre origen y destino
   - Genera múltiples opciones de ruta con diferentes combinaciones de depósitos
   - Calcula distancia usando fórmula de Haversine (temporal, se reemplazará por Google Maps)
   - Estima costos basándose en tarifas vigentes y promedios de camiones

2. **Asignación de ruta:**
   - Valida que la solicitud exista y esté en estado BORRADOR o PROGRAMADA
   - Crea los tramos correspondientes en estado ESTIMADO
   - Actualiza el estado de la solicitud a PROGRAMADA

3. **Asignación de camión a tramo:**
   - Valida que el camión esté disponible
   - Verifica que el camión tenga capacidad suficiente (peso y volumen)
   - Marca el camión como no disponible
   - Cambia el estado del tramo a ASIGNADO

4. **Inicio de tramo:**
   - Solo puede iniciarse si está en estado ASIGNADO
   - Registra fecha/hora real de inicio
   - Cambia estado a INICIADO
   - Si es el primer tramo, actualiza la solicitud a EN_TRANSITO

5. **Finalización de tramo:**
   - Solo puede finalizarse si está en estado INICIADO
   - Registra fecha/hora real de fin
   - Calcula costo real del tramo
   - Libera el camión (marca como disponible)
   - Cambia estado a FINALIZADO
   - Si es el último tramo, actualiza la solicitud a ENTREGADA y calcula costos finales

6. **Cálculo de costos:**
   - Costo aproximado = (distancia × tarifa_km_base) + (distancia × consumo_promedio × tarifa_combustible)
   - Costo real = (distancia × costo_km_camion) + (distancia × consumo_camion × tarifa_combustible) + estadía_deposito

---

## Flujo de Operación Completo

### 1. Creación de Solicitud
\`\`\`
Cliente → solicitudes-service
  ↓
Valida cliente en clientes-service
  ↓
Crea Solicitud (estado: BORRADOR)
  ↓
Crea Contenedor asociado
\`\`\`

### 2. Cálculo de Rutas
\`\`\`
Operador → rutas-service/calcular
  ↓
Obtiene datos de solicitud
  ↓
Consulta depósitos cercanos
  ↓
Calcula distancias (Haversine)
  ↓
Consulta tarifas vigentes
  ↓
Genera opciones de ruta con costos estimados
\`\`\`

### 3. Asignación de Ruta
\`\`\`
Operador → rutas-service/asignar
  ↓
Crea Ruta asociada a solicitud
  ↓
Crea Tramos (estado: ESTIMADO)
  ↓
Actualiza Solicitud (estado: PROGRAMADA)
\`\`\`

### 4. Asignación de Camiones
\`\`\`
Operador → rutas-service/tramos/{id}/asignar-camion
  ↓
Valida disponibilidad en camiones-service
  ↓
Verifica capacidad del camión
  ↓
Asigna camión al tramo
  ↓
Marca camión como no disponible
  ↓
Actualiza Tramo (estado: ASIGNADO)
\`\`\`

### 5. Ejecución del Transporte
\`\`\`
Transportista → rutas-service/tramos/{id}/iniciar
  ↓
Registra inicio del tramo
  ↓
Actualiza Tramo (estado: INICIADO)
  ↓
Si es primer tramo: Solicitud → EN_TRANSITO

... transporte en curso ...

Transportista → rutas-service/tramos/{id}/finalizar
  ↓
Registra fin del tramo
  ↓
Calcula costo real
  ↓
Libera camión
  ↓
Actualiza Tramo (estado: FINALIZADO)
  ↓
Si es último tramo: Solicitud → ENTREGADA
\`\`\`

### 6. Seguimiento
\`\`\`
Cliente → solicitudes-service/{id}/seguimiento
  ↓
Obtiene solicitud con contenedor
  ↓
Consulta ruta asociada en rutas-service
  ↓
Obtiene todos los tramos con estados
  ↓
Retorna información completa del seguimiento
\`\`\`

---

## Consideraciones Técnicas

### Comunicación entre Microservicios

Se utiliza **RestTemplate** para comunicación síncrona entre servicios:

\`\`\`java
@Service
public class ClienteServiceClient {
    private final RestTemplate restTemplate;
    private final String clientesServiceUrl = "http://localhost:8081";
    
    public ClienteResponseDTO getCliente(Long id) {
        return restTemplate.getForObject(
            clientesServiceUrl + "/api/clientes/" + id,
            ClienteResponseDTO.class
        );
    }
}
\`\`\`

### Manejo de Errores

- **404 Not Found**: Cuando no se encuentra un recurso
- **400 Bad Request**: Validaciones de negocio fallidas
- **409 Conflict**: Estados inválidos o transiciones no permitidas
- **503 Service Unavailable**: Cuando un servicio dependiente no está disponible

### Transacciones

- Cada servicio maneja sus propias transacciones
- No hay transacciones distribuidas (patrón Saga no implementado en esta versión)
- Se recomienda implementar compensaciones manuales en caso de fallos

### Próximas Mejoras

1. Integración con Google Maps API para cálculo real de distancias
2. Implementación de circuit breakers con Resilience4j
3. Comunicación asíncrona con mensajería (RabbitMQ/Kafka)
4. Patrón Saga para transacciones distribuidas
5. Cache distribuido con Redis
6. Eventos de dominio para desacoplamiento

---

## Testing

Cada servicio incluye:
- Tests unitarios de servicios con Mockito
- Tests de integración de repositorios con H2
- Tests de controladores con MockMvc

Ejemplo de ejecución:
\`\`\`bash
cd solicitudes-service
mvn test
\`\`\`

---

## Despliegue

Cada servicio tiene su Dockerfile y puede ejecutarse independientemente:

\`\`\`bash
# Solicitudes Service
cd solicitudes-service
docker build -t solicitudes-service:1.0.0 .
docker run -p 8085:8085 solicitudes-service:1.0.0

# Rutas Service
cd rutas-service
docker build -t rutas-service:1.0.0 .
docker run -p 8086:8086 rutas-service:1.0.0
\`\`\`

O usando docker-compose (ver Fase 3).
