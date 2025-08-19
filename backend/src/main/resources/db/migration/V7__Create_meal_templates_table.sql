-- Tabela szablonów posiłków
CREATE TABLE meal_templates (
    id BIGSERIAL PRIMARY KEY,
    external_id VARCHAR(255) NOT NULL UNIQUE,
    name VARCHAR(255) NOT NULL,
    name_lower VARCHAR(255) NOT NULL, -- dla szybkich wyszukiwań
    instructions TEXT,
    meal_type VARCHAR(50),
    category VARCHAR(100),
    created_by VARCHAR(255),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    last_used TIMESTAMP,
    usage_count INTEGER NOT NULL DEFAULT 0,

    -- Wartości odżywcze
    calories DECIMAL(8,2),
    protein DECIMAL(8,2),
    fat DECIMAL(8,2),
    carbs DECIMAL(8,2),

    -- Indeksy dla wydajności
    CONSTRAINT chk_usage_count_positive CHECK (usage_count >= 0)
);

-- Tabela zdjęć szablonów posiłków
CREATE TABLE meal_template_photos (
    id BIGSERIAL PRIMARY KEY,
    meal_template_id BIGINT NOT NULL,
    photo_url TEXT NOT NULL,
    display_order INTEGER NOT NULL DEFAULT 0,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    FOREIGN KEY (meal_template_id) REFERENCES meal_templates(id) ON DELETE CASCADE
);

-- Tabela składników szablonów posiłków
CREATE TABLE meal_template_ingredients (
    id BIGSERIAL PRIMARY KEY,
    meal_template_id BIGINT NOT NULL,
    name VARCHAR(255) NOT NULL,
    quantity DECIMAL(10,3) NOT NULL,
    unit VARCHAR(50) NOT NULL,
    original_text TEXT,
    category_id VARCHAR(255),
    has_custom_unit BOOLEAN NOT NULL DEFAULT FALSE,
    display_order INTEGER NOT NULL DEFAULT 0,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    FOREIGN KEY (meal_template_id) REFERENCES meal_templates(id) ON DELETE CASCADE,
    CONSTRAINT chk_quantity_positive CHECK (quantity > 0)
);

-- Indeksy dla wydajności
CREATE INDEX idx_meal_templates_name_lower ON meal_templates(name_lower);
CREATE INDEX idx_meal_templates_usage_count ON meal_templates(usage_count DESC);
CREATE INDEX idx_meal_templates_last_used ON meal_templates(last_used DESC);
CREATE INDEX idx_meal_templates_created_by ON meal_templates(created_by);
CREATE INDEX idx_meal_templates_meal_type ON meal_templates(meal_type);

-- Indeksy dla tabel powiązanych
CREATE INDEX idx_meal_template_photos_template_id ON meal_template_photos(meal_template_id);
CREATE INDEX idx_meal_template_ingredients_template_id ON meal_template_ingredients(meal_template_id);

-- Funkcja automatycznego ustawiania name_lower
CREATE OR REPLACE FUNCTION update_meal_template_name_lower()
RETURNS TRIGGER AS $$
BEGIN
    NEW.name_lower = LOWER(NEW.name);
    NEW.updated_at = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

-- Trigger dla automatycznego ustawiania name_lower
CREATE TRIGGER trigger_meal_template_name_lower
    BEFORE INSERT OR UPDATE ON meal_templates
    FOR EACH ROW
    EXECUTE FUNCTION update_meal_template_name_lower();

-- Komentarze dla dokumentacji
COMMENT ON TABLE meal_templates IS 'Szablony posiłków dla ręcznego kreatora diet';
COMMENT ON TABLE meal_template_photos IS 'Zdjęcia przypisane do szablonów posiłków';
COMMENT ON TABLE meal_template_ingredients IS 'Składniki szablonów posiłków';

COMMENT ON COLUMN meal_templates.external_id IS 'Publiczne ID w stylu Firestore dla API';
COMMENT ON COLUMN meal_templates.name_lower IS 'Nazwa w małych literach dla wyszukiwania';
COMMENT ON COLUMN meal_templates.usage_count IS 'Liczba użyć szablonu';
COMMENT ON COLUMN meal_templates.last_used IS 'Data ostatniego użycia szablonu';