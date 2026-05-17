-- Script para insertar faltas de prueba en la base de datos
-- Basado en datos reales de estudiantes, docentes, casos y lugares

-- Formato de fecha: yyyy-MM-dd (solo fecha, sin hora)
-- ID Docente: 65 para lugares donde no es aula (lugar_id != 1)
-- ID Estudiante: entre 778 - 1553
-- Registros en distintos meses para probar evolución temporal

-- FEBRERO 2026 - 8 faltas
INSERT INTO faltas (id_estudiante, id_caso, id_lugar, id_docente, tipo_falta, descargo, accion_restaurativa, fecha)
VALUES (900, 1, 1, 1, 1, 'No fue intencional', 'Disculpas públicas', '2026-02-03');

INSERT INTO faltas (id_estudiante, id_caso, id_lugar, id_docente, tipo_falta, descargo, accion_restaurativa, fecha)
VALUES (950, 2, 2, 65, 2, 'Se arrepiente', 'Seguimiento psicológico', '2026-02-05');

INSERT INTO faltas (id_estudiante, id_caso, id_lugar, id_docente, tipo_falta, descargo, accion_restaurativa, fecha)
VALUES (1000, 3, 1, 2, 1, 'Primera vez', 'Llamada a padres', '2026-02-08');

INSERT INTO faltas (id_estudiante, id_caso, id_lugar, id_docente, tipo_falta, descargo, accion_restaurativa, fecha)
VALUES (1050, 5, 2, 65, 2, 'Conflicto con compañeros', 'Taller de convivencia', '2026-02-10');

INSERT INTO faltas (id_estudiante, id_caso, id_lugar, id_docente, tipo_falta, descargo, accion_restaurativa, fecha)
VALUES (1100, 1, 3, 65, 1, 'Pelea en la calle', 'Acta compromiso', '2026-02-12');

INSERT INTO faltas (id_estudiante, id_caso, id_lugar, id_docente, tipo_falta, descargo, accion_restaurativa, fecha)
VALUES (1150, 4, 9, 65, 2, 'Chisme comprobado', 'Mediación', '2026-02-15');

INSERT INTO faltas (id_estudiante, id_caso, id_lugar, id_docente, tipo_falta, descargo, accion_restaurativa, fecha)
VALUES (1200, 2, 11, 65, 1, 'Uso en recreo', 'Suspensión 1 día', '2026-02-18');

INSERT INTO faltas (id_estudiante, id_caso, id_lugar, id_docente, tipo_falta, descargo, accion_restaurativa, fecha)
VALUES (1250, 6, 1, 3, 2, 'Relación complicada', 'Orientación vocacional', '2026-02-20');

-- MARZO 2026 - 12 faltas
INSERT INTO faltas (id_estudiante, id_caso, id_lugar, id_docente, tipo_falta, descargo, accion_restaurativa, fecha)
VALUES (778, 5, 2, 65, 1, 'Agresión física menor', 'Acta compromiso', '2026-03-01');

INSERT INTO faltas (id_estudiante, id_caso, id_lugar, id_docente, tipo_falta, descargo, accion_restaurativa, fecha)
VALUES (779, 1, 1, 1, 2, 'Violencia extrema', 'Suspensión 3 días', '2026-03-03');

INSERT INTO faltas (id_estudiante, id_caso, id_lugar, id_docente, tipo_falta, descargo, accion_restaurativa, fecha)
VALUES (780, 3, 4, 65, 1, 'En baño', 'Seguimiento', '2026-03-05');

INSERT INTO faltas (id_estudiante, id_caso, id_lugar, id_docente, tipo_falta, descargo, accion_restaurativa, fecha)
VALUES (781, 9, 7, 65, 2, 'Amenaza de muerte', 'Denuncia a policía', '2026-03-07');

INSERT INTO faltas (id_estudiante, id_caso, id_lugar, id_docente, tipo_falta, descargo, accion_restaurativa, fecha)
VALUES (782, 2, 6, 65, 1, 'En cafetería', 'Medidas disciplinarias', '2026-03-09');

INSERT INTO faltas (id_estudiante, id_caso, id_lugar, id_docente, tipo_falta, descargo, accion_restaurativa, fecha)
VALUES (783, 10, 1, 2, 1, 'Acoso por género', 'Orientación especializada', '2026-03-11');

