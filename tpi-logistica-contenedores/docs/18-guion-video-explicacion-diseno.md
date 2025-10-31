# Guión para Video - Explicación del Diseño

## Duración estimada: 8-10 minutos

---

## 1. INTRODUCCIÓN (1 minuto)
**[Integrante 1]**

"Buenas, somos el grupo [nombre] y vamos a presentar el diseño de nuestro sistema de logística de transporte de contenedores. Elegimos una arquitectura de microservicios con 6 servicios independientes más un API Gateway, y cada servicio tiene su propia base de datos PostgreSQL."

---

## 2. ARQUITECTURA DE MICROSERVICIOS (3 minutos)
**[Integrante 2]**

### ¿Por qué 6 microservicios?

"Dividimos el sistema en 6 microservicios porque cada uno tiene una responsabilidad clara y puede evolucionar independientemente:"

1. **clientes-service**: Gestiona solo clientes
2. **depositos-service**: Gestiona solo depósitos
3. **camiones-service**: Gestiona camiones y transportistas (están juntos porque un camión siempre tiene un transportista asignado)
4. **tarifas-service**: Gestiona las tarifas y configuraciones de precios
5. **solicitudes-service**: Gestiona solicitudes, contenedores y su historial de estados
6. **rutas-service**: Calcula rutas, gestiona tramos y asigna camiones

### ¿Por qué no más o menos servicios?

"Consideramos hacer menos servicios (por ejemplo, juntar depósitos y camiones), pero decidimos separarlos porque:
- Cada uno tiene lógica de negocio diferente
- Pueden escalar independientemente según la demanda
- Facilita el mantenimiento y testing

También consideramos hacer más servicios (por ejemplo, separar contenedores de solicitudes), pero decidimos mantenerlos juntos porque un contenedor no existe sin una solicitud."

---

## 3. MODELO DE BASE DE DATOS (3 minutos)
**[Integrante 3]**

### Decisiones clave del modelo:

**1. Una base de datos por microservicio**
"Cada microservicio tiene su propia base de datos PostgreSQL. Esto nos da independencia y evita acoplamiento entre servicios."

**2. Campos de ubicación directos (sin tabla Ubicacion compartida)**
"En lugar de crear una tabla Ubicacion centralizada, pusimos los campos de ubicación (dirección, latitud, longitud) directamente en las entidades que los necesitan:
- Solicitud tiene ubicacion_origen y ubicacion_destino
- Tramo tiene ubicacion_origen y ubicacion_destino  
- Deposito tiene su ubicación

Esto evita dependencias entre microservicios y cada uno es autónomo."

**3. Relaciones entre microservicios mediante IDs**
"Cuando un servicio necesita datos de otro, solo guarda el ID. Por ejemplo:
- Solicitud guarda cliente_id (referencia a clientes-service)
- Tramo guarda camion_id (referencia a camiones-service)
- Ruta guarda solicitud_id (referencia a solicitudes-service)

Luego hacen llamadas REST para obtener los datos completos cuando los necesitan."

**4. Historial de estados**
"Creamos HistorialEstadoContenedor para tener trazabilidad completa de todos los cambios de estado y ubicación de cada contenedor. Esto es clave para el seguimiento que piden los clientes."

---

## 4. VENTAJAS DEL DISEÑO (2 minutos)
**[Integrante 4]**

### Ventajas principales:

1. **Escalabilidad independiente**: Si hay muchas consultas de seguimiento, solo escalamos solicitudes-service
2. **Despliegue independiente**: Podemos actualizar tarifas-service sin tocar los demás
3. **Tecnologías diferentes**: Cada servicio podría usar una BD diferente si fuera necesario
4. **Equipos independientes**: Diferentes equipos pueden trabajar en paralelo sin conflictos
5. **Resiliencia**: Si cae camiones-service, los clientes aún pueden crear solicitudes
6. **Mantenibilidad**: Cada servicio es pequeño y fácil de entender

### Desventajas que mitigamos:

1. **Complejidad**: Usamos Docker Compose para simplificar el despliegue
2. **Transacciones distribuidas**: Implementamos compensaciones y estados intermedios
3. **Latencia**: Optimizamos las llamadas entre servicios y usamos caché cuando es necesario

---

## 5. INTEGRACIÓN CON REQUERIMIENTOS (1 minuto)
**[Todos juntos - resumen rápido]**

"Nuestro diseño cumple todos los requerimientos del TPI:
- Roles implementados con Keycloak (Cliente, Operador, Transportista)
- Integración con Google Maps API para distancias reales
- Cálculo de costos con tarifas configurables
- Seguimiento en tiempo real con historial de estados
- API REST documentada con Swagger
- Despliegue completo con Docker Compose"

---

## 6. CIERRE
**[Integrante 1]**

"En resumen, elegimos esta arquitectura porque balancea complejidad y beneficios: es lo suficientemente modular para ser mantenible y escalable, pero no tan fragmentada que sea difícil de gestionar. Cada decisión de diseño responde a un requerimiento específico del sistema de logística."

---

## TIPS PARA EL VIDEO:

1. **Mostrar diagramas**: Mientras hablan, mostrar el diagrama de microservicios (archivo 14) y el DER
2. **Señalar conexiones**: Usar flechas o resaltados para mostrar cómo se comunican los servicios
3. **Ejemplos concretos**: "Por ejemplo, cuando un cliente crea una solicitud, el solicitudes-service llama a clientes-service para validar que el cliente existe"
4. **Ser naturales**: No leer textualmente, usar esto como guía
5. **Dividir equitativamente**: Cada integrante habla 2-3 minutos
6. **Practicar transiciones**: "Ahora [nombre] va a explicar el modelo de datos..."

---

## PREGUNTAS FRECUENTES QUE PUEDEN SURGIR:

**P: ¿Por qué no un monolito?**
R: "Un monolito sería más simple inicialmente, pero no escala bien y cualquier cambio requiere redesplegar todo el sistema."

**P: ¿Por qué PostgreSQL y no MongoDB?**
R: "PostgreSQL porque nuestros datos son relacionales y necesitamos transacciones ACID. Las relaciones entre solicitudes, rutas y tramos son complejas y SQL las maneja mejor."

**P: ¿Cómo manejan las transacciones distribuidas?**
R: "Usamos el patrón Saga con compensaciones. Por ejemplo, si falla la asignación de un camión, revertimos el estado de la solicitud."

**P: ¿Por qué camiones y transportistas están juntos?**
R: "Porque tienen alta cohesión: un camión siempre tiene un transportista y se consultan juntos. Separarlos agregaría complejidad sin beneficios."
