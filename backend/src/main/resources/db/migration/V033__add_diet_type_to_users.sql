ALTER TABLE users
    ADD COLUMN IF NOT EXISTS diet_type VARCHAR(255);

UPDATE users
SET diet_type = COALESCE(diet_type, 'STANDARD');
