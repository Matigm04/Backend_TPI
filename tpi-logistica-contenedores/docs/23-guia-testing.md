# Guía de Testing - TPI Logística de Contenedores

## Introducción

Esta guía documenta la estrategia de testing implementada en el proyecto, utilizando **JUnit 5** y **Mockito** según los requerimientos del TPI.

## Estructura de Tests

### Tipos de Tests Implementados

1. **Tests Unitarios** - Prueban la lógica de negocio en la capa Service
2. **Tests de Integración** - Prueban los endpoints REST en la capa Controller
3. **Tests de Repository** - Prueban las consultas personalizadas (opcional)

## Patrón de Testing Establecido

### Tests Unitarios (Service Layer)

**Ubicación:** `src/test/java/com/logistica/{servicio}/service/`

**Características:**
- Usan `@ExtendWith(MockitoExtension.class)`
- Mockean dependencias con `@Mock`
- Inyectan el servicio bajo prueba con `@InjectMocks`
- Siguen el patrón **Given-When-Then**
- Usan **AssertJ** para assertions más legibles

**Ejemplo:**
\`\`\`java
@ExtendWith(MockitoExtension.class)
class ClienteServiceTest {
    
    @Mock
    private ClienteRepository clienteRepository;
    
    @InjectMocks
    private ClienteService clienteService;
    
    @Test
    @DisplayName("Crear cliente exitosamente")
    void crearCliente_Exitoso() {
        // Given
        when(clienteRepository.save(any())).thenReturn(cliente);
        
        // When
        ClienteResponseDTO resultado = clienteService.crearCliente(requestDTO);
        
        // Then
        assertThat(resultado).isNotNull();
        assertThat(resultado.getNombre()).isEqualTo("Juan");
        verify(clienteRepository).save(any());
    }
}
\`\`\`

### Tests de Integración (Controller Layer)

**Ubicación:** `src/test/java/com/logistica/{servicio}/controller/`

**Características:**
- Usan `@WebMvcTest(ControllerClass.class)`
- Mockean el servicio con `@MockBean`
- Usan `MockMvc` para simular peticiones HTTP
- Incluyen `@WithMockUser` para simular autenticación
- Verifican códigos de estado HTTP y respuestas JSON

**Ejemplo:**
\`\`\`java
@WebMvcTest(ClienteController.class)
class ClienteControllerTest {
    
    @Autowired
    private MockMvc mockMvc;
    
    @MockBean
    private ClienteService clienteService;
    
    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("POST /api/clientes - Crear cliente exitosamente")
    void crearCliente_Exitoso() throws Exception {
        // Given
        when(clienteService.crearCliente(any())).thenReturn(responseDTO);
        
        // When & Then
        mockMvc.perform(post("/api/clientes")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestDTO)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.nombre", is("Juan")));
    }
}
\`\`\`

## Cobertura de Tests

### Tests Implementados para clientes-service

#### ClienteServiceTest (Tests Unitarios)
- ✅ Crear cliente exitosamente
- ✅ Crear cliente con DNI duplicado lanza excepción
- ✅ Crear cliente con email duplicado lanza excepción
- ✅ Obtener cliente por ID exitosamente
- ✅ Obtener cliente por ID inexistente lanza excepción
- ✅ Obtener cliente por DNI exitosamente
- ✅ Obtener todos los clientes
- ✅ Obtener clientes activos
- ✅ Buscar clientes por nombre
- ✅ Actualizar cliente exitosamente
- ✅ Actualizar cliente inexistente lanza excepción
- ✅ Desactivar cliente exitosamente
- ✅ Activar cliente exitosamente
- ✅ Eliminar cliente exitosamente
- ✅ Eliminar cliente inexistente lanza excepción

**Total: 15 tests unitarios**

#### ClienteControllerTest (Tests de Integración)
- ✅ POST /api/clientes - Crear cliente exitosamente
- ✅ POST /api/clientes - Crear cliente con DNI duplicado retorna 400
- ✅ POST /api/clientes - Crear cliente con datos inválidos retorna 400
- ✅ GET /api/clientes/{id} - Obtener cliente por ID exitosamente
- ✅ GET /api/clientes/{id} - Cliente no encontrado retorna 404
- ✅ GET /api/clientes - Obtener todos los clientes
- ✅ GET /api/clientes/activos - Obtener clientes activos
- ✅ GET /api/clientes/buscar - Buscar clientes por nombre
- ✅ PUT /api/clientes/{id} - Actualizar cliente exitosamente
- ✅ PATCH /api/clientes/{id}/desactivar - Desactivar cliente
- ✅ PATCH /api/clientes/{id}/activar - Activar cliente
- ✅ DELETE /api/clientes/{id} - Eliminar cliente exitosamente

**Total: 12 tests de integración**

## Ejecución de Tests

### Ejecutar todos los tests
\`\`\`bash
cd clientes-service
mvn test
\`\`\`

### Ejecutar tests de una clase específica
\`\`\`bash
mvn test -Dtest=ClienteServiceTest
\`\`\`

### Ejecutar un test específico
\`\`\`bash
mvn test -Dtest=ClienteServiceTest#crearCliente_Exitoso
\`\`\`

### Ver reporte de cobertura
\`\`\`bash
mvn test jacoco:report
# El reporte se genera en: target/site/jacoco/index.html
\`\`\`

## Configuración de Tests

### application-test.yml

Configuración específica para el entorno de testing:
- Base de datos H2 en memoria
- DDL auto create-drop
- Logs de SQL habilitados
- Seguridad simplificada

## Buenas Prácticas Implementadas

1. **Nombres descriptivos:** Cada test tiene un nombre que describe claramente qué prueba
2. **@DisplayName:** Descripciones legibles en español para los reportes
3. **Given-When-Then:** Estructura clara de preparación, ejecución y verificación
4. **AssertJ:** Assertions fluidas y legibles
5. **Verificación de mocks:** Siempre verificamos que los mocks fueron llamados correctamente
6. **Tests independientes:** Cada test es independiente y puede ejecutarse solo
7. **@BeforeEach:** Preparación común en setUp() para evitar duplicación

## Próximos Pasos

Este patrón de testing debe replicarse en los demás microservicios:
- depositos-service
- camiones-service
- tarifas-service
- solicitudes-service
- rutas-service

## Métricas de Calidad

**Objetivo de cobertura:** Mínimo 80% de cobertura de código

**Tests por microservicio:**
- Mínimo 10 tests unitarios (Service)
- Mínimo 8 tests de integración (Controller)

## Recursos Adicionales

- [JUnit 5 User Guide](https://junit.org/junit5/docs/current/user-guide/)
- [Mockito Documentation](https://javadoc.io/doc/org.mockito/mockito-core/latest/org/mockito/Mockito.html)
- [Spring Boot Testing](https://docs.spring.io/spring-boot/docs/current/reference/html/features.html#features.testing)
- [AssertJ Documentation](https://assertj.github.io/doc/)
