-- Script de datos de prueba para depositos-service

-- Depósitos en diferentes ubicaciones de Argentina
INSERT INTO deposito (nombre, direccion, latitud, longitud, capacidad_maxima_contenedores, contenedores_actuales, costo_estadia_diario, horario_apertura, horario_cierre, observaciones, activo) VALUES
('Depósito Central CABA', 'Av. Warnes 2500, CABA', -34.5989, -58.4371, 100, 15, 5000.00, '08:00', '18:00', 'Depósito principal en Capital Federal', true),
('Depósito Norte GBA', 'Ruta 9 Km 45, San Fernando', -34.4414, -58.5569, 80, 8, 4000.00, '07:00', 19:00', 'Depósito en zona norte', true),
('Depósito Sur GBA', 'Camino Negro 1234, Avellaneda', -34.6617, -58.3656, 60, 12, 3500.00, '08:00', '18:00', 'Depósito en zona sur', true),
('Depósito Oeste GBA', 'Ruta 3 Km 25, La Matanza', -34.7704, -58.6259, 70, 5, 3800.00, '08:00', '17:00', 'Depósito en zona oeste', true),
('Depósito Rosario', 'Av. Circunvalación 5000, Rosario', -32.9442, -60.6505, 90, 20, 4500.00, '08:00', '18:00', 'Depósito en Rosario, Santa Fe', true),
('Depósito Córdoba', 'Ruta 9 Km 680, Córdoba', -31.4201, -64.1888, 85, 10, 4200.00, '08:00', '18:00', 'Depósito en Córdoba Capital', true),
('Depósito Mendoza', 'Ruta 7 Km 1050, Mendoza', -32.8895, -68.8458, 50, 3, 3000.00, '09:00', '17:00', 'Depósito en Mendoza', true),
('Depósito La Plata', 'Av. 520 y 137, La Plata', -34.9214, -57.9544, 65, 7, 3600.00, '08:00', '18:00', 'Depósito en La Plata', true);
