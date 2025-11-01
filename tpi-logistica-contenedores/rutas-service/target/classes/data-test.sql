-- Script de datos de prueba para rutas-service

-- Rutas para las solicitudes
INSERT INTO ruta (solicitud_id, cantidad_tramos, cantidad_depositos, distancia_total_km, costo_total_estimado, costo_total_real, fecha_creacion, estado) VALUES
(1, 2, 1, 85.5, 45000.00, NULL, CURRENT_TIMESTAMP - INTERVAL '2 days', 'EN_PROGRESO'),
(2, 3, 2, 720.0, 125000.00, NULL, CURRENT_TIMESTAMP - INTERVAL '1 day', 'ESTIMADA'),
(3, 2, 1, 310.0, 95000.00, 98500.00, CURRENT_TIMESTAMP - INTERVAL '10 days', 'COMPLETADA'),
(5, 1, 0, 65.0, 55000.00, NULL, CURRENT_TIMESTAMP - INTERVAL '1 day', 'EN_PROGRESO');

-- Tramos de las rutas
-- Ruta 1 (SOL-2025-001): CABA -> Depósito Norte -> San Fernando
INSERT INTO tramo (ruta_id, numero_orden, ubicacion_origen, ubicacion_destino, tipo_tramo, estado, distancia_km, costo_aproximado, costo_real, fecha_hora_inicio_estimada, fecha_hora_fin_estimada, fecha_hora_inicio_real, fecha_hora_fin_real, observaciones, camion_id) VALUES
(1, 1, 'Av. Corrientes 1234, CABA (-34.6037, -58.3816)', 'Depósito Norte GBA (-34.4414, -58.5569)', 'ORIGEN_DEPOSITO', 'FINALIZADO', 25.5, 15000.00, 15200.00, CURRENT_TIMESTAMP - INTERVAL '2 days', CURRENT_TIMESTAMP - INTERVAL '2 days' + INTERVAL '3 hours', CURRENT_TIMESTAMP - INTERVAL '2 days', CURRENT_TIMESTAMP - INTERVAL '2 days' + INTERVAL '3 hours', 'Tramo completado sin incidentes', 1),
(1, 2, 'Depósito Norte GBA (-34.4414, -58.5569)', 'Ruta 9 Km 45, San Fernando (-34.4414, -58.5569)', 'DEPOSITO_DESTINO', 'INICIADO', 60.0, 30000.00, NULL, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP + INTERVAL '4 hours', CURRENT_TIMESTAMP - INTERVAL '1 hour', NULL, 'Tramo en progreso', 1);

-- Ruta 2 (SOL-2025-002): CABA -> Depósito Central -> Depósito Rosario -> Córdoba
INSERT INTO tramo (ruta_id, numero_orden, ubicacion_origen, ubicacion_destino, tipo_tramo, estado, distancia_km, costo_aproximado, costo_real, fecha_hora_inicio_estimada, fecha_hora_fin_estimada, fecha_hora_inicio_real, fecha_hora_fin_real, observaciones, camion_id) VALUES
(2, 1, 'Av. Santa Fe 2345, CABA (-34.5945, -58.3974)', 'Depósito Central CABA (-34.5989, -58.4371)', 'ORIGEN_DEPOSITO', 'ESTIMADO', 15.0, 8000.00, NULL, CURRENT_TIMESTAMP + INTERVAL '1 day', CURRENT_TIMESTAMP + INTERVAL '1 day' + INTERVAL '2 hours', NULL, NULL, 'Primer tramo estimado', NULL),
(2, 2, 'Depósito Central CABA (-34.5989, -58.4371)', 'Depósito Rosario (-32.9442, -60.6505)', 'DEPOSITO_DEPOSITO', 'ESTIMADO', 305.0, 55000.00, NULL, CURRENT_TIMESTAMP + INTERVAL '1 day' + INTERVAL '4 hours', CURRENT_TIMESTAMP + INTERVAL '2 days', NULL, NULL, 'Segundo tramo estimado', NULL),
(2, 3, 'Depósito Rosario (-32.9442, -60.6505)', 'Ruta 9 Km 680, Córdoba (-31.4201, -64.1888)', 'DEPOSITO_DESTINO', 'ESTIMADO', 400.0, 62000.00, NULL, CURRENT_TIMESTAMP + INTERVAL '2 days' + INTERVAL '2 hours', CURRENT_TIMESTAMP + INTERVAL '3 days', NULL, NULL, 'Tramo final estimado', NULL);

-- Ruta 3 (SOL-2025-003): CABA -> Depósito Sur -> Rosario (COMPLETADA)
INSERT INTO tramo (ruta_id, numero_orden, ubicacion_origen, ubicacion_destino, tipo_tramo, estado, distancia_km, costo_aproximado, costo_real, fecha_hora_inicio_estimada, fecha_hora_fin_estimada, fecha_hora_inicio_real, fecha_hora_fin_real, observaciones, camion_id) VALUES
(3, 1, 'Av. Rivadavia 3456, CABA (-34.6092, -58.4370)', 'Depósito Sur GBA (-34.6617, -58.3656)', 'ORIGEN_DEPOSITO', 'FINALIZADO', 12.0, 7000.00, 7200.00, CURRENT_TIMESTAMP - INTERVAL '10 days', CURRENT_TIMESTAMP - INTERVAL '10 days' + INTERVAL '2 hours', CURRENT_TIMESTAMP - INTERVAL '10 days', CURRENT_TIMESTAMP - INTERVAL '10 days' + INTERVAL '2 hours', 'Tramo completado', 2),
(3, 2, 'Depósito Sur GBA (-34.6617, -58.3656)', 'Av. Circunvalación 5000, Rosario (-32.9442, -60.6505)', 'DEPOSITO_DESTINO', 'FINALIZADO', 298.0, 88000.00, 91300.00, CURRENT_TIMESTAMP - INTERVAL '9 days', CURRENT_TIMESTAMP - INTERVAL '6 days', CURRENT_TIMESTAMP - INTERVAL '9 days', CURRENT_TIMESTAMP - INTERVAL '6 days', 'Tramo completado con estadía de 3 días en depósito', 2);

-- Ruta 5 (SOL-2025-005): CABA -> La Plata (directo, sin depósitos)
INSERT INTO tramo (ruta_id, numero_orden, ubicacion_origen, ubicacion_destino, tipo_tramo, estado, distancia_km, costo_aproximado, costo_real, fecha_hora_inicio_estimada, fecha_hora_fin_estimada, fecha_hora_inicio_real, fecha_hora_fin_real, observaciones, camion_id) VALUES
(5, 1, 'Av. Callao 5678, CABA (-34.5989, -58.3926)', 'Av. 520 y 137, La Plata (-34.9214, -57.9544)', 'ORIGEN_DESTINO', 'INICIADO', 65.0, 55000.00, NULL, CURRENT_TIMESTAMP - INTERVAL '1 day', CURRENT_TIMESTAMP + INTERVAL '1 day', CURRENT_TIMESTAMP - INTERVAL '1 day', NULL, 'Envío directo sin depósitos intermedios', 4);
