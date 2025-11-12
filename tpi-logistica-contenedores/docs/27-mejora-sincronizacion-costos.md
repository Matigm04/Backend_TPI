# Mejora: Sincronización de Costos y Tiempos entre Rutas y Solicitudes

## Problema Identificado

Durante las pruebas del flujo completo, se detectó que aunque los tramos de la ruta contenían información de costos y tiempos (distancia, costo aproximado, costo real, fechas de inicio/fin), estos datos **no se reflejaban en la entidad Solicitud**.

### Síntoma:
Al consultar una solicitud después de calcular la ruta y finalizar el tramo:
```json
GET /api/solicitudes/1
{
  "id": 1,
  "numero": "SOL-2025-0001",
  "costoEstimado": null,      // ❌ Debería tener el costo calculado
  "tiempoEstimadoHoras": null, // ❌ Debería tener el tiempo estimado
  "costoFinal": null,          // ❌ Debería tener el costo real
  "tiempoRealHoras": null,     // ❌ Debería tener el tiempo real
  "rutaId": null               // ❌ Debería tener la referencia a la ruta
}
```

## Solución Implementada

Se creó una **integración entre microservicios** para mantener sincronizados los datos de costos y tiempos:

### 1. Nuevo Endpoint en `solicitudes-service`

**Endpoint:** `PATCH /api/solicitudes/{id}/costos-tiempos`

**DTO creado:**
```java
@Data
@Builder
public class ActualizarCostosDTO {
    private BigDecimal costoEstimado;
    private Integer tiempoEstimadoHoras;
    private BigDecimal costoFinal;
    private Integer tiempoRealHoras;
    private Long rutaId;
}
```

**Lógica:**
- Actualiza solo los campos que no son `null` en el DTO
- Permite actualizaciones parciales (solo estimados, solo finales, o ambos)
- Registra logs de las actualizaciones

### 2. Modificación en `rutas-service`

#### a) Al calcular la ruta (POST /api/rutas/calcular):

```java
// Después de guardar la ruta
actualizarSolicitudConCostos(
    solicitudId, 
    costoEstimado,      // Calculado a partir de la distancia
    tiempoEstimado,     // Calculado a partir de la distancia
    null,               // costoFinal (aún no finalizado)
    null,               // tiempoReal (aún no finalizado)
    rutaId              // Referencia a la ruta creada
);
```

#### b) Al finalizar el tramo (POST /api/rutas/tramos/{id}/finalizar):

```java
// Calcular tiempo real en horas
long minutos = Duration.between(fechaInicio, fechaFin).toMinutes();
Integer tiempoRealHoras = (int) Math.ceil(minutos / 60.0);

// Actualizar solicitud con valores finales
actualizarSolicitudConCostos(
    solicitudId,
    null,               // costoEstimado (ya está guardado)
    null,               // tiempoEstimado (ya está guardado)
    costoReal,          // Costo real del tramo finalizado
    tiempoRealHoras,    // Tiempo real calculado
    null                // rutaId (ya está guardado)
);
```

### 3. Método Auxiliar en RutaService

```java
private void actualizarSolicitudConCostos(Long solicitudId, 
                                           BigDecimal costoEstimado, 
                                           Integer tiempoEstimadoHoras, 
                                           BigDecimal costoFinal, 
                                           Integer tiempoRealHoras, 
                                           Long rutaId) {
    try {
        String url = solicitudesServiceUrl + "/api/solicitudes/" + solicitudId + "/costos-tiempos";
        
        ActualizarCostosDTO actualizacion = ActualizarCostosDTO.builder()
            .costoEstimado(costoEstimado)
            .tiempoEstimadoHoras(tiempoEstimadoHoras)
            .costoFinal(costoFinal)
            .tiempoRealHoras(tiempoRealHoras)
            .rutaId(rutaId)
            .build();
        
        restTemplate.patchForObject(url, actualizacion, Object.class);
        log.info("Solicitud actualizada con costos...");
    } catch (Exception e) {
        log.error("Error al actualizar solicitud: {}", e.getMessage());
        // No lanzamos excepción para no afectar el flujo principal
    }
}
```

**Características:**
- Usa `restTemplate` con propagación de JWT (ya configurado)
- Maneja errores sin interrumpir el flujo principal
- Registra logs para depuración

## Resultado Final

Ahora al consultar una solicitud después del flujo completo:

```json
GET /api/solicitudes/1
{
  "id": 1,
  "numero": "SOL-2025-0001",
  "estado": "BORRADOR",
  "costoEstimado": 1031.00,      // ✅ Actualizado en Paso 2 (calcular ruta)
  "tiempoEstimadoHoras": 1,      // ✅ Actualizado en Paso 2
  "costoFinal": 1031.00,         // ✅ Actualizado en Paso 5 (finalizar tramo)
  "tiempoRealHoras": 1,          // ✅ Actualizado en Paso 5
  "rutaId": 8,                   // ✅ Referencia a la ruta asociada
  "contenedor": { ... }
}
```

## Beneficios

1. **Consistencia de datos:** La solicitud siempre refleja el estado actual de la ruta
2. **Facilita consultas:** Los clientes pueden consultar la solicitud sin navegar a la ruta
3. **Auditoría completa:** Se mantiene registro de costos estimados vs reales
4. **Desacoplamiento:** El error en la actualización no afecta la operación principal
5. **Escalabilidad:** Patrón reutilizable para futuras integraciones

## Archivos Modificados

### solicitudes-service:
- ✅ `dto/ActualizarCostosDTO.java` (creado)
- ✅ `service/SolicitudService.java` (método `actualizarCostosYTiempos`)
- ✅ `controller/SolicitudController.java` (endpoint `PATCH /{id}/costos-tiempos`)

### rutas-service:
- ✅ `dto/ActualizarCostosDTO.java` (creado)
- ✅ `service/RutaService.java` (métodos `actualizarSolicitudConCostos`, modificaciones en `calcularRutaTentativa` y `finalizarTramo`)

## Testing

### Pasos para probar:

1. **Crear solicitud** → Verificar campos null
2. **Calcular ruta** → Verificar `costoEstimado` y `tiempoEstimadoHoras` actualizados
3. **Asignar camión** → Sin cambios en solicitud
4. **Iniciar tramo** → Sin cambios en solicitud
5. **Finalizar tramo** → Verificar `costoFinal` y `tiempoRealHoras` actualizados
6. **Consultar solicitud** → Todos los campos poblados correctamente

### Comandos:

```bash
# Reconstruir servicios
docker-compose build solicitudes-service rutas-service

# Reiniciar servicios
docker-compose up -d solicitudes-service rutas-service

# Ver logs para verificar actualizaciones
docker-compose logs -f rutas-service | grep "actualizada"
docker-compose logs -f solicitudes-service | grep "Actualizando costos"
```

## Consideraciones Futuras

1. **Estado automático:** Podría agregarse lógica para cambiar el estado de la solicitud a `COMPLETADA` cuando todos los tramos estén finalizados
2. **Cálculo de costos múltiples tramos:** Si hay varios tramos, sumar todos los costos
3. **Notificaciones:** Enviar notificación al cliente cuando se actualicen los costos finales
4. **Validaciones:** Asegurar que `costoFinal` ≤ `costoEstimado * factor_tolerancia`

---

**Fecha de implementación:** 2025-11-11  
**Autor:** Matias Gimenez  
**Tipo:** Mejora de integración entre microservicios
