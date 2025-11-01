-------------------------------------------------------------------------------------------
🔐 Keycloak
Propósito: Sistema de autenticación y autorización (Identity and Access Management - IAM)

Funciones principales:

Autenticación centralizada - Usuarios se autentican una vez para todos los microservicios
Gestión de usuarios y roles - Creas usuarios (cliente1, operador1, transportista1)
OAuth2 / OpenID Connect - Genera tokens JWT para autorizar requests
Single Sign-On (SSO) - Un login sirve para toda la aplicación
Control de acceso basado en roles (RBAC) - Define quién puede hacer qué


¿Qué hacen Swagger y Keycloak?
🔵 Swagger (SpringDoc OpenAPI)
Propósito: Documentación interactiva de tus APIs REST

Funciones principales:

Documentación automática - Genera documentación de todos tus endpoints a partir del código
Interfaz interactiva - Permite probar los endpoints directamente desde el navegador
Especificación OpenAPI - Crea especificaciones estándar de tu API
Testing sin Postman - Puedes enviar requests y ver responses sin herramientas externas
Ejemplo de uso en tu proyecto:

Aquí verás:

Todos los endpoints del servicio de clientes (/api/clientes)
Los métodos HTTP disponibles (GET, POST, PUT, DELETE)
Los parámetros requeridos
Los modelos de datos (Cliente, Contenedor, etc.)
Botón "Try it out" para ejecutar requests con autenticación JWT

¿Por qué los necesitas juntos?
Keycloak protege tus APIs - nadie puede acceder sin autenticarse
Swagger documenta cómo usar esas APIs protegidas
Swagger + Keycloak = Puedes probar endpoints autenticados desde el navegador
En Swagger UI puedes configurar el token JWT obtenido de Keycloak para probar endpoints protegidos sin usar Postman. 🚀

----------------------------------------------------------------------------------------------------------------------------

El comando docker-compose logs -f es el comando de diagnóstico correcto. Te conectaste a la "transmisión en vivo" de los logs de todos tus contenedores.

---------------------------------------------------------------------------------

