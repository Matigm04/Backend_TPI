-- Script de datos de prueba para solicitudes-service

-- Contenedores
INSERT INTO contenedor (codigo_identificacion, peso_kg, volumen_m3, largo_m, ancho_m, alto_m, estado, descripcion, fecha_creacion, deposito_id) VALUES
('CONT-2025-001', 8500, 25, 6.0, 2.4, 2.6, 'EN_TRANSITO', 'Contenedor estándar 20 pies', CURRENT_TIMESTAMP, NULL),
('CONT-2025-002', 15000, 45, 12.0, 2.4, 2.6, 'EN_DEPOSITO', 'Contenedor 40 pies', CURRENT_TIMESTAMP, 2),
('CONT-2025-003', 12000, 35, 9.0, 2.4, 2.6, 'ENTREGADO', 'Contenedor 30 pies', CURRENT_TIMESTAMP, NULL),
('CONT-2025-004', 9500, 28, 6.0, 2.4, 2.9, 'PENDIENTE_RETIRO', 'Contenedor high cube 20 pies', CURRENT_TIMESTAMP, NULL),
('CONT-2025-005', 18000, 50, 12.0, 2.4, 2.9, 'EN_TRANSITO', 'Contenedor high cube 40 pies', CURRENT_TIMESTAMP, NULL);

-- Solicitudes en diferentes estados
INSERT INTO solicitud (numero_solicitud, contenedor_id, cliente_id, ubicacion_origen, ubicacion_destino, estado, fecha_solicitud, fecha_programada, fecha_entrega_estimada, fecha_entrega_real, costo_estimado, costo_final, tiempo_estimado_horas, tiempo_real_horas, observaciones, activo, tarifa_id) VALUES
('SOL-2025-001', 1, 1, 'Av. Corrientes 1234, CABA (-34.6037, -58.3816)', 'Ruta 9 Km 45, San Fernando (-34.4414, -58.5569)', 'EN_TRANSITO', CURRENT_TIMESTAMP - INTERVAL '2 days', CURRENT_TIMESTAMP - INTERVAL '1 day', CURRENT_TIMESTAMP + INTERVAL '1 day', NULL, 45000.00, NULL, 24, NULL, 'Solicitud en tránsito hacia depósito norte', true, 1),
('SOL-2025-002', 2, 2, 'Av. Santa Fe 2345, CABA (-34.5945, -58.3974)', 'Ruta 9 Km 680, Córdoba (-31.4201, -64.1888)', 'PROGRAMADA', CURRENT_TIMESTAMP - INTERVAL '1 day', CURRENT_TIMESTAMP + INTERVAL '1 day', CURRENT_TIMESTAMP + INTERVAL '3 days', NULL, 125000.00, NULL, 48, NULL, 'Solicitud programada para envío a Córdoba', true, 2),
('SOL-2025-003', 3, 3, 'Av. Rivadavia 3456, CABA (-34.6092, -58.4370)', 'Av. Circunvalación 5000, Rosario (-32.9442, -60.6505)', 'ENTREGADA', CURRENT_TIMESTAMP - INTERVAL '10 days', CURRENT_TIMESTAMP - INTERVAL '8 days', CURRENT_TIMESTAMP - INTERVAL '6 days', CURRENT_TIMESTAMP - INTERVAL '6 days', 95000.00, 98500.00, 36, 38, 'Solicitud completada exitosamente', true, 2),
('SOL-2025-004', 4, 4, 'Av. Belgrano 4567, CABA (-34.6126, -58.3772)', 'Ruta 7 Km 1050, Mendoza (-32.8895, -68.8458)', 'PENDIENTE', CURRENT_TIMESTAMP, NULL, NULL, NULL, 180000.00, NULL, 72, NULL, 'Solicitud pendiente de confirmación', true, 3),
('SOL-2025-005', 5, 5, 'Av. Callao 5678, CABA (-34.5989, -58.3926)', 'Av. 520 y 137, La Plata (-34.9214, -57.9544)', 'EN_TRANSITO', CURRENT_TIMESTAMP - INTERVAL '1 day', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP + INTERVAL '2 days', NULL, 55000.00, NULL, 18, NULL, 'Solicitud en tránsito hacia La Plata', true, 2);

-- Historial de estados de contenedores
INSERT INTO historial_estado_contenedor (contenedor_id, estado_anterior, estado_nuevo, ubicacion, tramo_id, fecha_hora, observaciones, usuario_registro) VALUES
(1, 'PENDIENTE_RETIRO', 'EN_TRANSITO', 'Av. Corrientes 1234, CABA', NULL, CURRENT_TIMESTAMP - INTERVAL '2 days', 'Contenedor retirado del origen', 'sistema'),
(2, 'PENDIENTE_RETIRO', 'EN_TRANSITO', 'Av. Santa Fe 2345, CABA', NULL, CURRENT_TIMESTAMP - INTERVAL '3 days', 'Contenedor retirado del origen', 'sistema'),
(2, 'EN_TRANSITO', 'EN_DEPOSITO', 'Depósito Norte GBA', NULL, CURRENT_TIMESTAMP - INTERVAL '2 days', 'Contenedor ingresado a depósito', 'sistema'),
(3, 'PENDIENTE_RETIRO', 'EN_TRANSITO', 'Av. Rivadavia 3456, CABA', NULL, CURRENT_TIMESTAMP - INTERVAL '10 days', 'Contenedor retirado del origen', 'sistema'),
(3, 'EN_TRANSITO', 'ENTREGADO', 'Av. Circunvalación 5000, Rosario', NULL, CURRENT_TIMESTAMP - INTERVAL '6 days', 'Contenedor entregado en destino', 'sistema'),
(5, 'PENDIENTE_RETIRO', 'EN_TRANSITO', 'Av. Callao 5678, CABA', NULL, CURRENT_TIMESTAMP - INTERVAL '1 day', 'Contenedor retirado del origen', 'sistema');
