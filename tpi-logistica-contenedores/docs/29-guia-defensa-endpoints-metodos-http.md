# Guía para Defensa: Endpoints, Métodos HTTP y Cálculos de Tarifas

## Índice
1. [Roles y Seguridad](#roles-y-seguridad)
2. [Eliminación Lógica vs Física](#eliminación-lógica-vs-física)
3. [Métodos HTTP y su Correcta Aplicación](#métodos-http-y-su-correcta-aplicación)
4. [Análisis de Endpoints por Servicio](#análisis-de-endpoints-por-servicio)
5. [Cálculo de Tarifas y Consumos](#cálculo-de-tarifas-y-consumos)
6. [Atributos Utilizados en Cálculos](#atributos-utilizados-en-cálculos)
7. [Validación de Métodos HTTP](#validación-de-métodos-http)
8. [Preguntas Frecuentes en Defensa](#preguntas-frecuentes-en-defensa)

---

## Roles y Seguridad

### 🔐 Roles Implementados (Keycloak)

El sistema implementa **3 roles** según lo requerido:

| Rol | Descripción | Permisos Principales |
|-----|-------------|---------------------|
| **CLIENTE** | Usuario que solicita traslados | - Crear solicitudes<br>- Ver sus propias solicitudes<br>- Seguimiento de contenedores |
| **OPERADOR** | Administrador del sistema | - Gestionar clientes, depósitos, camiones<br>- Gestionar tarifas<br>- Ver todas las solicitudes<br>- Asignar camiones a tramos<br>- **Eliminar recursos (soft delete)** |
| **TRANSPORTISTA** | Conductor de camiones | - Ver tramos asignados a su camión<br>- Iniciar tramos<br>- Finalizar tramos<br>- Registrar tiempos y costos reales |

**⚠️ Nota Importante:** El sistema **NO usa rol ADMIN**. Todas las operaciones administrativas las realiza el **OPERADOR**.

### Ejemplos de Seguridad

```java
// Solo OPERADOR puede crear tarifas
@PostMapping
@PreAuthorize("hasRole('OPERADOR')")
public ResponseEntity<TarifaResponseDTO> crearTarifa(@Valid @RequestBody TarifaRequestDTO request)

// Solo OPERADOR puede eliminar clientes
@DeleteMapping("/{id}")
@PreAuthorize("hasRole('OPERADOR')")
public ResponseEntity<Void> eliminarCliente(@PathVariable Long id)

// TRANSPORTISTA puede iniciar tramos
@PostMapping("/tramos/{tramoId}/iniciar")
@PreAuthorize("hasRole('TRANSPORTISTA')")
public ResponseEntity<TramoResponseDTO> iniciarTramo(@PathVariable Long tramoId)

// CLIENTE puede ver sus solicitudes
@GetMapping("/cliente/{clienteId}")
@PreAuthorize("hasAnyRole('CLIENTE', 'OPERADOR')")
public ResponseEntity<List<SolicitudResponseDTO>> listarPorCliente(@PathVariable Long clienteId)
```

---

## Eliminación Lógica vs Física

### 🗑️ Estrategia de Eliminación

El sistema implementa **eliminación lógica (soft delete)** para todos los recursos:

| Tipo | Descripción | Implementación |
|------|-------------|----------------|
| **Eliminación Lógica (Soft Delete)** | El registro permanece en la BD | `setActivo(false)` |
| **Eliminación Física (Hard Delete)** | El registro se elimina de la BD | ❌ **NO usada** |

### Ventajas de Soft Delete

✅ **Auditoría:** Mantiene historial completo  
✅ **Recuperación:** Permite reactivar registros  
✅ **Integridad referencial:** No rompe relaciones existentes  
✅ **Análisis:** Permite estudiar datos históricos  

### Implementación Completa

**1. Modelo con campo `activo`:**
```java
@Entity
public class Cliente {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    private String nombre;
    private String email;
    
    @Column(nullable = false)
    private Boolean activo = true;  // ← Campo para soft delete
    
    // ... otros campos
}
```

**2. Repository con filtros:**
```java
@Repository
public interface ClienteRepository extends JpaRepository<Cliente, Long> {
    // Solo registros activos
    List<Cliente> findByActivo(Boolean activo);
    
    // Buscar por email solo si está activo
    Optional<Cliente> findByEmailAndActivoTrue(String email);
}
```

**3. Service - Método eliminar:**
```java
@Transactional
public void eliminarCliente(Long id) {
    log.info("Eliminando cliente con ID: {}", id);
    
    Cliente cliente = clienteRepository.findById(id)
            .orElseThrow(() -> new ClienteNotFoundException("Cliente no encontrado"));
    
    // Soft delete: NO usa deleteById()
    cliente.setActivo(false);
    clienteRepository.save(cliente);
    
    log.info("Cliente eliminado (desactivado) exitosamente con ID: {}", id);
}
```

**4. Service - Listar solo activos:**
```java
@Transactional(readOnly = true)
public List<ClienteResponseDTO> obtenerTodosLosClientes() {
    log.info("Obteniendo todos los clientes activos");
    
    // Filtra solo activos
    return clienteRepository.findByActivo(true).stream()
            .map(this::mapToResponseDTO)
            .collect(Collectors.toList());
}
```

**5. Controller:**
```java
@DeleteMapping("/{id}")
@PreAuthorize("hasRole('OPERADOR')")
@Operation(summary = "Eliminar cliente", 
           description = "Desactiva un cliente del sistema (eliminación lógica)")
public ResponseEntity<Void> eliminarCliente(@PathVariable Long id) {
    clienteService.eliminarCliente(id);
    return ResponseEntity.noContent().build();
}
```

### Comparación Antes/Después

| Operación | ❌ Antes (Hard Delete) | ✅ Ahora (Soft Delete) |
|-----------|----------------------|----------------------|
| **Eliminar** | `repository.deleteById(id)` | `entity.setActivo(false)` |
| **Listar** | `repository.findAll()` | `repository.findByActivo(true)` |
| **Buscar** | `repository.findById(id)` | `repository.findByIdAndActivoTrue(id)` |
| **En BD** | Registro desaparece | Registro con `activo=false` |
| **Recuperar** | ❌ Imposible | ✅ Posible con `setActivo(true)` |

### Ejemplo Real

```sql
-- Antes de DELETE /api/clientes/5
SELECT * FROM clientes WHERE id = 5;
| id | nombre | email           | activo |
|----|--------|-----------------|--------|
| 5  | Juan   | juan@email.com  | true   |

-- Después de DELETE /api/clientes/5
SELECT * FROM clientes WHERE id = 5;
| id | nombre | email           | activo |
|----|--------|-----------------|--------|
| 5  | Juan   | juan@email.com  | false  | ← Registro permanece

-- GET /api/clientes (solo retorna activos)
SELECT * FROM clientes WHERE activo = true;
| id | nombre | email             | activo |
|----|--------|-------------------|--------|
| 1  | María  | maria@email.com   | true   |
| 2  | Pedro  | pedro@email.com   | true   |
-- Cliente ID 5 NO aparece en el listado
```

### Reactivación

```java
@Transactional
public void activarCliente(Long id) {
    Cliente cliente = clienteRepository.findById(id)
            .orElseThrow(() -> new ClienteNotFoundException("Cliente no encontrado"));
    
    cliente.setActivo(true);
    clienteRepository.save(cliente);
    
    log.info("Cliente reactivado con ID: {}", id);
}
```

---

## Métodos HTTP y su Correcta Aplicación

### 📚 Conceptos Fundamentales

| Método | Propósito | Idempotente | Safe | Uso Correcto |
|--------|-----------|-------------|------|--------------|
| **GET** | Obtener recursos | ✅ Sí | ✅ Sí | Consultar datos sin modificarlos |
| **POST** | Crear recursos | ❌ No | ❌ No | Crear nuevas entidades |
| **PUT** | Reemplazar recurso completo | ✅ Sí | ❌ No | Actualizar todos los campos |
| **PATCH** | Actualización parcial | ❌ No | ❌ No | Modificar algunos campos específicos |
| **DELETE** | Eliminar recurso | ✅ Sí | ❌ No | Desactivar o eliminar entidades |

**Definiciones:**
- **Idempotente:** Ejecutar la operación múltiples veces produce el mismo resultado
- **Safe:** No modifica el estado del servidor

---

## Análisis de Endpoints por Servicio

### 1. Tarifas Service (`/api/tarifas`)

#### GET `/api/tarifas` - Listar todas las tarifas
**✅ Método Correcto:** GET  
**Justificación:** Solo consulta datos sin modificar estado  
**Controller:**
```java
@GetMapping
@PreAuthorize("hasRole('OPERADOR')")
public ResponseEntity<List<TarifaResponseDTO>> listarTodas() {
    List<TarifaResponseDTO> tarifas = tarifaService.listarTodas();
    return ResponseEntity.ok(tarifas);
}
```

**Service:**
```java
@Transactional(readOnly = true)
public List<TarifaResponseDTO> listarTodas() {
    log.info("Listando todas las tarifas activas");
    return tarifaRepository.findByActivoTrue().stream()
            .map(this::mapToResponseDTO)
            .collect(Collectors.toList());
}
```

**¿Por qué GET?**
- No modifica datos
- Es safe e idempotente
- Retorna lista de recursos
- `@Transactional(readOnly = true)` confirma que es solo lectura

---

#### POST `/api/tarifas` - Crear nueva tarifa
**✅ Método Correcto:** POST  
**Justificación:** Crea un nuevo recurso en el servidor  
**Controller:**
```java
@PostMapping
@PreAuthorize("hasRole('OPERADOR')")
@Operation(summary = "Crear una nueva tarifa")
public ResponseEntity<TarifaResponseDTO> crearTarifa(@Valid @RequestBody TarifaRequestDTO request) {
    TarifaResponseDTO response = tarifaService.crearTarifa(request);
    return ResponseEntity.status(HttpStatus.CREATED).body(response);
}
```

**Service:**
```java
@Transactional
public TarifaResponseDTO crearTarifa(TarifaRequestDTO request) {
    Tarifa tarifa = new Tarifa();
    tarifa.setTipo(request.getTipo());
    tarifa.setValor(request.getValor());
    // ... más campos
    Tarifa saved = tarifaRepository.save(tarifa);
    return mapToResponseDTO(saved);
}
```

**¿Por qué POST?**
- Crea un nuevo recurso (tarifa)
- Genera nuevo ID automáticamente
- No es idempotente (ejecutar 2 veces = 2 tarifas)
- Retorna `201 CREATED`

---

#### GET `/api/tarifas/{id}` - Obtener tarifa por ID
**✅ Método Correcto:** GET  
**Justificación:** Solo consulta un recurso específico  
**Controller:**
```java
@GetMapping("/{id}")
@PreAuthorize("hasRole('OPERADOR')")
public ResponseEntity<TarifaResponseDTO> obtenerPorId(@PathVariable Long id) {
    TarifaResponseDTO tarifa = tarifaService.obtenerPorId(id);
    return ResponseEntity.ok(tarifa);
}
```

**¿Por qué GET con path variable?**
- Identifica recurso único por ID
- No modifica estado
- `@PathVariable Long id` extrae el ID de la URL

---

#### PUT `/api/tarifas/{id}` - Actualizar tarifa completa
**✅ Método Correcto:** PUT  
**Justificación:** Reemplaza todos los campos de la tarifa  
**Controller:**
```java
@PutMapping("/{id}")
@PreAuthorize("hasRole('OPERADOR')")
public ResponseEntity<TarifaResponseDTO> actualizarTarifa(
        @PathVariable Long id,
        @Valid @RequestBody TarifaRequestDTO request) {
    TarifaResponseDTO response = tarifaService.actualizarTarifa(id, request);
    return ResponseEntity.ok(response);
}
```

**Service:**
```java
@Transactional
public TarifaResponseDTO actualizarTarifa(Long id, TarifaRequestDTO request) {
    Tarifa tarifa = tarifaRepository.findById(id)
            .orElseThrow(() -> new TarifaNotFoundException("Tarifa no encontrada"));
    
    // Actualiza TODOS los campos
    tarifa.setTipo(request.getTipo());
    tarifa.setDescripcion(request.getDescripcion());
    tarifa.setValor(request.getValor());
    tarifa.setUnidad(request.getUnidad());
    // ... todos los campos
    
    return mapToResponseDTO(tarifaRepository.save(tarifa));
}
```

**¿Por qué PUT y no PATCH?**
- PUT: Reemplaza el recurso completo
- Requiere enviar TODOS los campos
- Es idempotente (ejecutar 2 veces = mismo resultado)

---

#### DELETE `/api/tarifas/{id}` - Eliminar tarifa
**✅ Método Correcto:** DELETE  
**Justificación:** Desactiva el recurso (soft delete)  
**Controller:**
```java
@DeleteMapping("/{id}")
@PreAuthorize("hasRole('OPERADOR')")
public ResponseEntity<Void> eliminarTarifa(@PathVariable Long id) {
    tarifaService.eliminarTarifa(id);
    return ResponseEntity.noContent().build();
}
```

**Service:**
```java
@Transactional
public void eliminarTarifa(Long id) {
    Tarifa tarifa = tarifaRepository.findById(id)
            .orElseThrow(() -> new TarifaNotFoundException("Tarifa no encontrada"));
    
    tarifa.setActivo(false); // Soft delete
    tarifaRepository.save(tarifa);
}
```

**¿Por qué DELETE?**
- Indica intención de eliminar recurso
- Es idempotente (eliminar 2 veces = mismo resultado)
- Retorna `204 NO CONTENT`
- Implementa soft delete (setActivo(false))

---

### 2. Solicitudes Service (`/api/solicitudes`)

#### POST `/api/solicitudes` - Crear solicitud
**✅ Método Correcto:** POST  
**Justificación:** Crea solicitud + contenedor + calcula ruta automáticamente  
**Controller:**
```java
@PostMapping
public ResponseEntity<SolicitudResponseDTO> crearSolicitud(@Valid @RequestBody SolicitudRequestDTO request) {
    SolicitudResponseDTO response = solicitudService.crearSolicitud(request);
    return ResponseEntity.status(HttpStatus.CREATED).body(response);
}
```

**Flujo completo:**
1. Crea Solicitud en `solicitudes-service`
2. Crea Contenedor asociado
3. Llama a `rutas-service` para calcular ruta tentativa
4. Actualiza solicitud con costos y tiempos estimados

**¿Por qué POST?**
- Crea múltiples recursos (solicitud + contenedor)
- Dispara proceso complejo (cálculo de ruta)
- No es idempotente (múltiples solicitudes con mismo body = solicitudes diferentes)

---

#### PATCH `/api/solicitudes/{id}/estado` - Actualizar estado
**✅ Método Correcto:** PATCH  
**Justificación:** Actualiza solo el campo `estado`, no toda la solicitud  
**Controller:**
```java
@PatchMapping("/{id}/estado")
public ResponseEntity<SolicitudResponseDTO> actualizarEstado(
        @PathVariable Long id,
        @RequestParam EstadoSolicitud estado) {
    SolicitudResponseDTO response = solicitudService.actualizarEstado(id, estado);
    return ResponseEntity.ok(response);
}
```

**¿Por qué PATCH y no PUT?**
- Solo modifica el campo `estado`
- No requiere enviar todos los campos de la solicitud
- Usa `@RequestParam` para pasar el nuevo estado

---

#### PATCH `/api/solicitudes/{id}/costos-tiempos` - Actualizar costos
**✅ Método Correcto:** PATCH  
**Justificación:** Actualiza solo campos de costos y tiempos  
**Controller:**
```java
@PatchMapping("/{id}/costos-tiempos")
@Operation(summary = "Actualizar costos y tiempos de solicitud")
public ResponseEntity<SolicitudResponseDTO> actualizarCostosYTiempos(
        @PathVariable Long id,
        @RequestBody ActualizarCostosDTO actualizacion) {
    SolicitudResponseDTO response = solicitudService.actualizarCostosYTiempos(id, actualizacion);
    return ResponseEntity.ok(response);
}
```

**DTO usado:**
```java
@Data
public class ActualizarCostosDTO {
    private BigDecimal costoEstimado;
    private BigDecimal costoFinal;
    private Integer tiempoEstimadoHoras;
    private Integer tiempoRealHoras;
    private Long rutaId;
}
```

**¿Por qué PATCH?**
- Actualización parcial (solo 5 campos de 20+ campos totales)
- Endpoint interno para sincronización entre servicios
- No requiere validación completa de solicitud

---

### 3. Rutas Service (`/api/rutas`)

#### POST `/api/rutas/calcular` - Calcular ruta tentativa
**✅ Método Correcto:** POST  
**Justificación:** Crea una nueva ruta con todos sus tramos  
**Controller:**
```java
@PostMapping("/calcular")
@Operation(summary = "Calcular ruta tentativa")
public ResponseEntity<RutaResponseDTO> calcularRutaTentativa(@Valid @RequestBody RutaRequestDTO request) {
    RutaResponseDTO response = rutaService.calcularRutaTentativa(request);
    return ResponseEntity.status(HttpStatus.CREATED).body(response);
}
```

**Proceso complejo:**
1. Obtiene solicitud desde `solicitudes-service`
2. Calcula distancia con Google Maps API
3. Obtiene tarifa desde `tarifas-service`
4. Calcula tiempo desde Google Maps API
5. Calcula costo: `distancia × tarifa.valor`
6. Crea ruta y tramos
7. Actualiza solicitud con costos

**¿Por qué POST?**
- Crea recurso nuevo (ruta + tramos)
- Ejecuta cálculos complejos
- No es idempotente (cada llamada puede generar ruta diferente si cambian las tarifas)

---

#### POST `/api/rutas/tramos/{tramoId}/asignar-camion` - Asignar camión
**✅ Método Correcto:** POST  
**Justificación:** Cambia estado del tramo de ESTIMADO → ASIGNADO  
**Controller:**
```java
@PostMapping("/tramos/{tramoId}/asignar-camion")
public ResponseEntity<TramoResponseDTO> asignarCamionATramo(
        @PathVariable Long tramoId,
        @Valid @RequestBody AsignarCamionDTO request) {
    TramoResponseDTO response = rutaService.asignarCamionATramo(tramoId, request);
    return ResponseEntity.ok(response);
}
```

**Service:**
```java
@Transactional
public TramoResponseDTO asignarCamionATramo(Long tramoId, AsignarCamionDTO request) {
    Tramo tramo = tramoRepository.findById(tramoId)
        .orElseThrow(() -> new TramoNotFoundException("Tramo no encontrado"));
    
    validarCamionDisponible(request.getCamionId());
    
    tramo.setCamionId(request.getCamionId());
    tramo.setEstado(EstadoTramo.ASIGNADO);
    
    return mapTramoToResponseDTO(tramoRepository.save(tramo));
}
```

**¿Por qué POST y no PATCH?**
- Representa una **acción** (asignar), no solo actualizar un campo
- Modifica estado + valida disponibilidad del camión
- Sigue convención REST de acciones complejas: `POST /recurso/{id}/accion`

---

#### POST `/api/rutas/tramos/{tramoId}/iniciar` - Iniciar tramo
**✅ Método Correcto:** POST  
**Justificación:** Representa acción que cambia estado y registra timestamp  
**Controller:**
```java
@PostMapping("/tramos/{tramoId}/iniciar")
public ResponseEntity<TramoResponseDTO> iniciarTramo(@PathVariable Long tramoId) {
    TramoResponseDTO response = rutaService.iniciarTramo(tramoId);
    return ResponseEntity.ok(response);
}
```

**Service:**
```java
@Transactional
public TramoResponseDTO iniciarTramo(Long tramoId) {
    Tramo tramo = tramoRepository.findById(tramoId)
        .orElseThrow(() -> new TramoNotFoundException("Tramo no encontrado"));
    
    if (tramo.getCamionId() == null) {
        throw new IllegalStateException("No se puede iniciar un tramo sin camión asignado");
    }
    
    tramo.setFechaHoraInicio(LocalDateTime.now());
    tramo.setEstado(EstadoTramo.INICIADO);
    
    return mapTramoToResponseDTO(tramoRepository.save(tramo));
}
```

**¿Por qué POST?**
- Acción específica con validaciones
- No es idempotente (cada llamada registra nuevo timestamp)
- Cambia estado: ASIGNADO → INICIADO

---

#### POST `/api/rutas/tramos/{tramoId}/finalizar` - Finalizar tramo
**✅ Método Correcto:** POST  
**Justificación:** Acción que calcula costos reales y actualiza ruta  
**Controller:**
```java
@PostMapping("/tramos/{tramoId}/finalizar")
public ResponseEntity<TramoResponseDTO> finalizarTramo(@PathVariable Long tramoId) {
    TramoResponseDTO response = rutaService.finalizarTramo(tramoId);
    return ResponseEntity.ok(response);
}
```

**Service (proceso complejo):**
```java
@Transactional
public TramoResponseDTO finalizarTramo(Long tramoId) {
    Tramo tramo = tramoRepository.findById(tramoId)
        .orElseThrow(() -> new TramoNotFoundException("Tramo no encontrado"));
    
    // 1. Registrar tiempo de fin
    tramo.setFechaHoraFin(LocalDateTime.now());
    
    // 2. Calcular tiempo real (horas)
    long minutosTranscurridos = Duration.between(
        tramo.getFechaHoraInicio(), tramo.getFechaHoraFin()).toMinutes();
    tramo.setTiempoRealHoras((int) Math.ceil(minutosTranscurridos / 60.0));
    
    // 3. Calcular costo real = distancia × tarifa actual
    BigDecimal costoReal = calcularCostoReal(tramo);
    tramo.setCostoReal(costoReal);
    
    // 4. Cambiar estado
    tramo.setEstado(EstadoTramo.FINALIZADO);
    
    Tramo tramoGuardado = tramoRepository.save(tramo);
    
    // 5. Actualizar costoTotalReal de la ruta
    actualizarCostoTotalRutaDesdeTramos(tramo.getRuta());
    
    return mapTramoToResponseDTO(tramoGuardado);
}
```

**¿Por qué POST?**
- Acción compleja con múltiples operaciones
- Calcula costoReal, tiempoRealHoras
- Actualiza costoTotalReal de la ruta
- No es idempotente (cada ejecución usa timestamp actual)

---

#### DELETE `/api/rutas/{id}` - Desactivar ruta
**✅ Método Correcto:** DELETE  
**Justificación:** Elimina lógicamente el recurso  
**Controller:**
```java
@DeleteMapping("/{id}")
public ResponseEntity<Void> desactivarRuta(@PathVariable Long id) {
    rutaService.desactivarRuta(id);
    return ResponseEntity.noContent().build();
}
```

**Service:**
```java
@Transactional
public void desactivarRuta(Long id) {
    Ruta ruta = rutaRepository.findById(id)
        .orElseThrow(() -> new RutaNotFoundException("Ruta no encontrada"));
    
    // Validar que no haya tramos en proceso
    boolean tieneTramoActivo = ruta.getTramos().stream()
        .anyMatch(t -> t.getEstado() == EstadoTramo.INICIADO);
    
    if (tieneTramoActivo) {
        throw new IllegalStateException("No se puede desactivar ruta con tramos activos");
    }
    
    ruta.setActiva(false);
    rutaRepository.save(ruta);
}
```

**¿Por qué DELETE?**
- Indica intención de eliminar
- Implementa soft delete (setActiva(false))
- Incluye validación de negocio
- Retorna `204 NO CONTENT`

---

## Cálculo de Tarifas y Consumos

### Proceso Completo de Cálculo de Costos

#### 1. Costo Estimado (Al crear solicitud)

**Fórmula:**
```
costoEstimado = distanciaRealKm × tarifaVigente.valor
```

**Atributos involucrados:**

| Atributo | Fuente | Tipo | Descripción |
|----------|--------|------|-------------|
| `distanciaRealKm` | Google Maps API | BigDecimal | Distancia real por carretera |
| `tarifaVigente.valor` | tarifas-service | BigDecimal | Precio por km configurado |
| `costoEstimado` | Resultado cálculo | BigDecimal | Costo aproximado del traslado |

**Código completo:**
```java
// RutaService.calcularRutaTentativa()
@Transactional
public RutaResponseDTO calcularRutaTentativa(RutaRequestDTO request) {
    // 1. Obtener solicitud
    SolicitudDTO solicitud = obtenerSolicitud(request.getSolicitudId());
    
    // 2. Calcular distancia y tiempo con Google Maps
    DistanciaYTiempoDTO distanciaYTiempo = distanciaService.calcularDistanciaYTiempo(
        solicitud.getLatitudOrigen(),
        solicitud.getLongitudOrigen(),
        solicitud.getLatitudDestino(),
        solicitud.getLongitudDestino()
    );
    
    BigDecimal distancia = distanciaYTiempo.getDistanciaKm();
    Integer tiempoMinutos = distanciaYTiempo.getTiempoMinutos();
    
    // 3. Obtener tarifa vigente
    BigDecimal costo = calcularCostoConTarifa(distancia, solicitud.getTarifaId());
    
    // 4. Crear ruta y tramo
    Ruta ruta = Ruta.builder()
        .solicitudId(request.getSolicitudId())
        .distanciaTotalKm(distancia)
        .costoEstimado(costo)
        .build();
    
    return mapToResponseDTO(rutaRepository.save(ruta));
}

private BigDecimal calcularCostoConTarifa(BigDecimal distancia, Long tarifaId) {
    String url = tarifasServiceUrl + "/api/tarifas/" + tarifaId;
    TarifaDTO tarifa = restTemplate.getForObject(url, TarifaDTO.class);
    
    BigDecimal costo = distancia.multiply(tarifa.getValor())
        .setScale(2, RoundingMode.HALF_UP);
    
    log.info("Costo calculado con tarifa ID {}: {} km × ${}/km = ${}", 
             tarifaId, distancia, tarifa.getValor(), costo);
    
    return costo;
}
```

**Ejemplo real:**
```
Solicitud con tarifaId: 2
Coordenadas origen: -31.342516, -64.235711
Coordenadas destino: -31.361078, -64.212256

Google Maps responde:
  distance.value: 18570 metros → 18.57 km
  duration.value: 1380 segundos → 23 minutos

Tarifa ID 2:
  tipo: COSTO_KM_BASE
  valor: 1435.99
  unidad: ARS/km
  activo: true

Cálculo:
  costoEstimado = 18.57 km × $1,435.99/km
  costoEstimado = $26,666.33
```

---

#### 2. Costo Real (Al finalizar tramo)

**Fórmula:**
```
costoReal = distanciaKm × tarifaActual.valor
costoTotalReal = SUM(tramo.costoReal WHERE estado = FINALIZADO)
```

**Atributos involucrados:**

| Atributo | Fuente | Tipo | Descripción |
|----------|--------|------|-------------|
| `tramo.distanciaKm` | Google Maps API | BigDecimal | Distancia del tramo |
| `tarifaActual.valor` | tarifas-service | BigDecimal | Tarifa vigente al finalizar |
| `tramo.costoReal` | Cálculo | BigDecimal | Costo real del tramo |
| `ruta.costoTotalReal` | SUM(tramos) | BigDecimal | Suma de costos reales |

**Código completo:**
```java
// RutaService.finalizarTramo()
@Transactional
public TramoResponseDTO finalizarTramo(Long tramoId) {
    Tramo tramo = tramoRepository.findById(tramoId)
        .orElseThrow(() -> new TramoNotFoundException("Tramo no encontrado"));
    
    // 1. Registrar tiempo de finalización
    tramo.setFechaHoraFin(LocalDateTime.now());
    
    // 2. Calcular tiempo real (horas)
    long minutosTranscurridos = Duration.between(
        tramo.getFechaHoraInicio(), 
        tramo.getFechaHoraFin()
    ).toMinutes();
    
    int tiempoRealHoras = (int) Math.ceil(minutosTranscurridos / 60.0);
    tramo.setTiempoRealHoras(Math.max(1, tiempoRealHoras));
    
    // 3. Calcular costo real con tarifa actual
    BigDecimal costoReal = calcularCostoReal(tramo);
    tramo.setCostoReal(costoReal);
    
    // 4. Cambiar estado
    tramo.setEstado(EstadoTramo.FINALIZADO);
    
    Tramo tramoGuardado = tramoRepository.save(tramo);
    
    // 5. Actualizar costoTotalReal de la ruta
    Ruta ruta = tramo.getRuta();
    BigDecimal costoTotalReal = ruta.getTramos().stream()
        .filter(t -> t.getCostoReal() != null)
        .map(Tramo::getCostoReal)
        .reduce(BigDecimal.ZERO, BigDecimal::add);
    
    if (costoTotalReal.compareTo(BigDecimal.ZERO) > 0) {
        ruta.setCostoTotalReal(costoTotalReal);
        rutaRepository.save(ruta);
        log.info("Ruta {} actualizada con costoTotalReal: {}", ruta.getId(), costoTotalReal);
    }
    
    return mapTramoToResponseDTO(tramoGuardado);
}

private BigDecimal calcularCostoReal(Tramo tramo) {
    // Obtener tarifa actual (puede haber cambiado desde la estimación)
    Ruta ruta = tramo.getRuta();
    SolicitudDTO solicitud = obtenerSolicitud(ruta.getSolicitudId());
    
    String url = tarifasServiceUrl + "/api/tarifas/" + solicitud.getTarifaId();
    TarifaDTO tarifa = restTemplate.getForObject(url, TarifaDTO.class);
    
    BigDecimal costoReal = tramo.getDistanciaKm()
        .multiply(tarifa.getValor())
        .setScale(2, RoundingMode.HALF_UP);
    
    log.info("Costo real calculado para tramo {}: {} km × ${}/km = ${}", 
             tramo.getId(), tramo.getDistanciaKm(), tarifa.getValor(), costoReal);
    
    return costoReal;
}
```

**Ejemplo de actualización:**
```
Estado inicial:
  Ruta ID: 10
  Tramo ID: 5
    distanciaKm: 18.57
    costoAproximado: 26666.33
    costoReal: null
    estado: INICIADO
  
  ruta.costoEstimado: 26666.33
  ruta.costoTotalReal: null

Al finalizar tramo:
  1. Registrar: fechaHoraFin = 2025-11-13 22:45:00
  2. Calcular tiempo: 
     inicio = 2025-11-13 22:15:00
     fin = 2025-11-13 22:45:00
     diferencia = 30 minutos
     tiempoRealHoras = ceil(30/60) = 1 hora
  
  3. Calcular costo real:
     Obtener tarifa actual ID 2 → valor: 1435.99
     costoReal = 18.57 × 1435.99 = 26666.33
  
  4. Actualizar tramo:
     tramo.costoReal = 26666.33
     tramo.tiempoRealHoras = 1
     tramo.estado = FINALIZADO
  
  5. Actualizar ruta:
     costoTotalReal = SUM(todos los tramos finalizados)
     costoTotalReal = 26666.33 (solo hay 1 tramo)
     
Estado final:
  Tramo ID: 5
    costoReal: 26666.33
    tiempoRealHoras: 1
    estado: FINALIZADO
  
  Ruta ID: 10
    costoEstimado: 26666.33
    costoTotalReal: 26666.33
```

---

#### 3. Tiempo Estimado (Al calcular ruta)

**Fuente:** Google Maps Distance Matrix API  
**Proceso:**
1. Google Maps retorna `duration.value` (segundos)
2. Convertir: segundos → minutos (`duracionSegundos / 60`)
3. Convertir: minutos → horas (`Math.ceil(minutos / 60)`)
4. Aplicar mínimo: `Math.max(1, horas)`

**Código:**
```java
// GoogleMapsService.calcularTiempoViaje()
public Long calcularTiempoViaje(double lat1, double lon1, double lat2, double lon2) {
    String url = String.format(
        "%s/distancematrix/json?origins=%s,%s&destinations=%s,%s&mode=driving&units=metric&key=%s",
        baseUrl, lat1, lon1, lat2, lon2, apiKey
    );
    
    GoogleMapsDistanceResponse response = restTemplate.getForObject(url, GoogleMapsDistanceResponse.class);
    
    long duracionSegundos = response.getRows().get(0)
        .getElements().get(0)
        .getDuration().getValue(); // 1380 segundos
    
    long duracionMinutos = duracionSegundos / 60; // 23 minutos
    
    log.info("Tiempo de viaje calculado con Google Maps: {} minutos", duracionMinutos);
    return duracionMinutos;
}

// RutaService.calcularRutaTentativa()
DistanciaYTiempoDTO distanciaYTiempo = distanciaService.calcularDistanciaYTiempo(...);
Integer tiempoMinutos = distanciaYTiempo.getTiempoMinutos(); // 23
Integer tiempoHoras = (int) Math.ceil(tiempoMinutos / 60.0); // ceil(23/60) = 1
ruta.setTiempoEstimadoHoras(Math.max(1, tiempoHoras)); // 1

log.info("Tiempo calculado: {} minutos ({} hora(s))", tiempoMinutos, tiempoHoras);
```

**Ejemplo:**
```
Google Maps responde: duration.value = 1380 segundos
Conversión:
  1380 segundos / 60 = 23 minutos
  ceil(23 / 60) = ceil(0.383) = 1 hora
  max(1, 1) = 1 hora

tiempoEstimadoHoras = 1
```

---

#### 4. Tiempo Real (Al finalizar tramo)

**Fuente:** Diferencia entre timestamps  
**Fórmula:**
```
tiempoReal = fechaHoraFin - fechaHoraInicio (en minutos)
tiempoRealHoras = ceil(tiempoReal / 60)
```

**Código:**
```java
// RutaService.finalizarTramo()
LocalDateTime inicio = tramo.getFechaHoraInicio();
LocalDateTime fin = LocalDateTime.now();

long minutosTranscurridos = Duration.between(inicio, fin).toMinutes();
int tiempoRealHoras = (int) Math.ceil(minutosTranscurridos / 60.0);

tramo.setFechaHoraFin(fin);
tramo.setTiempoRealHoras(Math.max(1, tiempoRealHoras));

log.info("Tiempo real del tramo: {} minutos ({} hora(s))", 
         minutosTranscurridos, tiempoRealHoras);
```

**Ejemplo:**
```
fechaHoraInicio: 2025-11-13T22:15:00
fechaHoraFin:    2025-11-13T22:45:00

Diferencia: 30 minutos
tiempoRealHoras: ceil(30 / 60) = ceil(0.5) = 1 hora
```

---

## Atributos Utilizados en Cálculos

### Modelo: Tarifa

| Atributo | Tipo | Uso en Cálculo | Descripción |
|----------|------|----------------|-------------|
| `id` | Long | ✅ Identificador | Para buscar tarifa específica |
| `tipo` | TipoTarifa | ❌ Clasificación | COSTO_KM_BASE, COSTO_HORA, etc. |
| `valor` | BigDecimal | ✅ **Cálculo principal** | Precio por km (ej: 1435.99) |
| `unidad` | String | ❌ Descriptivo | "ARS/km" |
| `vigenciaDesde` | LocalDate | ✅ Validación | Fecha inicio vigencia |
| `vigenciaHasta` | LocalDate | ✅ Validación | Fecha fin vigencia |
| `activo` | Boolean | ✅ Filtro | Solo tarifas activas |

**Query para obtener tarifa vigente:**
```java
@Query("SELECT t FROM Tarifa t WHERE t.tipo = :tipo AND t.activo = true " +
       "AND :fecha BETWEEN t.vigenciaDesde AND t.vigenciaHasta")
Optional<Tarifa> findVigentePorTipo(@Param("tipo") TipoTarifa tipo, @Param("fecha") LocalDate fecha);
```

---

### Modelo: Ruta

| Atributo | Tipo | Uso en Cálculo | Descripción |
|----------|------|----------------|-------------|
| `distanciaTotalKm` | BigDecimal | ✅ Cálculo costo | De Google Maps API |
| `costoEstimado` | BigDecimal | ✅ Resultado | distancia × tarifa |
| `costoTotalReal` | BigDecimal | ✅ Resultado final | SUM(tramos.costoReal) |
| `tiempoEstimadoHoras` | Integer | ✅ Resultado | De Google Maps API |

---

### Modelo: Tramo

| Atributo | Tipo | Uso en Cálculo | Descripción |
|----------|------|----------------|-------------|
| `distanciaKm` | BigDecimal | ✅ Cálculo costo | Del segmento específico |
| `costoAproximado` | BigDecimal | ✅ Estimación | Al crear tramo |
| `costoReal` | BigDecimal | ✅ Cálculo final | Al finalizar tramo |
| `fechaHoraInicio` | LocalDateTime | ✅ Cálculo tiempo | Timestamp inicio |
| `fechaHoraFin` | LocalDateTime | ✅ Cálculo tiempo | Timestamp fin |
| `tiempoRealHoras` | Integer | ✅ Resultado | fin - inicio |
| `estado` | EstadoTramo | ✅ Control flujo | ESTIMADO/ASIGNADO/INICIADO/FINALIZADO |

---

### DTO: DistanciaYTiempoDTO

```java
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DistanciaYTiempoDTO {
    private BigDecimal distanciaKm;  // ✅ Usado en cálculo costo
    private Integer tiempoMinutos;    // ✅ Usado en cálculo tiempo
}
```

**Uso:**
```java
DistanciaYTiempoDTO resultado = distanciaService.calcularDistanciaYTiempo(lat1, lon1, lat2, lon2);

BigDecimal costo = resultado.getDistanciaKm().multiply(tarifa.getValor());
Integer horas = (int) Math.ceil(resultado.getTiempoMinutos() / 60.0);
```

---

## Validación de Métodos HTTP

### ✅ Correctos en el Sistema

| Endpoint | Método | ✅ Correcto | Justificación |
|----------|--------|-------------|---------------|
| `GET /api/tarifas` | GET | ✅ | Solo consulta, no modifica |
| `POST /api/tarifas` | POST | ✅ | Crea nuevo recurso |
| `PUT /api/tarifas/{id}` | PUT | ✅ | Reemplaza recurso completo |
| `DELETE /api/tarifas/{id}` | DELETE | ✅ | Soft delete (desactiva) |
| `PATCH /api/solicitudes/{id}/estado` | PATCH | ✅ | Actualización parcial (solo estado) |
| `PATCH /api/solicitudes/{id}/costos-tiempos` | PATCH | ✅ | Actualización parcial (5 campos) |
| `POST /api/rutas/calcular` | POST | ✅ | Crea ruta + ejecuta cálculos |
| `POST /api/rutas/tramos/{id}/iniciar` | POST | ✅ | Acción con cambio de estado |
| `POST /api/rutas/tramos/{id}/finalizar` | POST | ✅ | Acción compleja (calcula costos) |
| `DELETE /api/rutas/{id}` | DELETE | ✅ | Soft delete con validación |

---

### ❌ Errores Comunes a Evitar

| ❌ Incorrecto | ✅ Correcto | Por qué |
|---------------|-------------|---------|
| `GET /api/tarifas/crear` | `POST /api/tarifas` | GET no debe crear recursos |
| `POST /api/tarifas/{id}` | `PUT /api/tarifas/{id}` | POST para crear, PUT para actualizar |
| `PUT /api/solicitudes/{id}/estado` | `PATCH /api/solicitudes/{id}/estado` | PUT requiere todos los campos |
| `GET /api/rutas/tramos/{id}/finalizar` | `POST /api/rutas/tramos/{id}/finalizar` | GET no debe modificar estado |
| `PATCH /api/tarifas` | `POST /api/tarifas` | PATCH sin ID no tiene sentido |

---

## Preguntas Frecuentes en Defensa

### 1. ¿Por qué usas POST para `/api/rutas/tramos/{id}/iniciar` en lugar de PATCH?

**Respuesta:**
- POST representa una **acción** sobre el recurso, no solo actualizar campos
- `iniciar` es un verbo de acción en la URL, lo que indica operación compleja
- Realiza múltiples operaciones:
  1. Valida que el tramo tenga camión asignado
  2. Registra timestamp actual
  3. Cambia estado: ASIGNADO → INICIADO
- No es idempotente: ejecutar dos veces registra dos timestamps diferentes
- Sigue convención REST: `POST /recurso/{id}/accion`

**Código que lo justifica:**
```java
@PostMapping("/tramos/{tramoId}/iniciar")
public ResponseEntity<TramoResponseDTO> iniciarTramo(@PathVariable Long tramoId) {
    // Acción compleja, no simple actualización
    TramoResponseDTO response = rutaService.iniciarTramo(tramoId);
    return ResponseEntity.ok(response);
}
```

---

### 2. ¿Cómo se calcula el costo estimado y qué atributos se usan?

**Respuesta:**
El costo estimado se calcula al crear la solicitud mediante estos pasos:

1. **Obtener distancia real:**
   - Atributo: `contenedor.latitudOrigen`, `contenedor.longitudOrigen`
   - Atributo: `contenedor.latitudDestino`, `contenedor.longitudDestino`
   - Proceso: Enviar a Google Maps Distance Matrix API
   - Resultado: `distanciaRealKm` (BigDecimal)

2. **Obtener tarifa vigente:**
   - Atributo: `solicitud.tarifaId`
   - Proceso: Consultar `tarifas-service` GET `/api/tarifas/{id}`
   - Resultado: `tarifa.valor` (BigDecimal, ej: 1435.99)

3. **Calcular costo:**
   - Fórmula: `costoEstimado = distanciaRealKm × tarifa.valor`
   - Ejemplo: `18.57 km × 1435.99 ARS/km = 26,666.33 ARS`

4. **Guardar resultados:**
   - `ruta.distanciaTotalKm = 18.57`
   - `ruta.costoEstimado = 26666.33`
   - `solicitud.costoEstimado = 26666.33` (sincronizado)

**Código:**
```java
BigDecimal distancia = distanciaService.calcularDistanciaYTiempo(...).getDistanciaKm();
BigDecimal costo = calcularCostoConTarifa(distancia, solicitud.getTarifaId());

private BigDecimal calcularCostoConTarifa(BigDecimal distancia, Long tarifaId) {
    TarifaDTO tarifa = restTemplate.getForObject(
        tarifasServiceUrl + "/api/tarifas/" + tarifaId, TarifaDTO.class);
    return distancia.multiply(tarifa.getValor()).setScale(2, RoundingMode.HALF_UP);
}
```

---

### 3. ¿Qué diferencia hay entre costoEstimado y costoTotalReal?

**Respuesta:**

| Característica | costoEstimado | costoTotalReal |
|----------------|---------------|----------------|
| **Cuándo se calcula** | Al crear solicitud | Al finalizar todos los tramos |
| **Tarifa usada** | Vigente al momento de estimación | Vigente al momento de finalización |
| **Propósito** | Cotización para el cliente | Facturación real |
| **Puede cambiar** | No (queda fijo) | Sí (se acumula por tramos) |
| **Dónde se almacena** | `ruta.costoEstimado`, `solicitud.costoEstimado` | `ruta.costoTotalReal`, `solicitud.costoFinal` |

**Ejemplo con cambio de tarifa:**
```
Día 1 - Crear solicitud:
  Tarifa vigente: $1,000/km
  Distancia: 100 km
  costoEstimado = 100 × 1000 = $100,000

Día 15 - Finalizar tramo:
  Tarifa vigente: $1,200/km (aumentó)
  Distancia real: 102 km (ruta real ligeramente diferente)
  costoReal = 102 × 1200 = $122,400
  
costoEstimado: $100,000 (no cambió)
costoTotalReal: $122,400 (costo real final)
```

**Código:**
```java
// Cálculo estimado (al crear)
ruta.setCostoEstimado(distancia.multiply(tarifaOriginal.getValor()));

// Cálculo real (al finalizar tramos)
BigDecimal costoTotalReal = ruta.getTramos().stream()
    .filter(t -> t.getCostoReal() != null)
    .map(Tramo::getCostoReal)
    .reduce(BigDecimal.ZERO, BigDecimal::add);

ruta.setCostoTotalReal(costoTotalReal);
```

---

### 4. ¿Por qué PATCH en `/api/solicitudes/{id}/estado` y no PUT?

**Respuesta:**

**PUT requiere enviar TODOS los campos:**
```json
{
  "clienteId": 1,
  "tarifaId": 2,
  "numero": "SOL-20251113001",
  "estado": "EN_TRANSITO",
  "costoEstimado": 26666.33,
  "costoFinal": null,
  "tiempoEstimadoHoras": 1,
  "tiempoRealHoras": null,
  "rutaId": 10,
  "contenedor": { ... } // 10+ campos más
}
```

**PATCH solo envía el campo que cambió:**
```json
// Query param: ?estado=EN_TRANSITO
```

**Justificación técnica:**
- Solicitud tiene 15+ atributos
- Solo necesitamos cambiar 1 campo: `estado`
- PATCH es más eficiente y seguro
- Evita riesgo de sobrescribir otros campos accidentalmente

**Controller:**
```java
@PatchMapping("/{id}/estado")
public ResponseEntity<SolicitudResponseDTO> actualizarEstado(
        @PathVariable Long id,
        @RequestParam EstadoSolicitud estado) {
    // Solo actualiza el estado
    SolicitudResponseDTO response = solicitudService.actualizarEstado(id, estado);
    return ResponseEntity.ok(response);
}
```

**Service:**
```java
@Transactional
public SolicitudResponseDTO actualizarEstado(Long id, EstadoSolicitud estado) {
    Solicitud solicitud = solicitudRepository.findById(id)
        .orElseThrow(() -> new SolicitudNotFoundException("Solicitud no encontrada"));
    
    solicitud.setEstado(estado); // Solo actualiza este campo
    
    return mapToResponseDTO(solicitudRepository.save(solicitud));
}
```

---

### 5. ¿Cómo se obtiene el tiempo estimado y qué fuente se usa?

**Respuesta:**

**Fuente principal:** Google Maps Distance Matrix API

**Proceso completo:**

1. **Solicitud a Google Maps:**
```
GET https://maps.googleapis.com/maps/api/distancematrix/json
  ?origins=-31.342516,-64.235711
  &destinations=-31.361078,-64.212256
  &mode=driving
  &units=metric
  &key=AIzaSyAUp0j1WFgacoQYTKhtPI-CF6Ld7a7jHSg
```

2. **Respuesta de Google Maps:**
```json
{
  "rows": [{
    "elements": [{
      "distance": { "value": 18570 },
      "duration": { "value": 1380 },  ← 1380 segundos
      "status": "OK"
    }]
  }]
}
```

3. **Extracción:**
```java
long duracionSegundos = element.getDuration().getValue(); // 1380
long duracionMinutos = duracionSegundos / 60;            // 23 minutos
```

4. **Conversión a horas:**
```java
Integer tiempoMinutos = 23;
Integer tiempoHoras = (int) Math.ceil(tiempoMinutos / 60.0); // ceil(23/60) = 1
ruta.setTiempoEstimadoHoras(Math.max(1, tiempoHoras));       // 1 hora
```

**Atributos involucrados:**
- Entrada: `latitudOrigen`, `longitudOrigen`, `latitudDestino`, `longitudDestino`
- Google Maps: `duration.value` (segundos)
- Conversión: `tiempoMinutos = duracionSegundos / 60`
- Resultado: `tiempoEstimadoHoras = ceil(tiempoMinutos / 60)`
- Mínimo: `max(1, tiempoEstimadoHoras)`

**Fallback (si Google Maps falla):**
```java
// Estimación: 50 km/h promedio
int tiempoMinutos = distanciaKm
    .divide(BigDecimal.valueOf(50), 2, RoundingMode.HALF_UP)
    .multiply(BigDecimal.valueOf(60))
    .intValue();

return Math.max(tiempoMinutos, 30); // Mínimo 30 minutos
```

---

### 6. ¿Qué validaciones se hacen antes de asignar un camión?

**Respuesta:**

**Validaciones en orden:**

1. **Tramo existe:**
```java
Tramo tramo = tramoRepository.findById(tramoId)
    .orElseThrow(() -> new TramoNotFoundException("Tramo no encontrado con ID: " + tramoId));
```

2. **Camión está disponible:**
```java
private void validarCamionDisponible(Long camionId) {
    String url = camionesServiceUrl + "/api/camiones/" + camionId;
    CamionDTO camion = restTemplate.getForObject(url, CamionDTO.class);
    
    if (!camion.getDisponible()) {
        throw new CamionNoDisponibleException("Camión no disponible: " + camionId);
    }
    
    if (!camion.getActivo()) {
        throw new CamionNoDisponibleException("Camión inactivo: " + camionId);
    }
}
```

3. **Camión no tiene otros tramos activos:**
```java
List<Tramo> tramosActivos = tramoRepository.findByCamionIdAndEstadoIn(
    camionId, 
    Arrays.asList(EstadoTramo.ASIGNADO, EstadoTramo.INICIADO)
);

if (!tramosActivos.isEmpty()) {
    throw new CamionNoDisponibleException(
        "Camión " + camionId + " ya tiene tramos activos");
}
```

4. **Actualizar tramo:**
```java
tramo.setCamionId(camionId);
tramo.setEstado(EstadoTramo.ASIGNADO);
tramoRepository.save(tramo);
```

**Atributos validados:**
- `camion.disponible` (Boolean)
- `camion.activo` (Boolean)
- `tramo.estado` (debe permitir asignación)
- Tramos existentes del camión (no debe tener activos)

---

### 7. ¿Cuándo se actualiza costoTotalReal y cómo?

**Respuesta:**

**Momento:** Al finalizar cada tramo

**Proceso automático:**

1. **Finalizar tramo:**
```java
@PostMapping("/tramos/{tramoId}/finalizar")
public ResponseEntity<TramoResponseDTO> finalizarTramo(@PathVariable Long tramoId)
```

2. **Calcular costo real del tramo:**
```java
BigDecimal costoReal = tramo.getDistanciaKm()
    .multiply(tarifaActual.getValor())
    .setScale(2, RoundingMode.HALF_UP);

tramo.setCostoReal(costoReal);
tramo.setEstado(EstadoTramo.FINALIZADO);
```

3. **Sumar todos los tramos finalizados:**
```java
Ruta ruta = tramo.getRuta();

BigDecimal costoTotalReal = ruta.getTramos().stream()
    .filter(t -> t.getCostoReal() != null)        // Solo finalizados
    .map(Tramo::getCostoReal)                    // Obtener costos
    .reduce(BigDecimal.ZERO, BigDecimal::add);   // Sumar todos
```

4. **Actualizar ruta automáticamente:**
```java
if (costoTotalReal.compareTo(BigDecimal.ZERO) > 0) {
    ruta.setCostoTotalReal(costoTotalReal);
    rutaRepository.save(ruta);
    log.info("Ruta {} actualizada con costoTotalReal: {}", ruta.getId(), costoTotalReal);
}
```

**Ejemplo con múltiples tramos:**
```
Ruta con 3 tramos:

Tramo 1: FINALIZADO → costoReal: $10,000
Tramo 2: FINALIZADO → costoReal: $15,000
Tramo 3: INICIADO   → costoReal: null

costoTotalReal = $10,000 + $15,000 = $25,000

Cuando se finalice Tramo 3 (costoReal: $8,000):
costoTotalReal = $10,000 + $15,000 + $8,000 = $33,000
```

**Atributos involucrados:**
- `tramo.costoReal` (calculado al finalizar)
- `tramo.estado` (debe ser FINALIZADO)
- `ruta.costoTotalReal` (suma acumulativa)

---

### 8. ¿Por qué DELETE retorna 204 NO CONTENT y no 200 OK?

**Respuesta:**

**Código HTTP 204 NO CONTENT:**
- Indica que la operación fue exitosa
- Pero no hay contenido para retornar en el body
- Es más semántico para DELETE que 200 OK

**Comparación:**

| Status | Body | Cuándo usar |
|--------|------|-------------|
| `200 OK` | Con contenido | GET, POST, PUT que retornan datos |
| `201 CREATED` | Con recurso creado | POST exitoso |
| `204 NO CONTENT` | Sin body | DELETE exitoso |

**Implementación:**
```java
@DeleteMapping("/{id}")
public ResponseEntity<Void> eliminarTarifa(@PathVariable Long id) {
    tarifaService.eliminarTarifa(id);
    return ResponseEntity.noContent().build(); // 204
}

// Alternativa menos común:
@DeleteMapping("/{id}")
public ResponseEntity<MessageDTO> eliminarTarifa(@PathVariable Long id) {
    tarifaService.eliminarTarifa(id);
    return ResponseEntity.ok(new MessageDTO("Tarifa eliminada")); // 200
}
```

**Ventajas de 204:**
- Ahorra ancho de banda (no envía body)
- Estándar REST para DELETE
- Cliente sabe que operación fue exitosa sin procesar respuesta

---

### 9. ¿Qué pasa si la tarifa cambia entre la estimación y la finalización?

**Respuesta:**

**Escenario:**
```
Día 1 - Crear solicitud:
  Tarifa ID 2: valor = $1,000/km
  Distancia: 100 km
  costoEstimado = $100,000

Día 10 - Operador actualiza tarifa:
  PUT /api/tarifas/2
  {
    "valor": 1200.00
  }

Día 15 - Finalizar tramo:
  Tarifa ID 2: valor = $1,200/km (actualizada)
  Distancia: 100 km
  costoReal = $120,000
```

**Comportamiento del sistema:**

1. **costoEstimado NO cambia:**
```java
// Se calculó en el pasado y queda fijo
ruta.setCostoEstimado(100000); // Nunca se modifica
```

2. **costoReal usa tarifa actual:**
```java
@Transactional
public TramoResponseDTO finalizarTramo(Long tramoId) {
    // Obtiene la tarifa ACTUAL (no la de la estimación)
    TarifaDTO tarifaActual = restTemplate.getForObject(
        tarifasServiceUrl + "/api/tarifas/" + solicitud.getTarifaId(),
        TarifaDTO.class
    );
    
    BigDecimal costoReal = tramo.getDistanciaKm()
        .multiply(tarifaActual.getValor()); // Usa valor actual: $1,200
    
    tramo.setCostoReal(costoReal); // $120,000
}
```

3. **Resultado final:**
```
Solicitud:
  costoEstimado: $100,000 (original, no cambió)
  costoFinal: $120,000 (calculado con tarifa actual)
  
Diferencia: $20,000 (20% más caro)
```

**Justificación del diseño:**
- `costoEstimado`: Cotización original para el cliente
- `costoTotalReal`: Facturación real con tarifas vigentes
- Permite auditoría y análisis de variaciones

---

### 10. ¿Cómo se propaga el JWT entre servicios?

**Respuesta:**

**Configuración:**
```java
@Configuration
public class RestTemplateConfig {
    
    @Bean
    public RestTemplate restTemplate() {
        RestTemplate restTemplate = new RestTemplate();
        restTemplate.setInterceptors(Collections.singletonList(new JwtPropagationInterceptor()));
        return restTemplate;
    }
}
```

**Interceptor:**
```java
public class JwtPropagationInterceptor implements ClientHttpRequestInterceptor {
    
    @Override
    public ClientHttpResponse intercept(HttpRequest request, byte[] body, 
                                         ClientHttpRequestExecution execution) throws IOException {
        // 1. Obtener JWT del contexto de seguridad
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        
        if (authentication != null && authentication.getCredentials() != null) {
            String token = authentication.getCredentials().toString();
            
            // 2. Agregar header Authorization
            request.getHeaders().add("Authorization", "Bearer " + token);
        }
        
        // 3. Continuar con la petición
        return execution.execute(request, body);
    }
}
```

**Flujo completo:**
```
Cliente
  ↓ Authorization: Bearer eyJhbGc...
API Gateway
  ↓ Valida JWT
  ↓ Propaga JWT
Rutas Service
  ↓ RestTemplate con interceptor
  ↓ Authorization: Bearer eyJhbGc... (mismo token)
Tarifas Service
  ↓ Valida JWT
  ↓ Verifica rol OPERADOR
  ↓ Retorna tarifa
```

**Ejemplo en código:**
```java
// RutaService.calcularCostoConTarifa()
private BigDecimal calcularCostoConTarifa(BigDecimal distancia, Long tarifaId) {
    String url = tarifasServiceUrl + "/api/tarifas/" + tarifaId;
    
    // RestTemplate automáticamente propaga el JWT del contexto actual
    TarifaDTO tarifa = restTemplate.getForObject(url, TarifaDTO.class);
    
    return distancia.multiply(tarifa.getValor());
}

// Tarifas Service valida el JWT
@GetMapping("/{id}")
@PreAuthorize("hasRole('OPERADOR')") // ← Valida rol del JWT propagado
public ResponseEntity<TarifaResponseDTO> obtenerPorId(@PathVariable Long id) {
    return ResponseEntity.ok(tarifaService.obtenerPorId(id));
}
```

**Atributos del JWT:**
- `sub`: Usuario (ej: "operador1")
- `realm_access.roles`: ["ROLE_OPERADOR"]
- `exp`: Expiración del token
- `iat`: Fecha de emisión

---

## Resumen de Conceptos Clave

### Métodos HTTP Usados Correctamente

✅ **GET** → Consultar sin modificar  
✅ **POST** → Crear recursos o ejecutar acciones  
✅ **PUT** → Actualizar recurso completo  
✅ **PATCH** → Actualizar campos específicos  
✅ **DELETE** → Desactivar/eliminar (soft delete)

### Atributos Principales en Cálculos

✅ `distanciaKm` (Google Maps) × `tarifa.valor` → `costoEstimado`  
✅ `duration.value` (Google Maps) → `tiempoEstimadoHoras`  
✅ `fechaHoraFin - fechaHoraInicio` → `tiempoRealHoras`  
✅ SUM(`tramo.costoReal`) → `ruta.costoTotalReal`

### Flujo de Estados

✅ Tramo: ESTIMADO → ASIGNADO → INICIADO → FINALIZADO  
✅ Solicitud: BORRADOR → CONFIRMADA → EN_TRANSITO → COMPLETADA

---

**¡Listo para defender!** 🎓
