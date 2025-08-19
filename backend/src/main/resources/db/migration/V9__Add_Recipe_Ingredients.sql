-- Stwórz tabelę składników przepisów
CREATE TABLE recipe_ingredients (
    id BIGSERIAL PRIMARY KEY,
    recipe_id BIGINT NOT NULL,
    name VARCHAR(255) NOT NULL,
    quantity DECIMAL(10,3) NOT NULL,
    unit VARCHAR(100) NOT NULL,
    original_text VARCHAR(1000),
    category_id VARCHAR(255),
    has_custom_unit BOOLEAN NOT NULL DEFAULT FALSE,
    display_order INTEGER NOT NULL DEFAULT 0,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_recipe_ingredients_recipe
        FOREIGN KEY (recipe_id) REFERENCES recipes(id) ON DELETE CASCADE
);

-- Stwórz indeksy
CREATE INDEX idx_recipe_ingredients_recipe_id ON recipe_ingredients(recipe_id);
CREATE INDEX idx_recipe_ingredients_name ON recipe_ingredients(name);
CREATE INDEX idx_recipe_ingredients_category ON recipe_ingredients(category_id);