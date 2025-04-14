-- src/main/resources/db/migration/V6__fix_recipe_image_references_id_type.sql
-- Zmień typ kolumny na BIGINT zachowując autoinkrementację
ALTER TABLE recipe_image_references ALTER COLUMN id TYPE BIGINT;

-- Upewnij się, że sekwencja jest używana poprawnie
ALTER SEQUENCE recipe_image_references_id_seq AS BIGINT;