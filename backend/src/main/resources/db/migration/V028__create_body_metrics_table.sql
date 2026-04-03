-- Migration: create body_metrics table with one record per user per date.
-- Date: 2026-04-03

CREATE TABLE IF NOT EXISTS body_metrics (
    id UUID PRIMARY KEY,
    user_id UUID NOT NULL,
    weight DOUBLE PRECISION NOT NULL,
    body_fat DOUBLE PRECISION,
    muscle_mass DOUBLE PRECISION,
    date DATE NOT NULL,
    CONSTRAINT fk_body_metrics_user FOREIGN KEY (user_id) REFERENCES users (id),
    CONSTRAINT unique_body_metrics_user_date UNIQUE (user_id, date)
);