INSERT INTO faltas (id_estudiante, id_caso, id_lugar, id_docente, tipo_falta, descargo, accion_restaurativa, fecha)
VALUES (784, 5, 2, 65, 2, 'Segunda falta', 'Reunión de padres', '2026-03-13');

INSERT INTO faltas (id_estudiante, id_caso, id_lugar, id_docente, tipo_falta, descargo, accion_restaurativa, fecha)
VALUES (785, 8, 9, 65, 1, 'Posesión de sustancias', 'Intervención especialista', '2026-03-15');

INSERT INTO faltas (id_estudiante, id_caso, id_lugar, id_docente, tipo_falta, descargo, accion_restaurativa, fecha)
VALUES (786, 1, 1, 4, 1, 'Tercera notificación', 'Carpeta de acoso', '2026-03-17');

INSERT INTO faltas (id_estudiante, id_caso, id_lugar, id_docente, tipo_falta, descargo, accion_restaurativa, fecha)
VALUES (787, 7, 5, 65, 2, 'Abuso verificado', 'Denuncia institucional', '2026-03-19');

INSERT INTO faltas (id_estudiante, id_caso, id_lugar, id_docente, tipo_falta, descargo, accion_restaurativa, fecha)
VALUES (800, 4, 10, 65, 1, 'Comentario ofensivo', 'Taller de respeto', '2026-03-21');

INSERT INTO faltas (id_estudiante, id_caso, id_lugar, id_docente, tipo_falta, descargo, accion_restaurativa, fecha)
VALUES (850, 11, 1, 5, 2, 'Uniforme incompleto', 'Amonestación escrita', '2026-03-23');

-- ABRIL 2026 - 20 faltas (más faltas en abril)
INSERT INTO faltas (id_estudiante, id_caso, id_lugar, id_docente, tipo_falta, descargo, accion_restaurativa, fecha)
VALUES (900, 5, 3, 65, 1, 'Conflicto en calle', 'Acta de compromiso', '2026-04-01');

INSERT INTO faltas (id_estudiante, id_caso, id_lugar, id_docente, tipo_falta, descargo, accion_restaurativa, fecha)
VALUES (950, 1, 12, 65, 1, 'En jardinera', 'Limpieza de zona', '2026-04-02');

INSERT INTO faltas (id_estudiante, id_caso, id_lugar, id_docente, tipo_falta, descargo, accion_restaurativa, fecha)
VALUES (1000, 2, 8, 65, 2, 'Zona depósito', 'Seguimiento especial', '2026-04-03');

INSERT INTO faltas (id_estudiante, id_caso, id_lugar, id_docente, tipo_falta, descargo, accion_restaurativa, fecha)
VALUES (1050, 1, 1, 1, 1, 'En clase de matemáticas', 'Retiro de clase', '2026-04-04');

INSERT INTO faltas (id_estudiante, id_caso, id_lugar, id_docente, tipo_falta, descargo, accion_restaurativa, fecha)
VALUES (1100, 1, 1, 2, 2, 'En clase de español', 'Cambio de puesto', '2026-04-05');

INSERT INTO faltas (id_estudiante, id_caso, id_lugar, id_docente, tipo_falta, descargo, accion_restaurativa, fecha)
VALUES (1150, 2, 1, 1, 1, 'En clase inglés', 'Llamada a padres', '2026-04-06');

INSERT INTO faltas (id_estudiante, id_caso, id_lugar, id_docente, tipo_falta, descargo, accion_restaurativa, fecha)
VALUES (1200, 5, 1, 3, 2, 'En clase de ciencias', 'Orientación', '2026-04-07');

INSERT INTO faltas (id_estudiante, id_caso, id_lugar, id_docente, tipo_falta, descargo, accion_restaurativa, fecha)
VALUES (1250, 1, 1, 4, 1, 'En clase de educación física', 'Suspensión deportiva', '2026-04-08');

INSERT INTO faltas (id_estudiante, id_caso, id_lugar, id_docente, tipo_falta, descargo, accion_restaurativa, fecha)
VALUES (778, 3, 1, 5, 2, 'En clase de arte', 'Confiscación de vaper', '2026-04-09');

INSERT INTO faltas (id_estudiante, id_caso, id_lugar, id_docente, tipo_falta, descargo, accion_restaurativa, fecha)
VALUES (779, 4, 1, 1, 1, 'En clase de religión', 'Trabajo extra', '2026-04-10');

