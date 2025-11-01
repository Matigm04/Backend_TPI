-- Script de datos de prueba para camiones-service

-- Transportistas
INSERT INTO transportista (nombre, apellido, documento_tipo, documento_numero, telefono, email, licencia_conducir, fecha_vencimiento_licencia, fecha_registro, activo) VALUES
('Roberto', 'Gómez', 'DNI', '20123456', '+54 11 1111-2222', 'roberto.gomez@transport.com', 'B2-12345678', '2026-12-31', CURRENT_TIMESTAMP, true),
('Diego', 'Torres', 'DNI', '20234567', '+54 11 2222-3333', 'diego.torres@transport.com', 'B2-23456789', '2026-06-30', CURRENT_TIMESTAMP, true),
('Marcelo', 'Silva', 'DNI', '20345678', '+54 11 3333-4444', 'marcelo.silva@transport.com', 'B2-34567890', '2025-12-31', CURRENT_TIMESTAMP, true),
('Fernando', 'Ruiz', 'DNI', '20456789', '+54 11 4444-5555', 'fernando.ruiz@transport.com', 'B2-45678901', '2027-03-31', CURRENT_TIMESTAMP, true),
('Gustavo', 'Morales', 'DNI', '20567890', '+54 11 5555-6666', 'gustavo.morales@transport.com', 'B2-56789012', '2026-09-30', CURRENT_TIMESTAMP, true),
('Javier', 'Castro', 'DNI', '20678901', '+54 11 6666-7777', 'javier.castro@transport.com', 'B2-67890123', '2025-06-30', CURRENT_TIMESTAMP, false);

-- Camiones con diferentes capacidades
INSERT INTO camion (dominio, marca, modelo, anio, capacidad_peso_kg, capacidad_volumen_m3, consumo_combustible_km_litro, costo_base_por_km, disponible, transportista_id, fecha_registro, activo) VALUES
('AB123CD', 'Mercedes-Benz', 'Actros 2651', 2020, 25000, 80, 2.5, 150.00, true, 1, CURRENT_TIMESTAMP, true),
('EF456GH', 'Scania', 'R450', 2021, 30000, 90, 2.8, 180.00, true, 2, CURRENT_TIMESTAMP, true),
('IJ789KL', 'Volvo', 'FH16', 2019, 28000, 85, 2.6, 170.00, false, 3, CURRENT_TIMESTAMP, true),
('MN012OP', 'Iveco', 'Stralis', 2022, 20000, 70, 3.0, 140.00, true, 4, CURRENT_TIMESTAMP, true),
('QR345ST', 'Mercedes-Benz', 'Axor 2544', 2018, 22000, 75, 2.7, 145.00, true, 5, CURRENT_TIMESTAMP, true),
('UV678WX', 'Scania', 'G410', 2020, 26000, 82, 2.9, 160.00, false, 1, CURRENT_TIMESTAMP, true),
('YZ901AB', 'Volvo', 'FM380', 2021, 24000, 78, 2.8, 155.00, true, 2, CURRENT_TIMESTAMP, true),
('CD234EF', 'Iveco', 'Trakker', 2019, 32000, 95, 2.4, 190.00, true, 3, CURRENT_TIMESTAMP, true);
