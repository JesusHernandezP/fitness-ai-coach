-- Migration: add explicit creation timestamp to goals for deterministic latest-goal query.
-- Date: 2026-03-16

ALTER TABLE goals
ADD COLUMN IF NOT EXISTS created_at TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP;

UPDATE goals
SET created_at = CURRENT_TIMESTAMP
WHERE created_at IS NULL;

ALTER TABLE goals
ALTER COLUMN created_at SET DEFAULT NULL;