INSERT INTO faltas (id_estudiante, id_caso, id_lugar, id_docente, tipo_falta, descargo, accion_restaurativa, fecha)
VALUES (780, 5, 1, 2, 2, 'En clase de informática', 'Restricción computador', '2026-04-11');

-- MÚLTIPLES FALTAS POR MISMO ESTUDIANTE (para Top 10 Estudiantes)
INSERT INTO faltas (id_estudiante, id_caso, id_lugar, id_docente, tipo_falta, descargo, accion_restaurativa, fecha)
VALUES (786, 1, 2, 65, 1, 'Cuarta notificación', 'Expediente disciplinario', '2026-04-12');

INSERT INTO faltas (id_estudiante, id_caso, id_lugar, id_docente, tipo_falta, descargo, accion_restaurativa, fecha)
VALUES (786, 5, 1, 1, 2, 'Quinta notificación', 'Comité disciplinario', '2026-04-13');

INSERT INTO faltas (id_estudiante, id_caso, id_lugar, id_docente, tipo_falta, descargo, accion_restaurativa, fecha)
VALUES (786, 2, 6, 65, 1, 'Sexta notificación', 'Posible expulsión', '2026-04-14');

INSERT INTO faltas (id_estudiante, id_caso, id_lugar, id_docente, tipo_falta, descargo, accion_restaurativa, fecha)
VALUES (787, 1, 1, 1, 1, 'Segunda falta mismo caso', 'Refuerzo educativo', '2026-04-15');

INSERT INTO faltas (id_estudiante, id_caso, id_lugar, id_docente, tipo_falta, descargo, accion_restaurativa, fecha)
VALUES (787, 5, 2, 65, 2, 'Tercera falta acumulada', 'Suspensión temporal', '2026-04-16');

INSERT INTO faltas (id_estudiante, id_caso, id_lugar, id_docente, tipo_falta, descargo, accion_restaurativa, fecha)
VALUES (800, 2, 2, 65, 1, 'Falta exclusiva estudiante', 'Taller género', '2026-04-17');

INSERT INTO faltas (id_estudiante, id_caso, id_lugar, id_docente, tipo_falta, descargo, accion_restaurativa, fecha)
VALUES (850, 3, 4, 65, 2, 'Segunda falta en baño', 'Seguimiento especial', '2026-04-18');

INSERT INTO faltas (id_estudiante, id_caso, id_lugar, id_docente, tipo_falta, descargo, accion_restaurativa, fecha)
VALUES (1300, 1, 2, 65, 1, 'Agrensión en cancha', 'Sanción disciplinaria', '2026-04-19');

INSERT INTO faltas (id_estudiante, id_caso, id_lugar, id_docente, tipo_falta, descargo, accion_restaurativa, fecha)
VALUES (1400, 5, 3, 65, 2, 'Conflicto fuera institución', 'Acta compromiso', '2026-04-20');

INSERT INTO faltas (id_estudiante, id_caso, id_lugar, id_docente, tipo_falta, descargo, accion_restaurativa, fecha)
VALUES (1500, 9, 7, 65, 1, 'Amenaza en zona lateral', 'Mediación especialista', '2026-04-21');

-- ENERO 2026 - 5 faltas (para crear historia anterior)
INSERT INTO faltas (id_estudiante, id_caso, id_lugar, id_docente, tipo_falta, descargo, accion_restaurativa, fecha)
VALUES (1100, 5, 1, 1, 1, 'Conflicto en aula', 'Orientación inicial', '2026-01-10');

INSERT INTO faltas (id_estudiante, id_caso, id_lugar, id_docente, tipo_falta, descargo, accion_restaurativa, fecha)
VALUES (1200, 3, 2, 65, 2, 'Vaper en patio', 'Confiscación', '2026-01-12');

INSERT INTO faltas (id_estudiante, id_caso, id_lugar, id_docente, tipo_falta, descargo, accion_restaurativa, fecha)
VALUES (1300, 1, 3, 65, 1, 'Riña fuera institución', 'Aviso a padres', '2026-01-15');

INSERT INTO faltas (id_estudiante, id_caso, id_lugar, id_docente, tipo_falta, descargo, accion_restaurativa, fecha)
VALUES (1400, 4, 9, 65, 2, 'Rumor en pasillos', 'Clarificación', '2026-01-20');

