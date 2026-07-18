-- ==========================================
-- ADICION: Campos para gestion de guardias en zonas
-- ==========================================
ALTER TABLE "usuarios" ADD COLUMN "campus_id" INTEGER REFERENCES "campus"("id") ON DELETE SET NULL;
ALTER TABLE "usuarios" ADD COLUMN "zona_id" INTEGER REFERENCES "zonas"("id") ON DELETE SET NULL;
ALTER TABLE "usuarios" ADD COLUMN "tipo_guardia" VARCHAR(10) CHECK ("tipo_guardia" IN ('entrada', 'salida'));

-- Un guardia de entrada y un guardia de salida por zona maximo (o uno solo por zona si es entrada/salida)
ALTER TABLE "usuarios" ADD CONSTRAINT "uq_usuarios_zona_tipo_guardia" UNIQUE ("zona_id", "tipo_guardia");
