CREATE TABLE IF NOT EXISTS recipe_image_references (
    id SERIAL PRIMARY KEY,
    image_url VARCHAR(512) NOT NULL UNIQUE,
    storage_path VARCHAR(512) NOT NULL,
    reference_count INT NOT NULL DEFAULT 1,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_recipe_image_references_image_url ON recipe_image_references(image_url);

CREATE INDEX IF NOT EXISTS idx_recipe_image_references_reference_count ON recipe_image_references(reference_count);