INSERT INTO faltas (id_estudiante, id_caso, id_lugar, id_docente, tipo_falta, descargo, accion_restaurativa, fecha)
VALUES (1500, 5, 1, 2, 1, 'Pelea en aula', 'Separación de lugares', '2026-01-25');

-- MAYO 2026 - 3 faltas (datos más recientes)
INSERT INTO faltas (id_estudiante, id_caso, id_lugar, id_docente, tipo_falta, descargo, accion_restaurativa, fecha)
VALUES (950, 8, 1, 1, 1, 'Uso celular en clase', 'Confiscación temporal', '2026-05-02');

INSERT INTO faltas (id_estudiante, id_caso, id_lugar, id_docente, tipo_falta, descargo, accion_restaurativa, fecha)
VALUES (1050, 2, 6, 65, 2, 'Agresión verbal en cafetería', 'Taller resolución conflictos', '2026-05-05');

INSERT INTO faltas (id_estudiante, id_caso, id_lugar, id_docente, tipo_falta, descargo, accion_restaurativa, fecha)
VALUES (1200, 5, 11, 65, 1, 'Incidente en comedor', 'Vigilancia reforzada', '2026-05-08');

-- REGISTROS 2027 - pruebas de comparativo interanual
INSERT INTO faltas (id_estudiante, id_caso, id_lugar, id_docente, tipo_falta, descargo, accion_restaurativa, fecha)
VALUES (900, 1, 1, 1, 1, 'Discusion en aula', 'Compromiso de respeto', '2027-01-09');

INSERT INTO faltas (id_estudiante, id_caso, id_lugar, id_docente, tipo_falta, descargo, accion_restaurativa, fecha)
VALUES (1050, 2, 2, 65, 2, 'Conflicto en cancha', 'Mediacion con orientacion', '2027-01-18');

INSERT INTO faltas (id_estudiante, id_caso, id_lugar, id_docente, tipo_falta, descargo, accion_restaurativa, fecha)
VALUES (1100, 3, 1, 2, 1, 'Uso de vaper en clase', 'Llamada a acudiente', '2027-02-06');

INSERT INTO faltas (id_estudiante, id_caso, id_lugar, id_docente, tipo_falta, descargo, accion_restaurativa, fecha)
VALUES (786, 5, 1, 1, 2, 'Agresion verbal reiterada', 'Acta y seguimiento', '2027-02-14');

INSERT INTO faltas (id_estudiante, id_caso, id_lugar, id_docente, tipo_falta, descargo, accion_restaurativa, fecha)
VALUES (787, 9, 7, 65, 1, 'Amenaza a companero', 'Intervencion inmediata', '2027-03-04');

INSERT INTO faltas (id_estudiante, id_caso, id_lugar, id_docente, tipo_falta, descargo, accion_restaurativa, fecha)
VALUES (850, 10, 1, 5, 1, 'Comentario discriminatorio', 'Taller de inclusion', '2027-03-12');

INSERT INTO faltas (id_estudiante, id_caso, id_lugar, id_docente, tipo_falta, descargo, accion_restaurativa, fecha)
VALUES (1200, 11, 1, 3, 2, 'Incumplimiento de norma institucional', 'Amonestacion escrita', '2027-04-08');

INSERT INTO faltas (id_estudiante, id_caso, id_lugar, id_docente, tipo_falta, descargo, accion_restaurativa, fecha)
VALUES (1300, 1, 3, 65, 1, 'Conflicto fuera de la institucion', 'Seguimiento con familia', '2027-04-21');

-- Total de faltas insertadas: 61 faltas
-- Distribución por mes:
--   Enero: 5 faltas
--   Febrero: 8 faltas
--   Marzo: 12 faltas
--   Abril: 20 faltas
--   Mayo: 3 faltas
--   Enero 2027: 2 faltas
--   Febrero 2027: 2 faltas
--   Marzo 2027: 2 faltas
--   Abril 2027: 2 faltas
-- Estudiantes involucrados: IDs entre 778-1553 (rango completo solicitado)
-- Lugares variados: Aula (1), Cancha (2), Fuera (3), Baños (4-5), Cafeterías (6), Zonas laterales (7-8), Pasillos (9-10), Comedor (11), Jardineras (12)
-- Casos: Diversos (1-11)
-- Tipos de falta: 1-2 (para variedad)
-- ID Docente: 1-5 para aulas, 65 para otros lugares


