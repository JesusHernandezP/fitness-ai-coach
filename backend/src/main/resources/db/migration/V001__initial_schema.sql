CREATE TABLE users (
    id UUID PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    email VARCHAR(255) NOT NULL,
    password_hash VARCHAR(255),
    age INTEGER,
    height_cm DOUBLE PRECISION,
    weight_kg DOUBLE PRECISION,
    created_at TIMESTAMP(6) NOT NULL,
    CONSTRAINT uk_users_email UNIQUE (email)
);

CREATE TABLE body_metrics (
    id UUID PRIMARY KEY,
    user_id UUID NOT NULL,
    weight DOUBLE PRECISION NOT NULL,
    body_fat DOUBLE PRECISION,
    muscle_mass DOUBLE PRECISION,
    date DATE NOT NULL,
    CONSTRAINT fk_body_metrics_user FOREIGN KEY (user_id) REFERENCES users (id)
);

CREATE TABLE daily_logs (
    id UUID PRIMARY KEY,
    user_id UUID NOT NULL,
    log_date DATE NOT NULL,
    steps INTEGER,
    calories_consumed DOUBLE PRECISION,
    calories_burned DOUBLE PRECISION,
    CONSTRAINT fk_daily_logs_user FOREIGN KEY (user_id) REFERENCES users (id)
);

CREATE TABLE exercises (
    id UUID PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    muscle_group VARCHAR(255),
    equipment VARCHAR(255),
    description VARCHAR(255)
);

CREATE TABLE foods (
    id UUID PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    calories DOUBLE PRECISION NOT NULL,
    protein DOUBLE PRECISION NOT NULL,
    carbs DOUBLE PRECISION NOT NULL,
    fat DOUBLE PRECISION NOT NULL
);

CREATE TABLE goals (
    id UUID PRIMARY KEY,
    user_id UUID NOT NULL,
    goal_type VARCHAR(255) NOT NULL,
    target_weight DOUBLE PRECISION NOT NULL,
    target_calories DOUBLE PRECISION NOT NULL,
    CONSTRAINT fk_goals_user FOREIGN KEY (user_id) REFERENCES users (id)
);

CREATE TABLE meals (
    id UUID PRIMARY KEY,
    daily_log_id UUID NOT NULL,
    meal_type VARCHAR(255) NOT NULL,
    CONSTRAINT fk_meals_daily_log FOREIGN KEY (daily_log_id) REFERENCES daily_logs (id)
);

CREATE TABLE meal_items (
    id UUID PRIMARY KEY,
    meal_id UUID NOT NULL,
    food_id UUID NOT NULL,
    quantity DOUBLE PRECISION NOT NULL,
    calculated_calories DOUBLE PRECISION,
    CONSTRAINT fk_meal_items_meal FOREIGN KEY (meal_id) REFERENCES meals (id),
    CONSTRAINT fk_meal_items_food FOREIGN KEY (food_id) REFERENCES foods (id)
);

CREATE TABLE workout_sessions (
    id UUID PRIMARY KEY,
    daily_log_id UUID NOT NULL,
    exercise_id UUID NOT NULL,
    sets INTEGER,
    reps INTEGER,
    duration INTEGER,
    calories_burned DOUBLE PRECISION,
    CONSTRAINT fk_workout_sessions_daily_log FOREIGN KEY (daily_log_id) REFERENCES daily_logs (id),
    CONSTRAINT fk_workout_sessions_exercise FOREIGN KEY (exercise_id) REFERENCES exercises (id)
);

CREATE TABLE ai_recommendations (
    id UUID PRIMARY KEY,
    daily_log_id UUID NOT NULL,
    analysis_snapshot TEXT NOT NULL,
    advice TEXT NOT NULL,
    model VARCHAR(255) NOT NULL,
    created_at TIMESTAMP(6) NOT NULL
);

CREATE INDEX idx_body_metrics_user_id ON body_metrics (user_id);
CREATE INDEX idx_daily_logs_user_id ON daily_logs (user_id);
CREATE INDEX idx_goals_user_id ON goals (user_id);
CREATE INDEX idx_meals_daily_log_id ON meals (daily_log_id);
CREATE INDEX idx_meal_items_meal_id ON meal_items (meal_id);
CREATE INDEX idx_meal_items_food_id ON meal_items (food_id);
CREATE INDEX idx_workout_sessions_daily_log_id ON workout_sessions (daily_log_id);
CREATE INDEX idx_workout_sessions_exercise_id ON workout_sessions (exercise_id);
CREATE INDEX idx_ai_recommendations_daily_log_id ON ai_recommendations (daily_log_id);
