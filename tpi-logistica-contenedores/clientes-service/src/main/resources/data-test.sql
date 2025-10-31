-- Script de datos de prueba para clientes-service
-- Se ejecuta automáticamente con spring.jpa.defer-datasource-initialization=true

-- Clientes de prueba
INSERT INTO cliente (nombre, apellido, email, telefono, documento_tipo, documento_numero, direccion, fecha_registro, activo) VALUES
('Juan', 'Pérez', 'juan.perez@email.com', '+54 11 1234-5678', 'DNI', '12345678', 'Av. Corrientes 1234, CABA', CURRENT_TIMESTAMP, true),
('María', 'González', 'maria.gonzalez@email.com', '+54 11 2345-6789', 'DNI', '23456789', 'Av. Santa Fe 2345, CABA', CURRENT_TIMESTAMP, true),
('Carlos', 'Rodríguez', 'carlos.rodriguez@email.com', '+54 11 3456-7890', 'DNI', '34567890', 'Av. Rivadavia 3456, CABA', CURRENT_TIMESTAMP, true),
('Ana', 'Martínez', 'ana.martinez@email.com', '+54 11 4567-8901', 'DNI', '45678901', 'Av. Belgrano 4567, CABA', CURRENT_TIMESTAMP, true),
('Luis', 'Fernández', 'luis.fernandez@email.com', '+54 11 5678-9012', 'DNI', '56789012', 'Av. Callao 5678, CABA', CURRENT_TIMESTAMP, true),
('Laura', 'López', 'laura.lopez@email.com', '+54 11 6789-0123', 'DNI', '67890123', 'Av. Pueyrredón 6789, CABA', CURRENT_TIMESTAMP, false),
('Pedro', 'Sánchez', 'pedro.sanchez@email.com', '+54 11 7890-1234', 'DNI', '78901234', 'Av. Córdoba 7890, CABA', CURRENT_TIMESTAMP, true),
('Sofía', 'Ramírez', 'sofia.ramirez@email.com', '+54 11 8901-2345', 'DNI', '89012345', 'Av. Las Heras 8901, CABA', CURRENT_TIMESTAMP, true);
