-- Script de datos de prueba para tarifas-service

-- Tarifas vigentes
INSERT INTO tarifa (concepto, descripcion, valor, unidad, rango_peso_min_kg, rango_peso_max_kg, rango_volumen_min_m3, rango_volumen_max_m3, fecha_vigencia_desde, fecha_vigencia_hasta, activo) VALUES
-- Tarifas base por kilómetro según volumen
('COSTO_KM_BASE_PEQUEÑO', 'Costo por km para contenedores pequeños', 80.00, 'ARS/KM', 0, 10000, 0, 30, '2025-01-01', '2025-12-31', true),
('COSTO_KM_BASE_MEDIANO', 'Costo por km para contenedores medianos', 120.00, 'ARS/KM', 10001, 20000, 31, 60, '2025-01-01', '2025-12-31', true),
('COSTO_KM_BASE_GRANDE', 'Costo por km para contenedores grandes', 180.00, 'ARS/KM', 20001, 35000, 61, 100, '2025-01-01', '2025-12-31', true),

-- Costo de combustible
('COSTO_COMBUSTIBLE_LITRO', 'Precio del litro de combustible', 950.00, 'ARS/LITRO', NULL, NULL, NULL, NULL, '2025-01-01', '2025-12-31', true),

-- Cargo fijo de gestión por cantidad de tramos
('CARGO_GESTION_1_TRAMO', 'Cargo fijo por gestión - 1 tramo', 5000.00, 'ARS', NULL, NULL, NULL, NULL, '2025-01-01', '2025-12-31', true),
('CARGO_GESTION_2_TRAMOS', 'Cargo fijo por gestión - 2 tramos', 8000.00, 'ARS', NULL, NULL, NULL, NULL, '2025-01-01', '2025-12-31', true),
('CARGO_GESTION_3_TRAMOS', 'Cargo fijo por gestión - 3+ tramos', 12000.00, 'ARS', NULL, NULL, NULL, NULL, '2025-01-01', '2025-12-31', true),

-- Estadía en depósito (ya incluida en depósitos, pero como referencia)
('ESTADIA_DEPOSITO_ESTANDAR', 'Costo promedio de estadía diaria', 4000.00, 'ARS/DIA', NULL, NULL, NULL, NULL, '2025-01-01', '2025-12-31', true),

-- Tarifas históricas (no vigentes)
('COSTO_KM_BASE_PEQUEÑO', 'Costo por km para contenedores pequeños (2024)', 70.00, 'ARS/KM', 0, 10000, 0, 30, '2024-01-01', '2024-12-31', false),
('COSTO_COMBUSTIBLE_LITRO', 'Precio del litro de combustible (2024)', 850.00, 'ARS/LITRO', NULL, NULL, NULL, NULL, '2024-01-01', '2024-12-31', false);
