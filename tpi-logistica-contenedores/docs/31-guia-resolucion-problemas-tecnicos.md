# Guía Técnica de Resolución de Problemas - Sistema de Logística

**Audiencia**: Desarrolladores backend  
**Fecha**: 15 de Noviembre de 2025  
**Propósito**: Guía técnica detallada para resolver problemas identificados en pruebas E2E

---

## 📋 ÍNDICE

1. [Fix: Error 500 en Creación de Solicitudes](#fix-1-error-500-en-creación-de-solicitudes)
2. [Fix: Implementar Endpoint PATCH para Costos](#fix-2-implementar-endpoint-patch-para-costos)
3. [Fix: Validación de Rutas Duplicadas](#fix-3-validación-de-rutas-duplicadas)
4. [Fix: Tarifas Inactivas en Datos de Prueba](#fix-4-tarifas-inactivas-en-datos-de-prueba)
5. [Fix: API Gateway Circuit Breaker](#fix-5-api-gateway-circuit-breaker)
6. [Scripts SQL de Diagnóstico](#scripts-sql-de-diagnóstico)
7. [Configuración de Logging](#configuración-de-logging)

---

## FIX #1: Error 500 en Creación de Solicitudes

### 🔴 PRIORIDAD: CRÍTICA

### Problema
El endpoint `POST /api/solicitudes` retorna error 500 consistentemente. Los logs muestran que el cliente se valida correctamente, pero falla en la persistencia.

### Causa Raíz Sospechada
Posible problema de encoding/charset al comparar el estado "PENDIENTE" con el constraint de base de datos.

### Pasos de Diagnóstico

#### 1. Habilitar Logging DEBUG

**Archivo**: `solicitudes-service/src/main/resources/application.yml`

```yaml
logging:
  level:
    com.logistica.solicitudes: DEBUG
    org.hibernate.SQL: DEBUG
    org.hibernate.type.descriptor.sql.BasicBinder: TRACE
    org.hibernate.orm.jdbc.bind: TRACE
  pattern:
    console: "%d{yyyy-MM-dd HH:mm:ss.SSS} %5p [%t] %-40.40logger{39} : %m%n"
```

#### 2. Agregar Logging Explícito en el Service

**Archivo**: `solicitudes-service/.../service/SolicitudService.java`

```java
@Service
@Slf4j
public class SolicitudService {
    
    @Transactional
    public SolicitudResponseDTO crearSolicitud(SolicitudRequestDTO request) {
        log.info("=== INICIO CREACIÓN SOLICITUD ===");
        log.info("Cliente ID: {}", request.getClienteId());
        log.info("Tarifa ID: {}", request.getTarifaId());
        
        // Validar cliente
        validarCliente(request.getClienteId());
        log.info("✓ Cliente validado");
        
        // Crear contenedor
        Contenedor contenedor = crearContenedor(request.getContenedor());
        log.info("✓ Contenedor creado - ID: {}", contenedor.getId());
        
        // Crear solicitud
        Solicitud solicitud = Solicitud.builder()
                .clienteId(request.getClienteId())
                .contenedor(contenedor)
                .ubicacionOrigen(request.getUbicacionOrigen())
                .ubicacionDestino(request.getUbicacionDestino())
                .fechaProgramada(request.getFechaProgramada())
                .observaciones(request.getObservaciones())
                .tarifaId(request.getTarifaId())
                .estado(EstadoSolicitud.PENDIENTE)
                .activo(true)
                .fechaSolicitud(LocalDateTime.now())
                .numero(generarNumeroSolicitud())
                .build();
        
        // LOGGING CRÍTICO ANTES DE PERSISTIR
        log.debug("Estado a persistir: [{}]", solicitud.getEstado());
        log.debug("Estado name(): [{}]", solicitud.getEstado().name());
        log.debug("Estado toString(): [{}]", solicitud.getEstado().toString());
        log.debug("Estado bytes: {}", 
            Arrays.toString(solicitud.getEstado().name().getBytes(StandardCharsets.UTF_8)));
        
        try {
            // Intentar persistir
            Solicitud solicitudGuardada = solicitudRepository.save(solicitud);
            log.info("✓ Solicitud persistida - ID: {}, Número: {}", 
                solicitudGuardada.getId(), solicitudGuardada.getNumero());
            
            return mapToResponseDTO(solicitudGuardada);
            
        } catch (DataIntegrityViolationException e) {
            log.error("❌ Error de integridad de datos", e);
            log.error("Causa: {}", e.getMostSpecificCause().getMessage());
            throw new RuntimeException("Error al crear solicitud: " + 
                e.getMostSpecificCause().getMessage(), e);
        } catch (Exception e) {
            log.error("❌ Error inesperado al persistir solicitud", e);
            throw e;
        }
    }
}
```

#### 3. Verificar Encoding de Base de Datos

```sql
-- Conectarse a PostgreSQL
psql -U logistica -d solicitudes_db

-- Verificar encodings
SHOW SERVER_ENCODING;
SHOW CLIENT_ENCODING;

-- Debería ser:
-- server_encoding | UTF8
-- client_encoding | UTF8
```

#### 4. Crear Endpoint de Diagnóstico

**Archivo**: `solicitudes-service/.../controller/SolicitudController.java`

```java
@RestController
@RequestMapping("/api/solicitudes")
public class SolicitudController {
    
    // Endpoint de diagnóstico
    @PostMapping("/diagnostico")
    @PreAuthorize("hasRole('OPERADOR')")
    @Operation(summary = "[DEBUG] Validar solicitud sin persistir")
    public ResponseEntity<Map<String, Object>> diagnosticarSolicitud(
            @Valid @RequestBody SolicitudRequestDTO request) {
        
        Map<String, Object> diagnostico = new HashMap<>();
        
        try {
            // 1. Validar cliente
            diagnostico.put("clienteValido", validarCliente(request.getClienteId()));
            
            // 2. Validar contenedor
            ContenedorDTO cont = request.getContenedor();
            diagnostico.put("contenedorDTO", Map.of(
                "identificacion", cont.getIdentificacion(),
                "peso", cont.getPeso(),
                "volumen", cont.getVolumen(),
                "estado", cont.getEstado()
            ));
            
            // 3. Simular creación de entidad
            Solicitud solicitud = Solicitud.builder()
                    .estado(EstadoSolicitud.PENDIENTE)
                    .build();
            
            diagnostico.put("estadoEnum", solicitud.getEstado().name());
            diagnostico.put("estadoBytes", 
                solicitud.getEstado().name().getBytes(StandardCharsets.UTF_8));
            
            // 4. Verificar constraint
            diagnostico.put("estadosPermitidos", 
                Arrays.asList("PENDIENTE", "PROGRAMADA", "EN_TRANSITO", "ENTREGADA", "CANCELADA"));
            
            diagnostico.put("resultado", "OK - Validación exitosa");
            return ResponseEntity.ok(diagnostico);
            
        } catch (Exception e) {
            diagnostico.put("error", e.getMessage());
            diagnostico.put("stackTrace", 
                Arrays.stream(e.getStackTrace())
                    .limit(5)
                    .map(StackTraceElement::toString)
                    .collect(Collectors.toList())
            );
            return ResponseEntity.status(500).body(diagnostico);
        }
    }
}
```

#### 5. Posible Fix: Especificar @Enumerated Explícitamente

**Archivo**: `solicitudes-service/.../model/Solicitud.java`

```java
@Entity
@Table(name = "solicitudes")
public class Solicitud {
    
    // ... otros campos
    
    @Enumerated(EnumType.STRING)  // ← ASEGURAR que esté presente
    @Column(nullable = false, length = 20)
    private EstadoSolicitud estado;
    
    // ...
}
```

#### 6. Alternativa: Usar @Formula para Debugging

```java
@Entity
@Table(name = "solicitudes")
public class Solicitud {
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private EstadoSolicitud estado;
    
    // Campo temporal para debugging
    @Formula("CAST(estado AS TEXT)")
    @Transient
    private String estadoRaw;
    
    @PostLoad
    private void postLoad() {
        log.debug("Estado cargado desde BD: [{}] - Raw: [{}]", estado, estadoRaw);
    }
}
```

### Testing del Fix

```powershell
# 1. Rebuild del servicio
cd solicitudes-service
mvn clean package -DskipTests

# 2. Reiniciar contenedor
docker-compose restart solicitudes-service

# 3. Esperar a que inicie
Start-Sleep -Seconds 15

# 4. Probar endpoint de diagnóstico
$headers = @{ 
    Authorization = "Bearer $TOKEN"
    "Content-Type" = "application/json"
}
$body = @{
    clienteId = 5
    contenedor = @{
        identificacion = "TEST-DEBUG-01"
        peso = 1000
        volumen = 10
        largoM = 2
        anchoM = 2
        altoM = 2
        estado = "DISPONIBLE"
        descripcion = "Test"
        clienteId = 5
        direccionOrigen = "Test"
        latitudOrigen = -31.4
        longitudOrigen = -64.1
        direccionDestino = "Test"
        latitudDestino = -31.3
        longitudDestino = -64.2
    }
    ubicacionOrigen = "Test Origen"
    ubicacionDestino = "Test Destino"
    fechaProgramada = "2025-12-20"
    tarifaId = 2
} | ConvertTo-Json -Depth 10

Invoke-RestMethod -Uri "http://localhost:8085/api/solicitudes/diagnostico" `
    -Headers $headers -Method Post -Body $body
```

---

## FIX #2: Implementar Endpoint PATCH para Costos

### 🔴 PRIORIDAD: CRÍTICA

### Problema
El servicio de rutas intenta actualizar costos con `PATCH /api/solicitudes/{id}/costos-tiempos` pero el endpoint no existe.

### Implementación Completa

#### 1. Crear DTO para Actualización

**Archivo**: `solicitudes-service/.../dto/CostosTiemposDTO.java`

```java
package com.logistica.solicitudes.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CostosTiemposDTO {
    
    @NotNull(message = "El costo estimado es obligatorio")
    @Min(value = 0, message = "El costo estimado no puede ser negativo")
    private BigDecimal costoEstimado;
    
    @NotNull(message = "El tiempo estimado es obligatorio")
    @Min(value = 0, message = "El tiempo estimado no puede ser negativo")
    private Integer tiempoEstimadoHoras;
    
    // Opcionales - para cuando se complete
    private BigDecimal costoFinal;
    private Integer tiempoRealHoras;
}
```

#### 2. Agregar Método en Service

**Archivo**: `solicitudes-service/.../service/SolicitudService.java`

```java
@Service
@Slf4j
public class SolicitudService {
    
    /**
     * Actualiza costos y tiempos estimados de una solicitud.
     * Este método es invocado por el servicio de rutas después de calcular la ruta.
     *
     * @param solicitudId ID de la solicitud
     * @param dto Costos y tiempos a actualizar
     * @throws SolicitudNoEncontradaException si la solicitud no existe
     */
    @Transactional
    public void actualizarCostosTiempos(Long solicitudId, CostosTiemposDTO dto) {
        log.info("Actualizando costos y tiempos para solicitud ID: {}", solicitudId);
        
        Solicitud solicitud = solicitudRepository.findById(solicitudId)
                .orElseThrow(() -> new SolicitudNoEncontradaException(
                    "No se encontró la solicitud con ID: " + solicitudId));
        
        // Actualizar solo campos estimados (los finales se actualizan al completar)
        solicitud.setCostoEstimado(dto.getCostoEstimado());
        solicitud.setTiempoEstimadoHoras(dto.getTiempoEstimadoHoras());
        
        // Si vienen costos finales (opcional)
        if (dto.getCostoFinal() != null) {
            solicitud.setCostoFinal(dto.getCostoFinal());
        }
        if (dto.getTiempoRealHoras() != null) {
            solicitud.setTiempoRealHoras(dto.getTiempoRealHoras());
        }
        
        solicitudRepository.save(solicitud);
        
        log.info("✓ Costos y tiempos actualizados: Estimado=${}, {}h", 
            dto.getCostoEstimado(), dto.getTiempoEstimadoHoras());
    }
}
```

#### 3. Crear Endpoint en Controller

**Archivo**: `solicitudes-service/.../controller/SolicitudController.java`

```java
@RestController
@RequestMapping("/api/solicitudes")
public class SolicitudController {
    
    /**
     * Endpoint para que el servicio de rutas actualice costos y tiempos.
     * 
     * Seguridad: Requiere rol SISTEMA (comunicación entre microservicios)
     * o OPERADOR para testing manual.
     */
    @PatchMapping("/{id}/costos-tiempos")
    @PreAuthorize("hasAnyRole('SISTEMA', 'OPERADOR')")
    @Operation(
        summary = "Actualizar costos y tiempos de una solicitud",
        description = "Endpoint interno para que el servicio de rutas actualice estimaciones"
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Actualización exitosa"),
        @ApiResponse(responseCode = "404", description = "Solicitud no encontrada"),
        @ApiResponse(responseCode = "403", description = "Acceso denegado")
    })
    public ResponseEntity<Void> actualizarCostosTiempos(
            @PathVariable Long id,
            @Valid @RequestBody CostosTiemposDTO dto) {
        
        solicitudService.actualizarCostosTiempos(id, dto);
        return ResponseEntity.ok().build();
    }
}
```

#### 4. Actualizar Código en Rutas Service

**Archivo**: `rutas-service/.../service/RutaService.java`

```java
@Service
@Slf4j
public class RutaService {
    
    @Value("${services.solicitudes.url:http://solicitudes-service:8085}")
    private String solicitudesServiceUrl;
    
    private final RestTemplate restTemplate;
    
    private void actualizarCostosSolicitud(Long solicitudId, 
                                          BigDecimal costo, 
                                          Integer tiempoHoras) {
        try {
            String url = solicitudesServiceUrl + "/api/solicitudes/" + 
                        solicitudId + "/costos-tiempos";
            
            Map<String, Object> body = Map.of(
                "costoEstimado", costo,
                "tiempoEstimadoHoras", tiempoHoras
            );
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            
            HttpEntity<Map<String, Object>> request = new HttpEntity<>(body, headers);
            
            // Usar PATCH correctamente
            restTemplate.exchange(
                url,
                HttpMethod.PATCH,
                request,
                Void.class
            );
            
            log.info("✓ Costos actualizados en solicitud {}: ${} - {}h", 
                solicitudId, costo, tiempoHoras);
            
        } catch (Exception e) {
            log.error("❌ Error al actualizar costos de solicitud {}: {}", 
                solicitudId, e.getMessage());
            // No lanzar excepción - la ruta ya está guardada
            // Solo loggear el error
        }
    }
}
```

#### 5. Configurar RestTemplate para PATCH

**Archivo**: `rutas-service/.../config/RestTemplateConfig.java`

```java
@Configuration
public class RestTemplateConfig {
    
    @Bean
    public RestTemplate restTemplate(RestTemplateBuilder builder) {
        return builder
                .requestFactory(() -> {
                    SimpleClientHttpRequestFactory factory = 
                        new SimpleClientHttpRequestFactory();
                    factory.setConnectTimeout(5000);
                    factory.setReadTimeout(5000);
                    return factory;
                })
                .build();
    }
}
```

### Testing del Fix

```powershell
# Test directo del endpoint PATCH
$headers = @{ 
    Authorization = "Bearer $TOKEN"
    "Content-Type" = "application/json"
}

$body = @{
    costoEstimado = 15000.50
    tiempoEstimadoHoras = 5
} | ConvertTo-Json

Invoke-RestMethod -Uri "http://localhost:8085/api/solicitudes/13/costos-tiempos" `
    -Headers $headers -Method Patch -Body $body

# Verificar actualización
$solicitud = Invoke-RestMethod -Uri "http://localhost:8085/api/solicitudes/13" `
    -Headers $headers

Write-Host "Costo Estimado: $($solicitud.costoEstimado)"
Write-Host "Tiempo Estimado: $($solicitud.tiempoEstimadoHoras)h"
```

---

## FIX #3: Validación de Rutas Duplicadas

### 🔴 PRIORIDAD: ALTA

### Problema
Si se intenta calcular una ruta dos veces para la misma solicitud, falla con duplicate key error.

### Implementación

#### 1. Agregar Método de Validación en Repository

**Archivo**: `rutas-service/.../repository/RutaRepository.java`

```java
@Repository
public interface RutaRepository extends JpaRepository<Ruta, Long> {
    
    Optional<Ruta> findBySolicitudId(Long solicitudId);
    
    boolean existsBySolicitudId(Long solicitudId);
    
    @Query("SELECT r FROM Ruta r WHERE r.solicitudId = :solicitudId " +
           "AND r.activo = true")
    Optional<Ruta> findRutaActivaBySolicitudId(@Param("solicitudId") Long solicitudId);
}
```

#### 2. Validar en Service antes de Calcular

**Archivo**: `rutas-service/.../service/RutaService.java`

```java
@Service
@Slf4j
public class RutaService {
    
    @Transactional
    public RutaResponseDTO calcularRutaTentativa(Long solicitudId) {
        log.info("Calculando ruta tentativa para solicitud ID: {}", solicitudId);
        
        // VALIDACIÓN: Verificar si ya existe una ruta
        if (rutaRepository.existsBySolicitudId(solicitudId)) {
            log.warn("La solicitud {} ya tiene una ruta calculada", solicitudId);
            throw new RutaYaExisteException(
                "La solicitud " + solicitudId + " ya tiene una ruta calculada. " +
                "Use el endpoint de recálculo si desea actualizarla."
            );
        }
        
        // Continuar con el cálculo normal...
        // ...
    }
    
    /**
     * Recalcula una ruta existente (elimina la anterior y crea nueva)
     */
    @Transactional
    public RutaResponseDTO recalcularRuta(Long solicitudId) {
        log.info("Recalculando ruta para solicitud ID: {}", solicitudId);
        
        // Eliminar ruta anterior si existe
        rutaRepository.findBySolicitudId(solicitudId).ifPresent(ruta -> {
            log.info("Eliminando ruta anterior ID: {}", ruta.getId());
            rutaRepository.delete(ruta);
        });
        
        // Calcular nueva ruta
        return calcularRutaTentativa(solicitudId);
    }
}
```

#### 3. Crear Excepción Custom

**Archivo**: `rutas-service/.../exception/RutaYaExisteException.java`

```java
package com.logistica.rutas.exception;

public class RutaYaExisteException extends RuntimeException {
    
    public RutaYaExisteException(String message) {
        super(message);
    }
    
    public RutaYaExisteException(String message, Throwable cause) {
        super(message, cause);
    }
}
```

#### 4. Agregar Endpoint de Recálculo

**Archivo**: `rutas-service/.../controller/RutaController.java`

```java
@RestController
@RequestMapping("/api/rutas")
public class RutaController {
    
    @PostMapping("/calcular/{solicitudId}")
    @PreAuthorize("hasAnyRole('OPERADOR', 'SISTEMA')")
    @Operation(summary = "Calcular ruta para una solicitud")
    public ResponseEntity<RutaResponseDTO> calcularRuta(@PathVariable Long solicitudId) {
        RutaResponseDTO ruta = rutaService.calcularRutaTentativa(solicitudId);
        return ResponseEntity.status(HttpStatus.CREATED).body(ruta);
    }
    
    @PostMapping("/recalcular/{solicitudId}")
    @PreAuthorize("hasRole('OPERADOR')")
    @Operation(
        summary = "Recalcular ruta existente",
        description = "Elimina la ruta anterior y calcula una nueva"
    )
    public ResponseEntity<RutaResponseDTO> recalcularRuta(@PathVariable Long solicitudId) {
        RutaResponseDTO ruta = rutaService.recalcularRuta(solicitudId);
        return ResponseEntity.ok(ruta);
    }
    
    @GetMapping("/solicitud/{solicitudId}")
    @Operation(summary = "Obtener ruta por solicitud")
    public ResponseEntity<RutaResponseDTO> obtenerPorSolicitud(
            @PathVariable Long solicitudId) {
        return rutaService.obtenerPorSolicitudId(solicitudId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}
```

#### 5. Exception Handler Global

**Archivo**: `rutas-service/.../exception/GlobalExceptionHandler.java`

```java
@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {
    
    @ExceptionHandler(RutaYaExisteException.class)
    public ResponseEntity<ErrorResponse> handleRutaYaExiste(
            RutaYaExisteException ex,
            WebRequest request) {
        
        log.warn("Ruta ya existe: {}", ex.getMessage());
        
        ErrorResponse error = ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.CONFLICT.value())
                .error("Conflict")
                .message(ex.getMessage())
                .path(request.getDescription(false).replace("uri=", ""))
                .build();
        
        return ResponseEntity.status(HttpStatus.CONFLICT).body(error);
    }
}
```

### Testing del Fix

```powershell
# 1. Calcular ruta primera vez
POST http://localhost:8086/api/rutas/calcular/13
Response: 201 CREATED

# 2. Intentar calcular de nuevo (debe fallar con 409)
POST http://localhost:8086/api/rutas/calcular/13
Response: 409 CONFLICT
Body: { "message": "La solicitud 13 ya tiene una ruta calculada..." }

# 3. Recalcular (debe funcionar)
POST http://localhost:8086/api/rutas/recalcular/13
Response: 200 OK
```

---

## FIX #4: Tarifas Inactivas en Datos de Prueba

### 🟠 PRIORIDAD: MEDIA

### Problema
El script `data-test.sql` no activa correctamente las tarifas COSTO_KM_BASE.

### Solución

**Archivo**: `tarifas-service/src/main/resources/data-test.sql`

```sql
-- Limpiar datos existentes
DELETE FROM tarifas;

-- Resetear secuencia
ALTER SEQUENCE tarifas_id_seq RESTART WITH 1;

-- Insertar tarifas ACTIVAS
INSERT INTO tarifas (
    tipo, descripcion, valor, unidad,
    rango_peso_min_kg, rango_peso_max_kg,
    rango_volumen_minm3, rango_volumen_maxm3,
    vigencia_desde, vigencia_hasta,
    activo, fecha_creacion, fecha_actualizacion
) VALUES 
-- TARIFAS PRINCIPALES (ACTIVAS)
('COSTO_KM_BASE', 'Costo base por kilómetro', 1435.99, '$/km', 
 NULL, NULL, NULL, NULL, 
 '2025-01-01', NULL, 
 true, NOW(), NOW()),  -- ← ACTIVA

('COSTO_COMBUSTIBLE_LITRO', 'Precio combustible por litro', 950.00, '$/litro',
 NULL, NULL, NULL, NULL,
 '2025-01-01', NULL,
 true, NOW(), NOW()),  -- ← ACTIVA

('ESTADIA_DEPOSITO', 'Costo de estadía por día en depósito', 5000.00, '$/día',
 NULL, NULL, NULL, NULL,
 '2025-01-01', NULL,
 true, NOW(), NOW()),  -- ← ACTIVA

('CARGO_GESTION_FIJO', 'Cargo fijo de gestión por envío', 8000.00, '$ fijo',
 NULL, NULL, NULL, NULL,
 '2025-01-01', NULL,
 true, NOW(), NOW()),  -- ← ACTIVA

-- TARIFAS POR RANGO DE PESO (ACTIVAS)
('COSTO_KM_BASE', 'Tarifa contenedores pequeños (0-10 ton)', 80.00, '$/km',
 0, 10000, NULL, NULL,
 '2025-01-01', NULL,
 true, NOW(), NOW()),

('COSTO_KM_BASE', 'Tarifa contenedores medianos (10-20 ton)', 120.00, '$/km',
 10000, 20000, NULL, NULL,
 '2025-01-01', NULL,
 true, NOW(), NOW()),

('COSTO_KM_BASE', 'Tarifa contenedores grandes (20-35 ton)', 180.00, '$/km',
 20000, 35000, NULL, NULL,
 '2025-01-01', NULL,
 true, NOW(), NOW());

-- Verificar inserción
SELECT 
    id, 
    tipo, 
    valor, 
    activo,
    CASE WHEN activo THEN '✓ ACTIVA' ELSE '✗ INACTIVA' END as estado
FROM tarifas
ORDER BY id;
```

### Validación Automática al Iniciar Servicio

**Archivo**: `tarifas-service/.../service/TarifaValidationService.java`

```java
@Service
@Slf4j
public class TarifaValidationService {
    
    private final TarifaRepository tarifaRepository;
    
    @EventListener(ApplicationReadyEvent.class)
    public void validarTarifasMinimasAlIniciar() {
        log.info("=== VALIDANDO TARIFAS MÍNIMAS REQUERIDAS ===");
        
        // Verificar tarifa COSTO_KM_BASE activa
        boolean existeCostoKmBase = tarifaRepository.existsByTipoAndActivo(
            TipoTarifa.COSTO_KM_BASE, true);
        
        if (!existeCostoKmBase) {
            log.error("❌ NO SE ENCONTRÓ TARIFA COSTO_KM_BASE ACTIVA");
            log.error("El sistema requiere al menos una tarifa COSTO_KM_BASE activa");
            log.error("Por favor, active una tarifa o corrija data-test.sql");
            
            // Opción: Crear tarifa por defecto
            crearTarifaPorDefecto();
        } else {
            log.info("✓ Tarifa COSTO_KM_BASE activa encontrada");
        }
        
        log.info("=== VALIDACIÓN COMPLETADA ===");
    }
    
    private void crearTarifaPorDefecto() {
        log.warn("Creando tarifa COSTO_KM_BASE por defecto...");
        
        Tarifa tarifa = Tarifa.builder()
                .tipo(TipoTarifa.COSTO_KM_BASE)
                .descripcion("Tarifa por defecto (generada automáticamente)")
                .valor(BigDecimal.valueOf(100.00))
                .unidad("$/km")
                .vigenciaDesde(LocalDate.now())
                .activo(true)
                .build();
        
        tarifaRepository.save(tarifa);
        log.info("✓ Tarifa por defecto creada - ID: {}", tarifa.getId());
    }
}
```

---

## FIX #5: API Gateway Circuit Breaker

### 🟠 PRIORIDAD: MEDIA

### Problema
API Gateway retorna 503 Service Unavailable de forma intermitente.

### Implementación Resilience4j

**Archivo**: `api-gateway/src/main/resources/application.yml`

```yaml
spring:
  cloud:
    gateway:
      routes:
        - id: clientes-service
          uri: http://clientes-service:8081
          predicates:
            - Path=/api/clientes/**
          filters:
            - name: CircuitBreaker
              args:
                name: clientesCircuit
                fallbackUri: forward:/fallback/clientes
            - name: Retry
              args:
                retries: 3
                statuses: BAD_GATEWAY,SERVICE_UNAVAILABLE
                methods: GET
                backoff:
                  firstBackoff: 50ms
                  maxBackoff: 500ms
                  factor: 2
                  basedOnPreviousValue: false

resilience4j:
  circuitbreaker:
    configs:
      default:
        slidingWindowSize: 10
        minimumNumberOfCalls: 5
        permittedNumberOfCallsInHalfOpenState: 3
        automaticTransitionFromOpenToHalfOpenEnabled: true
        waitDurationInOpenState: 10s
        failureRateThreshold: 50
        slowCallRateThreshold: 50
        slowCallDurationThreshold: 2s
        recordExceptions:
          - org.springframework.web.client.HttpServerErrorException
          - java.io.IOException
          - java.util.concurrent.TimeoutException
    instances:
      clientesCircuit:
        baseConfig: default
      depositosCircuit:
        baseConfig: default
      camionesCircuit:
        baseConfig: default
      tarifasCircuit:
        baseConfig: default
      solicitudesCircuit:
        baseConfig: default
      rutasCircuit:
        baseConfig: default

  timelimiter:
    configs:
      default:
        timeoutDuration: 5s
    instances:
      clientesCircuit:
        baseConfig: default

logging:
  level:
    org.springframework.cloud.gateway: DEBUG
    io.github.resilience4j: DEBUG
```

### Fallback Controller

**Archivo**: `api-gateway/.../controller/FallbackController.java`

```java
@RestController
@RequestMapping("/fallback")
@Slf4j
public class FallbackController {
    
    @GetMapping("/clientes")
    public ResponseEntity<Map<String, Object>> clientesFallback() {
        log.warn("Circuit breaker activado para clientes-service");
        
        Map<String, Object> response = Map.of(
            "error", "Service Temporarily Unavailable",
            "message", "El servicio de clientes no está disponible. Intente nuevamente en unos momentos.",
            "timestamp", LocalDateTime.now().toString(),
            "suggestion", "Puede intentar acceder directamente en http://localhost:8081"
        );
        
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(response);
    }
    
    // Repetir para cada servicio...
}
```

---

## Scripts SQL de Diagnóstico

### Script 1: Verificar Estado General

```sql
-- Ejecutar en PostgreSQL
psql -U logistica

-- Verificar todas las bases de datos
\l

-- Conectar a cada base y verificar
\c clientes_db
SELECT 'CLIENTES' as servicio, COUNT(*) as total FROM clientes;

\c depositos_db
SELECT 'DEPOSITOS' as servicio, COUNT(*) as total FROM depositos;

\c camiones_db
SELECT 'CAMIONES' as servicio, COUNT(*) as total FROM camiones;

\c tarifas_db
SELECT 'TARIFAS' as servicio, COUNT(*) as total FROM tarifas;
SELECT 'TARIFAS ACTIVAS' as servicio, COUNT(*) as total FROM tarifas WHERE activo = true;

\c solicitudes_db
SELECT 'SOLICITUDES' as servicio, COUNT(*) as total FROM solicitudes;
SELECT 'CONTENEDORES' as servicio, COUNT(*) as total FROM contenedores;

\c rutas_db
SELECT 'RUTAS' as servicio, COUNT(*) as total FROM rutas;
SELECT 'TRAMOS' as servicio, COUNT(*) as total FROM tramos;
```

### Script 2: Diagnóstico de Solicitudes

```sql
\c solicitudes_db

-- Ver constraint de estados
SELECT 
    constraint_name, 
    check_clause
FROM information_schema.check_constraints 
WHERE constraint_schema = 'public' 
  AND constraint_name LIKE '%solicitudes%';

-- Ver solicitudes con problemas
SELECT 
    id,
    numero,
    estado,
    LENGTH(estado::text) as longitud_estado,
    ASCII(SUBSTRING(estado::text, 1, 1)) as primer_char_ascii,
    cliente_id,
    contenedor_id,
    activo
FROM solicitudes
WHERE estado NOT IN ('PENDIENTE', 'PROGRAMADA', 'EN_TRANSITO', 'ENTREGADA', 'CANCELADA');

-- Ver contenedores sin solicitud (disponibles)
SELECT 
    c.id,
    c.identificacion,
    c.estado,
    c.cliente_id,
    CASE WHEN s.id IS NULL THEN 'DISPONIBLE' ELSE 'ASIGNADO' END as asignacion
FROM contenedores c
LEFT JOIN solicitudes s ON c.id = s.contenedor_id
WHERE c.activo = true;
```

### Script 3: Limpiar Datos de Prueba

```sql
-- ADVERTENCIA: Esto eliminará TODOS los datos de prueba

\c solicitudes_db
DELETE FROM solicitudes;
DELETE FROM contenedores;
ALTER SEQUENCE solicitudes_id_seq RESTART WITH 1;
ALTER SEQUENCE contenedores_id_seq RESTART WITH 1;

\c rutas_db
DELETE FROM tramos;
DELETE FROM rutas;
ALTER SEQUENCE rutas_id_seq RESTART WITH 1;
ALTER SEQUENCE tramos_id_seq RESTART WITH 1;

-- Reiniciar servicios para recargar datos
-- docker-compose restart solicitudes-service rutas-service
```

---

## Configuración de Logging

### application.yml para Todos los Servicios

```yaml
logging:
  level:
    root: INFO
    com.logistica: DEBUG
    org.springframework.web: DEBUG
    org.springframework.security: DEBUG
    org.hibernate.SQL: DEBUG
    org.hibernate.type.descriptor.sql.BasicBinder: TRACE
  pattern:
    console: "%clr(%d{yyyy-MM-dd HH:mm:ss.SSS}){faint} %clr(%5p) %clr(${PID:- }){magenta} %clr(---){faint} %clr([%15.15t]){faint} %clr(%-40.40logger{39}){cyan} %clr(:){faint} %m%n%wEx"
    file: "%d{yyyy-MM-dd HH:mm:ss.SSS} %5p ${PID:- } --- [%t] %-40.40logger{39} : %m%n%wEx"
  file:
    name: /app/logs/service.log
    max-size: 10MB
    max-history: 30
    total-size-cap: 1GB
```

### Habilitar Logs por Docker Compose

```yaml
# docker-compose.yml
services:
  solicitudes-service:
    environment:
      SPRING_PROFILES_ACTIVE: docker
      LOGGING_LEVEL_COM_LOGISTICA: DEBUG
      LOGGING_LEVEL_ORG_HIBERNATE_SQL: DEBUG
    volumes:
      - ./logs/solicitudes:/app/logs
```

---

## Checklist de Testing Post-Fix

- [ ] ✅ Rebuild de todos los servicios modificados
- [ ] ✅ Reiniciar contenedores afectados
- [ ] ✅ Verificar logs de inicio sin errores
- [ ] ✅ Probar endpoint de diagnóstico en solicitudes
- [ ] ✅ Crear solicitud vía API (debe funcionar)
- [ ] ✅ Calcular ruta (debe funcionar)
- [ ] ✅ Verificar que costos se actualizan en solicitud
- [ ] ✅ Intentar calcular ruta duplicada (debe fallar con 409)
- [ ] ✅ Probar recálculo de ruta (debe funcionar)
- [ ] ✅ Verificar circuit breaker con servicio caído
- [ ] ✅ Ejecutar flujo E2E completo sin workarounds

---

**Última actualización**: 15 de Noviembre de 2025  
**Versión**: 1.0  
**Autor**: GitHub Copilot (Claude Sonnet 4.5)
